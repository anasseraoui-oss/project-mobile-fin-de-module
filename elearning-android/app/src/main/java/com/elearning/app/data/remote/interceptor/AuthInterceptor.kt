package com.elearning.app.data.remote.interceptor

import com.elearning.app.data.local.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * OkHttp Interceptor responsible for:
 *  1. Injecting the Bearer token into every outgoing request.
 *  2. Detecting 401 Unauthorized responses.
 *  3. Attempting a token refresh exactly once (thread-safe via ReentrantLock).
 *  4. Retrying the original request with the new token.
 *  5. Clearing tokens and signalling logout if refresh also fails.
 *
 * ⚠️ IMPORTANT — Why ReentrantLock instead of kotlinx Mutex:
 *   kotlinx.coroutines.sync.Mutex + runBlocking on OkHttp thread pool threads
 *   creates a deadlock: the coroutine suspends waiting for the mutex, but
 *   runBlocking has already occupied the thread → SIGABRT (Fatal signal 6).
 *   A standard ReentrantLock never suspends — it blocks the calling thread
 *   directly, which is safe inside OkHttp's intercept().
 *
 * Uses [Provider<AuthApiService>] (lazy injection) to break the circular dependency:
 *   NetworkModule → AuthInterceptor → AuthApiService → Retrofit → NetworkModule
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authServiceProvider: Provider<com.elearning.app.data.remote.api.AuthApiService>
) : Interceptor {

    /**
     * Java ReentrantLock — blocks the thread directly (no coroutine suspension).
     * Safe to use inside OkHttp's intercept() which runs on a background thread.
     */
    private val refreshLock = ReentrantLock()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Read access token synchronously from DataStore using Dispatchers.IO
        val accessToken = runBlocking { tokenManager.getAccessToken() }
        val authenticatedRequest = originalRequest.withBearerToken(accessToken)
        val response = chain.proceed(authenticatedRequest)

        // Fast path: not a 401 — return immediately
        if (response.code != 401) return response

        // 401 → attempt token refresh
        response.close()

        // Only one thread at a time does the refresh
        refreshLock.lock()
        try {
            // Double-check: another thread may have already refreshed the token
            val currentToken = runBlocking { tokenManager.getAccessToken() }
            val tokenWasAlreadyRefreshed = currentToken != null &&
                    currentToken != accessToken

            return if (tokenWasAlreadyRefreshed) {
                // Another thread already refreshed — just retry with the new token
                chain.proceed(originalRequest.withBearerToken(currentToken))
            } else {
                // We are the first — actually do the refresh
                val refreshed = runBlocking { tryRefreshToken() }
                if (refreshed) {
                    val newToken = runBlocking { tokenManager.getAccessToken() }
                    chain.proceed(originalRequest.withBearerToken(newToken))
                } else {
                    // Refresh failed — clear stored tokens (user must re-login)
                    runBlocking { tokenManager.clearAll() }
                    chain.proceed(originalRequest) // Returns 401 to the ViewModel
                }
            }
        } finally {
            refreshLock.unlock()
        }
    }

    /**
     * Calls the /oauth2/token refresh endpoint and persists the new tokens.
     * Returns true on success, false on any network or server error.
     */
    private suspend fun tryRefreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        return try {
            val response = authServiceProvider.get().refreshToken(
                grantType = "refresh_token",
                refreshToken = refreshToken,
                clientId = CLIENT_ID
            )
            if (response.isSuccessful) {
                val body = response.body() ?: return false
                tokenManager.saveTokens(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken ?: refreshToken,
                    tokenType = body.tokenType ?: "Bearer",
                    expiresIn = body.expiresIn ?: 3600L
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val CLIENT_ID = "elearning-mobile-client"
    }
}

/** Extension: create a copy of the Request with an Authorization: Bearer header. */
private fun Request.withBearerToken(token: String?): Request {
    if (token.isNullOrBlank()) return this
    return newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
}
