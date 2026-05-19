// src/main/java/com/elearning/resourceserver/api/UserProfileController.java
package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;

    /**
     * GET /api/v1/users/{id}/profile — Profil public : UNIQUEMENT firstName + lastName + avatarUrl
     * Jamais email/passwordHash exposés
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "avatarKey", user.getAvatarKey() != null ? user.getAvatarKey() : ""
        ));
    }
}
