package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.Progression;
import com.elearning.resourceserver.repository.ProgressionRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgressionController {

    private final ProgressionRepository progressionRepository;

    @GetMapping("/apprenants/me/progression")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<List<Progression>> getMyProgression() {
        return ResponseEntity.ok(progressionRepository.findAllByApprenantId(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/formations/{id}/progression/me")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<List<Progression>> getMyProgressionForFormation(@PathVariable UUID id) {
        return ResponseEntity.ok(progressionRepository.findByApprenantIdAndFormationId(SecurityUtils.getCurrentUserId(), id));
    }

    @GetMapping("/formations/{id}/apprenants/progression")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<List<Progression>> getStudentsProgressionForFormation(@PathVariable UUID id) {
        return ResponseEntity.ok(progressionRepository.findAllByFormationId(id));
    }
}
