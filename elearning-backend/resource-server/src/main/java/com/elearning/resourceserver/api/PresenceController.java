package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.PresenceService;
import com.elearning.resourceserver.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @PostMapping("/seances/{id}/qr-code/generate")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<?> generateQrCode(@PathVariable UUID id) {
        return ResponseEntity.ok(presenceService.generateQrCodeToken(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/presences/scan")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> scanQrCode(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String token = payload.get("token");
        String ipAddress = request.getRemoteAddr();
        return ResponseEntity.ok(presenceService.scanQrCode(SecurityUtils.getCurrentUserId(), token, ipAddress));
    }

    @PostMapping("/presences/manual-code")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> scanManualCode(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String code = payload.get("code");
        String ipAddress = request.getRemoteAddr();
        return ResponseEntity.ok(presenceService.scanManualCode(SecurityUtils.getCurrentUserId(), code, ipAddress));
    }

    @GetMapping("/seances/{id}/presences")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<?> listPresences(@PathVariable UUID id) {
        return ResponseEntity.ok(presenceService.listPresences(id));
    }

    @PostMapping("/seances/{id}/manual-code/generate")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<?> generateManualCode(@PathVariable UUID id) {
        return ResponseEntity.ok(presenceService.generateManualCode(id, SecurityUtils.getCurrentUserId()));
    }
}
