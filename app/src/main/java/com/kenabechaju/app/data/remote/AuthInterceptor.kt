package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches `Authorization: Bearer <access_token>` to every request that
 * doesn't already carry one — the escape hatch is for `ApiService.refresh`/
 * `.logout`, which explicitly supply the *refresh* token via `@Header`
 * instead, and must never have this interceptor overwrite it with the
 * (wrong, expired-by-definition) access token.
 */
class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header("Authorization") != null) {
            return chain.proceed(original)
        }
        // Interceptors run off the main thread on OkHttp's own dispatcher,
        // and TokenStore's synchronous read is only safe once at least one
        // suspend read has happened this process — true by the time any
        // authenticated call fires, since the app's own start-destination
        // check always reads it first.
        val token = runBlocking { tokenStore.accessToken() }
        val authed = if (token != null) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        return chain.proceed(authed)
    }
}
