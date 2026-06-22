package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.FormationService;
import com.elearning.resourceserver.domain.dto.CategoryDto;
import com.elearning.resourceserver.domain.dto.FormationRequestDto;
import com.elearning.resourceserver.domain.dto.FormationResponseDto;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<Page<FormationResponseDto>> getFormations(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(formationService.searchFormations(level, language, search, categoryId, pageable));
    }

    @GetMapping("/enrolled")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<List<FormationResponseDto>> getMyEnrolledFormations() {
        return ResponseEntity.ok(formationService.getEnrolledFormations(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getFormationCategories() {
        return ResponseEntity.ok(formationService.getCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormationResponseDto> getFormation(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getFormationDetails(id));
    }

    @GetMapping("/{id}/cover-url")
    public ResponseEntity<Map<String, String>> getFormationCoverUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.getFormationCoverUrl(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<FormationResponseDto> createFormation(
            @RequestPart("data") String formationDataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        return ResponseEntity.ok(formationService.createFormation(formationDataJson, thumbnail, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> updateFormation(@PathVariable UUID id, @RequestBody FormationRequestDto formationDto) {
        formationService.updateFormation(id, formationDto, resolveOwnerScopeId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cover")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<FormationResponseDto> updateFormationCover(
            @PathVariable UUID id,
            @RequestPart("thumbnail") MultipartFile thumbnail) {
        return ResponseEntity.ok(formationService.updateCoverImage(id, thumbnail, resolveOwnerScopeId()));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> publishFormation(@PathVariable UUID id) {
        formationService.publishFormation(id, resolveOwnerScopeId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> archiveFormation(@PathVariable UUID id) {
        formationService.archiveFormation(id, resolveOwnerScopeId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> deleteFormation(@PathVariable UUID id) {
        formationService.deleteFormation(id, resolveOwnerScopeId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> enrollInFormation(@PathVariable UUID id) {
        return ResponseEntity.ok(formationService.enrollUser(SecurityUtils.getCurrentUserId(), id));
    }

    private UUID resolveOwnerScopeId() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!"ROLE_ADMIN_ORG".equals(SecurityUtils.getCurrentUserRole())) {
            return currentUserId;
        }

        UUID organisationId = SecurityUtils.getCurrentOrganisationId();
        if (organisationId != null) {
            return organisationId;
        }

        return userRepository.findById(currentUserId)
                .or(() -> {
                    String email = SecurityUtils.getCurrentUserEmail();
                    return email == null ? java.util.Optional.empty() : userRepository.findByEmail(email);
                })
                .map(user -> user.getOrganisationId() != null ? user.getOrganisationId() : currentUserId)
                .orElse(currentUserId);
    }
}
