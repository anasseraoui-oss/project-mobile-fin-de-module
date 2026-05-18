package com.elearning.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Extension for DataStore on Context
val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * TokenManager — Secure storage for OAuth2 tokens using Jetpack DataStore.
 *
 * All token data is stored in DataStore<Preferences>. For production-level
 * encryption, the DataStore should be backed by EncryptedFile or AndroidKeyStore.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_TOKEN_TYPE = stringPreferencesKey("token_type")
        private val KEY_EXPIRES_AT = longPreferencesKey("expires_at")   // epoch millis
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")

        /** Grace period before the access token is considered expired (30 seconds). */
        private val EXPIRY_GRACE_MS = TimeUnit.SECONDS.toMillis(30)
    }

    // ──────────────────────────────── WRITE ──────────────────────────────────

    /**
     * Persist access + refresh tokens after a successful authentication.
     * @param expiresIn seconds until expiry, as returned by the token endpoint.
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        expiresIn: Long = 3600L
    ) {
        val expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn)
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_TOKEN_TYPE] = tokenType
            prefs[KEY_EXPIRES_AT] = expiresAt
        }
    }

    /**
     * Update only the access token (e.g., after a silent refresh).
     */
    suspend fun updateAccessToken(newAccessToken: String, expiresIn: Long = 3600L) {
        val expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn)
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = newAccessToken
            prefs[KEY_EXPIRES_AT] = expiresAt
        }
    }

    /**
     * Save user metadata after fetching the profile.
     */
    suspend fun saveUserInfo(userId: String, role: String) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_ROLE] = role
        }
    }

    /**
     * Remove all stored tokens and user info (logout / revocation).
     */
    suspend fun clearAll() {
        context.authDataStore.edit { it.clear() }
    }

    // ──────────────────────────────── READ ───────────────────────────────────

    /** Returns the raw access token string, or null if not stored. */
    suspend fun getAccessToken(): String? =
        context.authDataStore.data.first()[KEY_ACCESS_TOKEN]

    /** Returns the raw refresh token string, or null if not stored. */
    suspend fun getRefreshToken(): String? =
        context.authDataStore.data.first()[KEY_REFRESH_TOKEN]

    /** Returns the token type ("Bearer"), or "Bearer" by default. */
    suspend fun getTokenType(): String =
        context.authDataStore.data.first()[KEY_TOKEN_TYPE] ?: "Bearer"

    /** Returns epoch millis when the access token expires, or 0 if not set. */
    suspend fun getExpiresAt(): Long =
        context.authDataStore.data.first()[KEY_EXPIRES_AT] ?: 0L

    suspend fun getUserId(): String? =
        context.authDataStore.data.first()[KEY_USER_ID]

    suspend fun getUserRole(): String? =
        context.authDataStore.data.first()[KEY_USER_ROLE]

    // ──────────────────────────────── STATE ──────────────────────────────────

    /**
     * Returns true when a valid (non-expired) access token exists.
     * Accounts for the 30-second grace window.
     */
    suspend fun isTokenValid(): Boolean {
        val token = getAccessToken() ?: return false
        val expiresAt = getExpiresAt()
        if (expiresAt == 0L) return token.isNotBlank()
        return System.currentTimeMillis() < (expiresAt - EXPIRY_GRACE_MS)
    }

    /**
     * Returns true when a refresh token is stored (user was authenticated).
     */
    suspend fun hasRefreshToken(): Boolean =
        !getRefreshToken().isNullOrBlank()

    /**
     * Observe token validity as a Flow — emits whenever the stored preferences change.
     */
    fun observeTokenValidity(): Flow<Boolean> =
        context.authDataStore.data.map { prefs ->
            val token = prefs[KEY_ACCESS_TOKEN]
            val expiresAt = prefs[KEY_EXPIRES_AT] ?: 0L
            if (token.isNullOrBlank()) return@map false
            if (expiresAt == 0L) return@map true
            System.currentTimeMillis() < (expiresAt - EXPIRY_GRACE_MS)
        }

    /**
     * Convenience: build the full "Bearer <token>" header value for injection.
     */
    suspend fun getBearerHeader(): String? {
        val token = getAccessToken() ?: return null
        val type = getTokenType()
        return "$type $token"
    }
}
