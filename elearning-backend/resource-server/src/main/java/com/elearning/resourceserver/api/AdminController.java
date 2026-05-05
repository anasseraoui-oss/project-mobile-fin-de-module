package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.OrganisationService;
import com.elearning.resourceserver.domain.dto.AdminStatsDto;
import com.elearning.resourceserver.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.elearning.resourceserver.util.SecurityUtils;


@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrganisationService organisationService;
    private final StatsRepository statsRepository;

    @PostMapping("/organisations/{id}/validate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> validateOrganisation(@PathVariable UUID id) {
        organisationService.validateOrganisation(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/organisations/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> suspendOrganisation(@PathVariable UUID id) {
        organisationService.suspendOrganisation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminStatsDto> getAdminStats() {
        return ResponseEntity.ok(statsRepository.getAdminStats());
    }
}
