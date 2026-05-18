// src/main/java/com/elearning/resourceserver/api/CertificatController.java
package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.CertificateService;
import com.elearning.resourceserver.domain.Certificat;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.repository.CertificatRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CertificatController {

    private final CertificateService certificateService;
    private final CertificatRepository certificatRepository;

    /**
     * GET /api/v1/certificats/me — Liste des certificats de l'apprenant courant
     */
    @GetMapping("/api/v1/certificats/me")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<List<Certificat>> getMyCertificates() {
        return ResponseEntity.ok(certificatRepository.findByApprenantId(SecurityUtils.getCurrentUserId()));
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
}
