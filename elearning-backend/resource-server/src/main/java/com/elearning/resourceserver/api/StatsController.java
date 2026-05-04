package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.dto.AdminStatsDto;
import com.elearning.resourceserver.domain.dto.OrganisationStatsDto;
import com.elearning.resourceserver.domain.dto.TrainerStatsDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.repository.StatsRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StatsController {

    private final StatsRepository statsRepository;

    @GetMapping("/organisations/{id}/stats")
    @PreAuthorize("hasAnyRole('ORGANISATION', 'SUPER_ADMIN')")
    public ResponseEntity<OrganisationStatsDto> getOrganisationStats(@PathVariable UUID id) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            UUID userOrgId = SecurityUtils.getCurrentOrganisationId();
            if (userOrgId == null || !userOrgId.equals(id)) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à voir ces statistiques.");
            }
        }
        return ResponseEntity.ok(statsRepository.getOrganisationStats(id));
    }

    @GetMapping("/formateurs/me/stats")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<TrainerStatsDto> getTrainerStats() {
        return ResponseEntity.ok(statsRepository.getTrainerStats(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminStatsDto> getAdminStats() {
        return ResponseEntity.ok(statsRepository.getAdminStats());
    }
}
