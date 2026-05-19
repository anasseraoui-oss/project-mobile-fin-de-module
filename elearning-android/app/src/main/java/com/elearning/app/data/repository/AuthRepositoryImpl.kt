package com.elearning.app.data.repository

import android.content.Context
import android.net.Uri
import com.elearning.app.BuildConfig
import com.elearning.app.data.local.datastore.TokenManager
import com.elearning.app.data.remote.api.AuthApiService
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.AuthState
import com.elearning.app.domain.model.AuthTokens
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.User
import com.elearning.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepositoryImpl — data-layer implementation of [AuthRepository].
 *
 * Responsibilities:
 * - Classic login via ROPC grant (email/password).
 * - PKCE flow construction for Google & Facebook SSO.
 * - Token exchange, refresh, and revocation.
 * - Persisting tokens via [TokenManager].
 * - Exposing auth state as a Flow.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApiService: AuthApiService,
    private val resourceApiService: ResourceApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    // ──────────────────────────── CLASSIC LOGIN ──────────────────────────────

    override suspend fun loginClassic(email: String, password: String): Result<AuthTokens> {
        return try {
            val response = authApiService.loginWithPassword(
                username = email,
                password = password
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                val tokens = body.toDomain()
                tokenManager.saveTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    tokenType = tokens.tokenType,
                    expiresIn = tokens.expiresIn
                )
                Result.Success(tokens)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Authentication failed"
                Result.Error(Exception(errorMsg), errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    // ──────────────────────────── PKCE FLOW ──────────────────────────────────

    override fun buildGoogleAuthRequest(): String {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("${com.elearning.app.core.config.NetworkConfig.AUTH_SERVER_URL}oauth2/authorize"),
            Uri.parse("${com.elearning.app.core.config.NetworkConfig.AUTH_SERVER_URL}oauth2/token")
        )

        val (codeVerifier, codeChallenge) = generatePkcePair()

        val request = AuthorizationRequest.Builder(
            serviceConfig,
            "elearning-mobile-client",
            ResponseTypeValues.CODE,
            Uri.parse("com.elearning.app://oauth2redirect")
        )
            .setScopes("openid", "profile", "email", "offline_access")
            .setCodeVerifier(codeVerifier, codeChallenge, "S256")
            .setAdditionalParameters(
                mapOf("identity_provider" to "google")
            )
            .build()
            .toUri()
            .toString()

        // Store the code verifier for later use in exchangeCodeForTokens
        PkceStore.codeVerifier = codeVerifier

        return request
    }

    override fun buildFacebookAuthRequest(): String {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("${com.elearning.app.core.config.NetworkConfig.AUTH_SERVER_URL}oauth2/authorize"),
            Uri.parse("${com.elearning.app.core.config.NetworkConfig.AUTH_SERVER_URL}oauth2/token")
        )

        val (codeVerifier, codeChallenge) = generatePkcePair()

        val request = AuthorizationRequest.Builder(
            serviceConfig,
            "elearning-mobile-client",
            ResponseTypeValues.CODE,
            Uri.parse("com.elearning.app://oauth2redirect")
        )
            .setScopes("openid", "profile", "email", "offline_access")
            .setCodeVerifier(codeVerifier, codeChallenge, "S256")
            .setAdditionalParameters(
                mapOf("identity_provider" to "facebook")
            )
            .build()
            .toUri()
            .toString()

        PkceStore.codeVerifier = codeVerifier
        return request
    }

    // ──────────────────────────── CODE EXCHANGE ──────────────────────────────

    override suspend fun exchangeCodeForTokens(
        code: String,
        codeVerifier: String,
        redirectUri: String
    ): Result<AuthTokens> {
        return try {
            val response = authApiService.exchangeAuthorizationCode(
                code = code,
                redirectUri = redirectUri,
                codeVerifier = codeVerifier
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                val tokens = body.toDomain()
                tokenManager.saveTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    tokenType = tokens.tokenType,
                    expiresIn = tokens.expiresIn
                )
                Result.Success(tokens)
            } else {
                Result.Error(Exception("Token exchange failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    // ──────────────────────────── REFRESH TOKEN ──────────────────────────────

    override suspend fun refreshTokens(): Result<AuthTokens> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.Error(Exception("No refresh token stored"))

        return try {
            val response = authApiService.refreshToken(
                grantType = "refresh_token",
                refreshToken = refreshToken,
                clientId = "elearning-mobile-client"
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                val tokens = body.toDomain()
                tokenManager.saveTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken.ifBlank { refreshToken },
                    tokenType = tokens.tokenType,
                    expiresIn = tokens.expiresIn
                )
                Result.Success(tokens)
            } else {
                tokenManager.clearAll()
                Result.Error(Exception("Refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    // ──────────────────────────── LOGOUT ─────────────────────────────────────

    override suspend fun logout(): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
            if (!token.isNullOrBlank()) {
                authApiService.revokeToken(token = token)  // best-effort
            }
            tokenManager.clearAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            // Even on error, clear local state
            tokenManager.clearAll()
            Result.Success(Unit)
        }
    }

    // ──────────────────────────── CURRENT USER ───────────────────────────────

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = resourceApiService.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()!!.toDomain()
                tokenManager.saveUserInfo(user.id.toString(), user.role.name)
                Result.Success(user)
            } else {
                Result.Error(Exception("Failed to fetch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    // ──────────────────────────── AUTH STATE ─────────────────────────────────

    override fun observeAuthState(): Flow<AuthState> =
        tokenManager.observeTokenValidity().map { isValid ->
            if (isValid) AuthState.Authenticated else AuthState.Unauthenticated
        }

    // ──────────────────────────── REGISTER ───────────────────────────────────

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit> {
        return try {
            val response = authApiService.register(
                com.elearning.app.data.remote.dto.RegisterRequestDto(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
            )
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error(Exception("Registration failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    // ──────────────────────────── FORGOT PASSWORD ────────────────────────────

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = authApiService.forgotPassword(mapOf("email" to email))
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error(Exception("Request failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    // ──────────────────────────── PKCE UTILS ─────────────────────────────────

    private fun generatePkcePair(): Pair<String, String> {
        val secureRandom = SecureRandom()
        val codeVerifierBytes = ByteArray(32)
        secureRandom.nextBytes(codeVerifierBytes)
        val codeVerifier = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(codeVerifierBytes)

        val digest = MessageDigest.getInstance("SHA-256")
        val challengeBytes = digest.digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        val codeChallenge = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(challengeBytes)

        return Pair(codeVerifier, codeChallenge)
    }
}

/**
 * In-memory store for the PKCE code verifier during the OAuth2 flow.
 * In a production app, persist this securely (e.g. EncryptedSharedPreferences).
 */
object PkceStore {
    var codeVerifier: String = ""
}
