package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * Mirrors the web app's own `lib/api/client.ts` 401-handling exactly: on a
 * 401, refresh once, retry the original request with the new token. OkHttp
 * calls this from its own dispatcher threads and may call it concurrently
 * for several requests that all failed around the same time — `synchronized`
 * plus the "did someone else already refresh?" check below de-duplicates
 * that into a single real network call, the same guarantee the web
 * client's shared in-flight `Deferred` gives it.
 *
 * `apiService` is a `Provider`, not a plain injected instance, specifically
 * to break the dependency cycle: the `OkHttpClient` this authenticator
 * attaches to is what `ApiService` itself is built from.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val apiService: Provider<ApiService>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null // already retried once — give up, don't loop.

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return synchronized(this) {
            runBlocking {
                val currentAccess = tokenStore.accessToken()
                if (currentAccess != null && currentAccess != failedToken) {
                    // Another concurrent 401 already refreshed while this one
                    // was waiting on the lock — reuse it, no network call.
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccess")
                        .build()
                }

                val refreshToken = tokenStore.refreshToken() ?: return@runBlocking null
                try {
                    val result = apiService.get().refresh("Bearer $refreshToken")
                    tokenStore.save(result.accessToken, result.refreshToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${result.accessToken}")
                        .build()
                } catch (e: Exception) {
                    // The refresh token is dead (expired, revoked, or already
                    // rotated by a concurrent use elsewhere) — clear the
                    // session rather than retry forever with a stale token.
                    tokenStore.clear()
                    null
                }
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result += 1
            prior = prior.priorResponse
        }
        return result
    }
}
