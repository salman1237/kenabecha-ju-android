package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.remote.dto.TokenResponseDto
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Deliberately separate from [ApiService] and built from a plain
 * [okhttp3.OkHttpClient] with no [TokenAuthenticator] attached (see
 * `NetworkModule`'s `@RefreshClient`-qualified client). Calling refresh
 * through the authenticated client is a real, confirmed deadlock risk if
 * the refresh token is itself invalid (e.g. the app was pointed at a
 * different backend while a stale session was still stored): that 401
 * would recursively invoke [TokenAuthenticator.authenticate] on a call
 * already inside it, and [kotlinx.coroutines.sync.Mutex] isn't reentrant —
 * confirmed live, two concurrent 401s against a stale session hung forever.
 */
interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Header("Authorization") refreshBearer: String): TokenResponseDto
}
