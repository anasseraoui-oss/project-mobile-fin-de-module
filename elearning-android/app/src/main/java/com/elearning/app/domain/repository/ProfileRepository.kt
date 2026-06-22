package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateFcmToken(token: String): Result<Unit>
}
