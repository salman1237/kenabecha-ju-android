package com.kenabechaju.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth")

/**
 * Holds the access/refresh token pair — the one thing standing in for the
 * browser cookie jar the web app relies on. Backed by DataStore for real
 * persistence across process death, with an in-memory cache in front so the
 * OkHttp interceptor (which runs on every single request) never blocks on
 * disk I/O to read it.
 */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")

    @Volatile private var cachedAccess: String? = null
    @Volatile private var cachedRefresh: String? = null
    @Volatile private var loaded = false

    private suspend fun ensureLoaded() {
        if (loaded) return
        val prefs = context.authDataStore.data.first()
        cachedAccess = prefs[accessKey]
        cachedRefresh = prefs[refreshKey]
        loaded = true
    }

    suspend fun accessToken(): String? {
        ensureLoaded()
        return cachedAccess
    }

    suspend fun refreshToken(): String? {
        ensureLoaded()
        return cachedRefresh
    }

    /** Synchronous read for the OkHttp interceptor, which cannot suspend.
     *  Safe once [ensureLoaded] has run at least once this process — every
     *  app start calls [accessToken] during the splash/start-destination
     *  check before any authenticated request could possibly fire. */
    fun accessTokenBlocking(): String? = cachedAccess
    fun refreshTokenBlocking(): String? = cachedRefresh

    suspend fun save(accessToken: String, refreshToken: String) {
        cachedAccess = accessToken
        cachedRefresh = refreshToken
        loaded = true
        context.authDataStore.edit { prefs ->
            prefs[accessKey] = accessToken
            prefs[refreshKey] = refreshToken
        }
    }

    suspend fun clear() {
        cachedAccess = null
        cachedRefresh = null
        loaded = true
        context.authDataStore.edit { it.clear() }
    }
}
