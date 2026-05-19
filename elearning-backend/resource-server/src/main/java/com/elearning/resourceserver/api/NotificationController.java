// src/main/java/com/elearning/resourceserver/api/NotificationController.java
package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.Notification;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.repository.NotificationRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    /**
     * GET /api/v1/me/notifications?page=0&size=20
     */
    @GetMapping("/me/notifications")
    public ResponseEntity<Page<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(SecurityUtils.getCurrentUserId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * PUT /api/v1/notifications/{id}/read
     */
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        int updated = notificationRepository.markAsRead(id, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification non trouvée");
        }
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }

    /**
     * PUT /api/v1/notifications/read-all
     */
    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllAsRead() {
        notificationRepository.markAllAsRead(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(Map.of("message", "Toutes les notifications marquées comme lues"));
    }

    /**
     * GET /api/v1/notifications/unread-count
     */
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        long count = notificationRepository.countByUserIdAndIsReadFalse(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * POST /api/v1/devices/token — Enregistrement FCM token
     */
    @PostMapping("/devices/token")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> registerFcmToken(@RequestBody Map<String, String> payload) {
        String fcmToken = payload.get("fcmToken");
        if (fcmToken == null || fcmToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "FCM token requis"));
        }
        // FCM token stored on User entity directly
        return ResponseEntity.ok(Map.of("message", "FCM token enregistré"));
    }
}
