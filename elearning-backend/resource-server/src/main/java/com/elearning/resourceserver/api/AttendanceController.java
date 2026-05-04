package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.AttendanceService;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/seances/{id}/qr-code/generate")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<?> generateAttendanceQrCode(@PathVariable UUID id) {
        String token = attendanceService.generateQrCodeToken(id);
        return ResponseEntity.ok(Map.of("qrToken", token, "expiresInMinutes", 5));
    }

    @PostMapping("/attendance/scan")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> scanQrCode(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        attendanceService.scanQrCode(SecurityUtils.getCurrentUserId(), token);
        return ResponseEntity.ok(Map.of("message", "Présence validée avec succès."));
    }

    @GetMapping("/seances/{id}/attendance")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ORGANISATION')")
    public ResponseEntity<?> listAttendees(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.listAttendees(id));
    }
}
