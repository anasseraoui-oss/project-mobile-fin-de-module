package com.elearning.app.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val FILE_NAME = "auth_prefs_encrypted"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"

        private val EXPIRY_GRACE_MS = TimeUnit.SECONDS.toMillis(30)
    }

    private val securePrefs: SharedPreferences by lazy { createSecurePreferences() }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        expiresIn: Long = 3600L
    ) = withContext(Dispatchers.IO) {
        val expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn)
        securePrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    suspend fun updateAccessToken(newAccessToken: String, expiresIn: Long = 3600L) = withContext(Dispatchers.IO) {
        val expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn)
        securePrefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    suspend fun saveUserInfo(userId: String, role: String) = withContext(Dispatchers.IO) {
        securePrefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        securePrefs.edit().clear().apply()
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        securePrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        securePrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    suspend fun getTokenType(): String = withContext(Dispatchers.IO) {
        securePrefs.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
    }

    suspend fun getExpiresAt(): Long = withContext(Dispatchers.IO) {
        securePrefs.getLong(KEY_EXPIRES_AT, 0L)
    }

    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        securePrefs.getString(KEY_USER_ID, null)
    }

    suspend fun getUserRole(): String? = withContext(Dispatchers.IO) {
        securePrefs.getString(KEY_USER_ROLE, null)
    }

    suspend fun isTokenValid(): Boolean = withContext(Dispatchers.IO) {
        isTokenValidSnapshot()
    }

    suspend fun hasRefreshToken(): Boolean = withContext(Dispatchers.IO) {
        !securePrefs.getString(KEY_REFRESH_TOKEN, null).isNullOrBlank()
    }

    fun observeTokenValidity(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(isTokenValidSnapshot())
        }
        trySend(isTokenValidSnapshot())
        securePrefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { securePrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    suspend fun getBearerHeader(): String? {
        val token = getAccessToken() ?: return null
        val type = getTokenType()
        return "$type $token"
    }

    private fun isTokenValidSnapshot(): Boolean {
        val token = securePrefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = securePrefs.getLong(KEY_EXPIRES_AT, 0L)
        if (token.isNullOrBlank()) return false
        if (expiresAt == 0L) return true
        return System.currentTimeMillis() < (expiresAt - EXPIRY_GRACE_MS)
    }

    @Suppress("DEPRECATION")
    private fun createSecurePreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context.applicationContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
