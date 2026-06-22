package com.elearning.app.domain.repository

import com.elearning.app.domain.model.AppNotification
import com.elearning.app.domain.model.Result

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<AppNotification>>
    suspend fun updateFcmToken(token: String): Result<Unit>
}
