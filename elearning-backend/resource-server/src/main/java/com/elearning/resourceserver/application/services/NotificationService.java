package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Inscription;
import com.elearning.resourceserver.domain.Notification;
import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.enums.InscriptionStatus;
import com.elearning.resourceserver.domain.enums.NotificationType;
import com.elearning.resourceserver.repository.InscriptionRepository;
import com.elearning.resourceserver.repository.NotificationRepository;
import com.elearning.resourceserver.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    @Transactional
    public void sendToUser(UUID userId, String title, String message, Map<String, String> extraData) {
        sendToUser(userId, title, message, extraData, null);
    }

    @Transactional
    public void sendToUser(UUID userId, String title, String message, Map<String, String> extraData, String dedupeKey) {
        if (userId == null) {
            return;
        }
        if (isDuplicate(userId, dedupeKey)) {
            log.debug("Skipping duplicate notification {} for user {}", dedupeKey, userId);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        Notification notification = persistNotification(userId, title, message, extraData);
        payload.put("id", notification.getId().toString());
        payload.put("title", title);
        payload.put("message", message);
        payload.put("createdAt", notification.getCreatedAt());
        if (extraData != null) {
            payload.putAll(extraData);
        }

        log.info("Sending notification to user {}: {}", userId, title);
        messagingTemplate.convertAndSend("/topic/user." + userId, payload);
        sendPushBestEffort(userId, title, message, extraData);
    }

    @Transactional
    public int sendToFormationSubscribers(UUID formationId, String title, String message, Map<String, String> extraData, String dedupeKey) {
        if (formationId == null) {
            return 0;
        }
        List<Inscription> recipients = Stream.concat(
                inscriptionRepository.findByFormationIdAndStatus(formationId, InscriptionStatus.EN_COURS).stream(),
                inscriptionRepository.findByFormationIdAndStatus(formationId, InscriptionStatus.TERMINEE).stream()
        ).toList();

        recipients.forEach(inscription -> sendToUser(
                inscription.getApprenantId(),
                title,
                message,
                extraData,
                dedupeKey == null ? null : dedupeKey + ":" + formationId
        ));
        return recipients.size();
    }

    public void sendToTopic(String topicName, String title, String message, Map<String, String> extraData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        payload.put("title", title);
        payload.put("message", message);
        payload.put("createdAt", LocalDateTime.now());
        if (extraData != null) {
            payload.putAll(extraData);
        }

        log.info("Sending notification to topic {}: {}", topicName, title);
        messagingTemplate.convertAndSend("/topic/" + topicName, payload);
    }

    private Notification persistNotification(UUID userId, String title, String message, Map<String, String> extraData) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(resolveType(extraData))
                .title(title)
                .body(message)
                .data(toJson(extraData))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    private boolean isDuplicate(UUID userId, String dedupeKey) {
        if (dedupeKey == null || dedupeKey.isBlank()) {
            return false;
        }
        String key = "notification:dedupe:" + userId + ":" + dedupeKey;
        Boolean inserted = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofHours(24));
        return Boolean.FALSE.equals(inserted);
    }

    private NotificationType resolveType(Map<String, String> extraData) {
        if (extraData == null) {
            return NotificationType.GENERAL;
        }
        String type = extraData.get("type");
        if (type == null || type.isBlank()) {
            return NotificationType.GENERAL;
        }
        try {
            return NotificationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return NotificationType.GENERAL;
        }
    }

    private String toJson(Map<String, String> extraData) {
        if (extraData == null || extraData.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(extraData);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private void sendPushBestEffort(UUID userId, String title, String message, Map<String, String> extraData) {
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            return;
        }

        userRepository.findById(userId)
                .map(User::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .ifPresent(token -> {
                    try {
                        Message.Builder builder = Message.builder()
                                .setToken(token)
                                .setNotification(com.google.firebase.messaging.Notification.builder()
                                        .setTitle(title)
                                        .setBody(message)
                                        .build());
                        if (extraData != null && !extraData.isEmpty()) {
                            builder.putAllData(extraData);
                        }
                        FirebaseMessaging.getInstance(firebaseApp).sendAsync(builder.build());
                    } catch (Exception e) {
                        log.warn("Could not enqueue FCM notification for user {}: {}", userId, e.getMessage());
                    }
                });
    }
}
