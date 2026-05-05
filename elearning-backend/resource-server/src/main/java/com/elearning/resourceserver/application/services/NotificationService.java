package com.elearning.resourceserver.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(UUID userId, String title, String message, Map<String, String> extraData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        payload.put("title", title);
        payload.put("message", message);
        payload.put("createdAt", LocalDateTime.now());
        if (extraData != null) {
            payload.putAll(extraData);
        }

        log.info("Sending notification to user {}: {}", userId, title);
        messagingTemplate.convertAndSend("/topic/user." + userId, payload);
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
}
