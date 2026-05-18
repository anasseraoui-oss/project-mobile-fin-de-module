package com.elearning.app.di

import com.elearning.app.BuildConfig
import com.elearning.app.data.local.datastore.TokenManager
import com.elearning.app.data.remote.api.AuthApiService
import com.elearning.app.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * NetworkModule — provides Retrofit instances and OkHttp clients.
 *
 * Two Retrofit instances are created:
 *  - "auth"     → points to the Authorization Server (no auth interceptor to avoid loops)
 *  - "resource" → points to the Resource Server with Bearer token injection
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ──────────────────────────── Logging ────────────────────────────────────

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

    // ──────────────────────────── OkHttp Clients ─────────────────────────────

    /**
     * Auth OkHttpClient — used only for token requests (no Bearer header to avoid loops).
     */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Resource OkHttpClient — attaches Bearer token automatically and retries on 401.
     */
    @Provides
    @Singleton
    @Named("resource")
    fun provideResourceOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)       // JWT Bearer injection + refresh
        .addInterceptor(loggingInterceptor)    // After auth so token is visible in logs (debug)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)     // Longer for stream URLs
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ──────────────────────────── Retrofit Instances ─────────────────────────

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("auth") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(com.elearning.app.core.config.NetworkConfig.AUTH_SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("resource")
    fun provideResourceRetrofit(@Named("resource") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(com.elearning.app.core.config.NetworkConfig.RESOURCE_SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ──────────────────────────── API Services ────────────────────────────────

    /**
     * AuthApiService uses the "auth" Retrofit (no auth interceptor = no circular dependency).
     */
    @Provides
    @Singleton
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideResourceApiService(@Named("resource") retrofit: Retrofit): com.elearning.app.data.remote.api.ResourceApiService =
        retrofit.create(com.elearning.app.data.remote.api.ResourceApiService::class.java)
}
