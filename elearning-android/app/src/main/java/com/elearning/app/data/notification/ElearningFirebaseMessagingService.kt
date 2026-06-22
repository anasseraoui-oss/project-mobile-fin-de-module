package com.elearning.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elearning.app.domain.repository.ProfileRepository
import com.elearning.app.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ElearningFirebaseMessagingService — handles incoming FCM push notifications.
 *
 * Responsibilities:
 *  - Display system notifications with deep link support.
 *  - Forward the FCM token to the backend on registration.
 */
@AndroidEntryPoint
class ElearningFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "elearning_default_channel"
        const val CHANNEL_NAME = "E-Learning Notifications"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("FCM", "Message received from: ${message.from}")
        android.util.Log.d("FCM", "Data payload: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "E-Learning"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val deepLink = message.data["deep_link"]

        showNotification(title, body, deepLink)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "New FCM token generated: $token")
        serviceScope.launch {
            val result = profileRepository.updateFcmToken(token)
            android.util.Log.d("FCM", "Token update result: $result")
        }
    }

    private fun showNotification(title: String, body: String, deepLink: String?) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure the channel exists (required on API 26+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications de la plateforme E-Learning"
        }
        notificationManager.createNotificationChannel(channel)

        // Build the intent with the deep link
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            deepLink?.let { putExtra("deep_link", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
