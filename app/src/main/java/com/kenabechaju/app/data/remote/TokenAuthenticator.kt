package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.TokenStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors the web app's own `lib/api/client.ts` 401-handling exactly: on a
 * 401, refresh once, retry the original request with the new token. OkHttp
 * calls this from its own dispatcher threads and may call it concurrently
 * for several requests that all failed around the same time — the [mutex]
 * plus the "did someone else already refresh?" check below de-duplicates
 * that into a single real network call, the same guarantee the web
 * client's shared in-flight `Deferred` gives it.
 *
 * A [Mutex], not a `synchronized(this)` block, guarding the refresh below —
 * a real deadlock, not a hypothetical one: a plain JVM monitor held across a
 * suspended network call can starve OkHttp's own dispatcher of the thread it
 * needs to actually run that call. And [refreshApi] is deliberately typed as
 * [RefreshApi] — a client with no [TokenAuthenticator] attached — not the
 * full [ApiService]: routing the refresh call back through *this same*
 * authenticator risks a self-deadlock the moment the refresh token is
 * itself invalid (a 401 from `/auth/refresh` would recursively call
 * [authenticate] on a call already inside it, and [Mutex] isn't reentrant).
 * Both failure modes were confirmed live, not hypothetical: two concurrent
 * 401s against a stale session (from switching this build to point at a
 * different backend while an old session was still stored) hung every
 * request forever with no response ever logged.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null // already retried once — give up, don't loop.

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return runBlocking {
            mutex.withLock {
                val currentAccess = tokenStore.accessToken()
                if (currentAccess != null && currentAccess != failedToken) {
                    // Another concurrent 401 already refreshed while this one
                    // was waiting on the lock — reuse it, no network call.
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccess")
                        .build()
                }

                val refreshToken = tokenStore.refreshToken() ?: return@withLock null
                try {
                    val result = refreshApi.refresh("Bearer $refreshToken")
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
