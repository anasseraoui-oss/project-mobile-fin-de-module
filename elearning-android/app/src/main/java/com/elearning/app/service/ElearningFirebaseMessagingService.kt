package com.elearning.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ElearningFirebaseMessagingService : FirebaseMessagingService() {

    // Inject repositories here if token needs to be sent to backend
    // @Inject lateinit var tokenRepository: TokenRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Automatically sync new token with Resource Server / Backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // tokenRepository.sendFCMToken(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: message.data["title"] ?: "Nouvelle notification"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val deepLink = message.data["deepLink"] // e.g. elearning://formation/123

        showNotification(title, body, deepLink)
    }

    private fun showNotification(title: String, body: String, deepLink: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "elearning_main_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifications Générales",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Deep linking Intent config
        // val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink ?: "elearning://home"))
        // val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: replace with app icon
            .setAutoCancel(true)
            // .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
