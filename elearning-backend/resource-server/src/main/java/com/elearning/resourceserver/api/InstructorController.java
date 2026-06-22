package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.FormationService;
import com.elearning.resourceserver.domain.dto.InstructorDashboardDto;
import com.elearning.resourceserver.domain.dto.InstructorFormationSummaryDto;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/formateurs/me")
@RequiredArgsConstructor
public class InstructorController {

    private final FormationService formationService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<InstructorDashboardDto> getDashboard() {
        return ResponseEntity.ok(formationService.getInstructorDashboard(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentOrganisationId(),
                SecurityUtils.getCurrentUserRole()
        ));
    }

    @GetMapping("/formations")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Page<InstructorFormationSummaryDto>> getMyFormations(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(formationService.getInstructorFormations(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentOrganisationId(),
                SecurityUtils.getCurrentUserRole(),
                status,
                pageable
        ));
    }
}
