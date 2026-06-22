// src/main/java/com/elearning/resourceserver/api/CertificatController.java
package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.CertificateService;
import com.elearning.resourceserver.domain.Certificat;
import com.elearning.resourceserver.domain.Formation;
import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.dto.CertificatResponseDto;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.repository.CertificatRepository;
import com.elearning.resourceserver.repository.FormationRepository;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CertificatController {

    private final CertificateService certificateService;
    private final CertificatRepository certificatRepository;
    private final FormationRepository formationRepository;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/certificats/me — Liste des certificats de l'apprenant courant
     */
    @GetMapping("/api/v1/certificats/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CertificatResponseDto>> getMyCertificates() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!"ROLE_APPRENANT".equals(SecurityUtils.getCurrentUserRole())) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(certificatRepository.findByApprenantId(userId)
                .stream()
                .map(certificat -> mapToDto(certificat, userId))
                .toList());
    }

    /**
     * GET /api/v1/certificats/{id}/download — Téléchargement du certificat PDF (Presigned URL)
     */
    @GetMapping("/api/v1/certificats/{id}/download")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> downloadCertificate(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Certificat cert = certificatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificat non trouvé"));

        // Vérifier que le certificat appartient à l'utilisateur courant
        if (!cert.getApprenantId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès interdit"));
        }

        // Générer une presigned URL valide 60 minutes
        String downloadUrl = certificateService.getDownloadUrl(cert.getPdfKey(), 60);

        return ResponseEntity.ok(Map.of(
            "downloadUrl", downloadUrl,
            "expiresInMinutes", 60,
            "certificatId", cert.getId(),
            "issuedAt", cert.getIssuedAt()
        ));
    }

    /**
     * GET /verify/{verificationCode} — Vérification publique (sans auth)
     */
    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<?> verifyCertificate(@PathVariable UUID verificationCode) {
        Certificat cert = certificatRepository.findByVerificationCode(verificationCode).orElse(null);
        if (cert == null) {
            return ResponseEntity.status(404).body(Map.of(
                "valide", false,
                "message", "Certificat introuvable ou code invalide"
            ));
        }
        return ResponseEntity.ok(Map.of(
            "valide", true,
            "apprenantId", cert.getApprenantId(),
            "formationId", cert.getFormationId(),
            "issuedAt", cert.getIssuedAt(),
            "averageScore", cert.getAverageScore()
        ));
    }

    private CertificatResponseDto mapToDto(Certificat certificat, UUID userId) {
        Formation formation = formationRepository.findById(certificat.getFormationId()).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        String downloadUrl = certificat.getPdfKey() != null
                ? certificateService.getDownloadUrl(certificat.getPdfKey(), 60)
                : null;

        BigDecimal averageScore = certificat.getAverageScore() != null ? certificat.getAverageScore() : BigDecimal.ZERO;

        CertificatResponseDto dto = new CertificatResponseDto();
        dto.setId(certificat.getId());
        dto.setFormationId(certificat.getFormationId());
        dto.setFormationTitle(formation != null ? formation.getTitle() : "Formation");
        dto.setApprenantId(certificat.getApprenantId());
        dto.setLearnerName(user != null ? user.getFullName() : "Apprenant");
        dto.setIssuedAt(certificat.getIssuedAt());
        dto.setAverageScore(averageScore);
        dto.setScore(averageScore.intValue());
        dto.setMaxScore(100);
        dto.setPdfKey(certificat.getPdfKey());
        dto.setDownloadUrl(downloadUrl);
        dto.setVerificationCode(certificat.getVerificationCode());
        return dto;
    }
}
