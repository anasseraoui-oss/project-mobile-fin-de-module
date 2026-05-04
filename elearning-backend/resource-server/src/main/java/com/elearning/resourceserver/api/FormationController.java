package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.FormationService;
import com.elearning.resourceserver.domain.dto.FormationRequestDto;
import com.elearning.resourceserver.domain.dto.FormationResponseDto;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;
    
    @GetMapping
    public ResponseEntity<Page<FormationResponseDto>> getFormations(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(formationService.searchFormations(level, language, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormationResponseDto> getFormation(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getFormationDetails(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANISATION')")
    public ResponseEntity<FormationResponseDto> createFormation(
            @RequestPart("data") String formationDataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        UUID organisationId = SecurityUtils.getCurrentOrganisationId();
        if (organisationId == null) {
            organisationId = SecurityUtils.getCurrentUserId(); // Fallback for testing if missing in claims
        }
        return ResponseEntity.ok(formationService.createFormation(formationDataJson, thumbnail, organisationId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISATION')")
    public ResponseEntity<Void> updateFormation(@PathVariable UUID id, @RequestBody FormationRequestDto formationDto) {
        UUID requesterId = SecurityUtils.getCurrentOrganisationId();
        if (requesterId == null) requesterId = SecurityUtils.getCurrentUserId();
        formationService.updateFormation(id, formationDto, requesterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANISATION')")
    public ResponseEntity<Void> publishFormation(@PathVariable UUID id) {
        UUID requesterId = SecurityUtils.getCurrentOrganisationId();
        if (requesterId == null) requesterId = SecurityUtils.getCurrentUserId();
        formationService.publishFormation(id, requesterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> enrollInFormation(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.enrollUser(SecurityUtils.getCurrentUserId(), id));
    }
}
