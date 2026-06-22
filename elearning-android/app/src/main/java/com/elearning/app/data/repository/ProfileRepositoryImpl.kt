package com.elearning.app.data.repository

import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.UserProfile
import com.elearning.app.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ResourceApiService
) : ProfileRepository {

    override suspend fun getProfile(): Result<UserProfile> = try {
        Result.Success(api.getCurrentUserProfile().toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = try {
        val response = api.updateFcmToken(mapOf("fcmToken" to token))
        if (response.isSuccessful) {
            Result.Success(Unit)
        } else {
            Result.Error(IllegalStateException("FCM token update failed: HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
