package com.elearning.app.data.remote.interceptor

import com.elearning.app.data.local.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * OkHttp Interceptor responsible for:
 *  1. Injecting the Bearer token into every outgoing request.
 *  2. Detecting 401 Unauthorized responses.
 *  3. Attempting a token refresh exactly once (thread-safe via Mutex).
 *  4. Retrying the original request with the new token.
 *  5. Clearing tokens and signalling logout if refresh also fails.
 *
 * Uses [Provider<AuthService>] (lazy injection) to break the circular dependency:
 *   NetworkModule -> AuthInterceptor -> AuthService -> Retrofit -> NetworkModule
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    // Lazy provider avoids circular dependency at graph construction time
    private val authServiceProvider: Provider<com.elearning.app.data.remote.api.AuthApiService>
) : Interceptor {

    /** Ensures only ONE refresh call runs at a time across concurrent requests. */
    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Attach current access token (if available)
        val authenticatedRequest = runBlocking {
            originalRequest.withBearerToken(tokenManager.getAccessToken())
        }

        val response = chain.proceed(authenticatedRequest)

        // Fast path: not a 401, return immediately
        if (response.code != 401) return response

        // 401 → attempt token refresh
        response.close()

        return runBlocking {
            refreshMutex.withLock {
                // Double-check: another coroutine may have already refreshed
                val currentToken = tokenManager.getAccessToken()
                val alreadyRefreshed = currentToken != null &&
                        currentToken != (authenticatedRequest.header("Authorization")
                    ?.removePrefix("Bearer ")?.trim())

                if (alreadyRefreshed) {
                    // Re-try with the token that was refreshed by another coroutine
                    chain.proceed(originalRequest.withBearerToken(currentToken))
                } else {
                    // Actually attempt a token refresh
                    val refreshed = tryRefreshToken()
                    if (refreshed) {
                        val newToken = tokenManager.getAccessToken()
                        chain.proceed(originalRequest.withBearerToken(newToken))
                    } else {
                        // Refresh failed — clear tokens (user must re-login)
                        tokenManager.clearAll()
                        chain.proceed(originalRequest) // will return 401 to ViewModel
                    }
                }
            }
        }
    }

    /**
     * Calls the refresh endpoint and updates the stored tokens.
     * Returns true on success, false on any error.
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

/** Extension: create a copy of the Request with an Authorization header. */
private fun Request.withBearerToken(token: String?): Request {
    if (token.isNullOrBlank()) return this
    return newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
}
