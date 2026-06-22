package com.elearning.app.data.repository

import android.util.Log
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.AppNotification
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: ResourceApiService
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<AppNotification>> {
        return try {
            val dtos = api.getNotifications().content.orEmpty()
            Result.Success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications", e)
            Result.Error(e, "Impossible de charger les notifications")
        }
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            val response = api.updateFcmToken(mapOf("fcmToken" to token))
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error(Exception("Failed to update FCM token"))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token", e)
            Result.Error(e)
        }
    }

    private companion object {
        private const val TAG = "NotificationRepositoryImpl"
    }
}
