// src/main/java/com/elearning/resourceserver/api/AuthController.java
package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.enums.AuthProvider;
import com.elearning.resourceserver.domain.enums.Role;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * POST /api/v1/auth/register — Inscription locale (APPRENANT par défaut)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");

        if (email == null || password == null || firstName == null || lastName == null) {
            throw new ValidationException("Les champs email, password, firstName et lastName sont obligatoires");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Un compte avec cet email existe déjà");
        }
        if (password.length() < 8) {
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.APPRENANT);
        user.setProvider(AuthProvider.LOCAL);
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        User saved = userRepository.save(user);

        // Generate email verification token
        String verifyToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("email:verify:" + verifyToken, saved.getId().toString(), 24, TimeUnit.HOURS);
        log.info("Email verification token for user {}: {}", saved.getId(), verifyToken);

        return ResponseEntity.status(201).body(Map.of(
            "message", "Compte créé. Vérifiez votre email.",
            "userId", saved.getId(),
            "verifyToken", verifyToken // En prod: envoyer par email seulement
        ));
    }

    /**
     * POST /api/v1/auth/verify-email — Vérification de l'email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null) {
            throw new ValidationException("Token de vérification requis");
        }

        String redisKey = "email:verify:" + token;
        String userId = redisTemplate.opsForValue().get(redisKey);
        if (userId == null) {
            throw new ValidationException("Token invalide ou expiré");
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setIsEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redisTemplate.delete(redisKey);

        return ResponseEntity.ok(Map.of("message", "Email vérifié avec succès"));
    }

    /**
     * POST /api/v1/auth/forgot-password — Demande de réinitialisation
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null) {
            throw new ValidationException("Email requis");
        }

        // Always return 200 to avoid user enumeration
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("password:reset:" + resetToken, user.getId().toString(), 1, TimeUnit.HOURS);
            log.info("Password reset token for {}: {}", email, resetToken); // En prod: envoyer par email
        });

        return ResponseEntity.ok(Map.of("message", "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé"));
    }

    /**
     * POST /api/v1/auth/reset-password — Réinitialisation du mot de passe
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        if (token == null || newPassword == null) {
            throw new ValidationException("Token et nouveau mot de passe requis");
        }
        if (newPassword.length() < 8) {
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères");
        }

        String redisKey = "password:reset:" + token;
        String userId = redisTemplate.opsForValue().get(redisKey);
        if (userId == null) {
            throw new ValidationException("Token invalide ou expiré");
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redisTemplate.delete(redisKey);

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }

    /**
     * GET /api/v1/auth/me — Profil courant (sans données sensibles)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Ne jamais exposer passwordHash
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "avatarKey", user.getAvatarKey() != null ? user.getAvatarKey() : "",
            "role", user.getRole().name(),
            "organisationId", user.getOrganisationId() != null ? user.getOrganisationId() : "",
            "isEmailVerified", user.getIsEmailVerified(),
            "createdAt", user.getCreatedAt()
        ));
    }
}
