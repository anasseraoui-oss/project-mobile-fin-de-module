// src/main/java/com/elearning/resourceserver/api/UserProfileController.java
package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.dto.UserProfileResponseDto;
import com.elearning.resourceserver.domain.enums.InscriptionStatus;
import com.elearning.resourceserver.domain.enums.QuizStatus;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.CertificatRepository;
import com.elearning.resourceserver.repository.InscriptionRepository;
import com.elearning.resourceserver.repository.OrganisationRepository;
import com.elearning.resourceserver.repository.ProgressRepository;
import com.elearning.resourceserver.repository.ProgressionRepository;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ProgressionRepository progressionRepository;
    private final ProgressRepository progressRepository;
    private final CertificatRepository certificatRepository;
    private final MinioService minioService;

    @Value("${minio.bucket-public:elearning-public}")
    private String bucketPublic;

    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponseDto> getMyProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvÃ©"));

        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setAvatarKey(user.getAvatarKey());
        dto.setAvatarUrl(generateAvatarUrl(user.getAvatarKey()));
        dto.setOrganisationId(user.getOrganisationId());
        dto.setOrganisationName(resolveOrganisationName(user.getOrganisationId()));
        dto.setEnrolledFormations(inscriptionRepository.findByApprenantId(userId).size());
        dto.setCompletedFormations((int) inscriptionRepository.findByApprenantId(userId).stream()
                .filter(inscription -> inscription.getStatus() == InscriptionStatus.TERMINEE)
                .count());
        dto.setCompletedCourses((int) progressionRepository.findAllByApprenantId(userId).stream()
                .filter(progression -> progression.getQuizStatus() == QuizStatus.VALIDE)
                .count());
        dto.setCertificatesCount(certificatRepository.findByApprenantId(userId).size());
        Long watchedSeconds = progressRepository.sumWatchedSecondsByUserId(userId);
        dto.setHoursSpent((int) Math.ceil((watchedSeconds != null ? watchedSeconds : 0L) / 3600.0));

        return ResponseEntity.ok(dto);
    }

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

    private String resolveOrganisationName(UUID organisationId) {
        if (organisationId == null) {
            return null;
        }
        return organisationRepository.findById(organisationId)
                .map(org -> org.getName())
                .orElse(null);
    }

    private String generateAvatarUrl(String avatarKey) {
        if (avatarKey == null || avatarKey.isBlank()) {
            return null;
        }
        try {
            return minioService.generatePresignedUrl(bucketPublic, avatarKey, 60);
        } catch (Exception ignored) {
            return null;
        }
    }
}
