package com.elearning.app.domain.repository

import com.elearning.app.domain.model.AuthState
import com.elearning.app.domain.model.AuthTokens
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contract for all authentication operations.
 * Implementations live in the data layer.
 */
interface AuthRepository {

    /**
     * Login with email & password (Resource Owner Password Credentials flow).
     */
    suspend fun loginClassic(email: String, password: String): Result<AuthTokens>

    /**
     * Initiate OAuth2 PKCE flow for Google. Returns the authorization request URI.
     */
    fun buildGoogleAuthRequest(): String

    /**
     * Initiate OAuth2 PKCE flow for Facebook. Returns the authorization request URI.
     */
    fun buildFacebookAuthRequest(): String

    /**
     * Exchange the authorization code + code verifier for tokens (PKCE).
     */
    suspend fun exchangeCodeForTokens(
        code: String,
        codeVerifier: String,
        redirectUri: String
    ): Result<AuthTokens>

    /**
     * Refresh the access token using the stored refresh token.
     */
    suspend fun refreshTokens(): Result<AuthTokens>

    /**
     * Revoke all tokens and clear local state.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Fetch the currently authenticated user profile.
     */
    suspend fun getCurrentUser(): Result<User>

    /**
     * Observe the current authentication state as a Flow.
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Register a new user with email and password.
     */
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit>

    /**
     * Request a password-reset email.
     */
    suspend fun forgotPassword(email: String): Result<Unit>

    /**
     * Login via Google SSO (with ID Token).
     */
    suspend fun loginWithGoogle(idToken: String): Result<AuthTokens>
}
