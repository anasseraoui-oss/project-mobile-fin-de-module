package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.CertificateService;
import com.elearning.resourceserver.domain.Certificat;
import com.elearning.resourceserver.repository.CertificatRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/api/v1/certificats/me")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<List<Certificat>> getMyCertificates() {
        return ResponseEntity.ok(certificatRepository.findByApprenantId(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<?> verifyCertificate(@PathVariable UUID verificationCode) {
        Certificat cert = certificatRepository.findByVerificationCode(verificationCode).orElse(null);
        if (cert == null) {
            return ResponseEntity.status(404).body(Map.of("valide", false, "message", "Certificat introuvable"));
        }
        return ResponseEntity.ok(Map.of(
            "valide", true,
            "apprenantId", cert.getApprenantId(),
            "formationId", cert.getFormationId(),
            "issuedAt", cert.getIssuedAt()
        ));
    }
}
