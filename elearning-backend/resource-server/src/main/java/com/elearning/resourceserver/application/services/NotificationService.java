package com.elearning.resourceserver.application.services;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    // private final DeviceTokenRepository deviceTokenRepository;
    // private final NotificationRepository notificationRepository;

    public void sendToUser(UUID userId, String title, String body, Map<String, String> data) {
        // Enregistrer en BDD historique
        saveNotification(userId, title, body, data);

        // Fetch tokens from DB: List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        String mockToken = "sample-device-token"; // Remplacer par itération sur les tokens
        
        try {
            Message message = Message.builder()
                    .setToken(mockToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();
            
            // FirebaseMessaging.getInstance().send(message);
            log.info("Sent push notification to User: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send notification to User: {}", userId, e);
        }
    }

    public void sendToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();

            // FirebaseMessaging.getInstance().send(message);
            log.info("Sent push notification to Topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send notification to topic: {}", topic, e);
        }
    }

    private void saveNotification(UUID userId, String title, String body, Map<String, String> data) {
        // com.elearning.resourceserver.domain.Notification notif = new ...
        // notificationRepository.save(notif);
    }
}
