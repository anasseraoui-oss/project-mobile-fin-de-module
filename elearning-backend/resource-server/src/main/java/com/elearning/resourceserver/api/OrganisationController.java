package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.OrganisationService;
import com.elearning.resourceserver.domain.Organisation;
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
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping("/organisations/register")
    public ResponseEntity<Organisation> registerOrganisation(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");
        String sector = payload.get("sector");
        String website = payload.get("website");
        UUID ownerId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(organisationService.registerOrganisation(name, description, sector, website, ownerId));
    }

    @GetMapping("/organisations/{id}/public")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(organisationService.getPublicProfile(id));
    }

    @GetMapping("/organisations/{id}/formateurs")
    @PreAuthorize("hasAnyRole('ADMIN_ORG', 'SUPER_ADMIN')")
    public ResponseEntity<?> listFormateurs(@PathVariable UUID id) {
        return ResponseEntity.ok(organisationService.listFormateurs(id));
    }

    @PostMapping("/organisations/{id}/formateurs/invite")
    @PreAuthorize("hasRole('ADMIN_ORG')")
    public ResponseEntity<?> inviteFormateur(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        organisationService.inviteFormateur(id, email, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/organisations/{id}/formateurs/{formateurId}")
    @PreAuthorize("hasRole('ADMIN_ORG')")
    public ResponseEntity<?> removeFormateur(@PathVariable UUID id, @PathVariable UUID formateurId) {
        organisationService.removeFormateur(id, formateurId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }
}
