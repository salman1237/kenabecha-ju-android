package com.kenabechaju.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kenabechaju.app.BuildConfig
import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.AuthInterceptor
import com.kenabechaju.app.data.remote.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true // the API can grow fields this app doesn't know about yet
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            // OkHttp's default 10s read timeout is too short for this
            // client: it's shared with the assistant's SSE stream, whose
            // first bytes can legitimately arrive well past 10s (a
            // tool-call round trip — decide to search, run the DB query,
            // then a second OpenAI call for the actual reply — yields zero
            // SSE bytes the whole time). Confirmed live: a real request
            // that succeeded server-side (real OpenAI 200, real listings
            // found) still threw a client-side SocketTimeoutException
            // ("timeout") because no bytes arrived within 10s.
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
