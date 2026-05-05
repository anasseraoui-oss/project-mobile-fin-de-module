package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Organisation;
import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.enums.OrganisationStatus;
import com.elearning.resourceserver.domain.enums.Role;
import com.elearning.resourceserver.domain.events.OrganisationValidatedEvent;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.repository.FormationRepository;
import com.elearning.resourceserver.repository.OrganisationRepository;
import com.elearning.resourceserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Register a new organisation (status=PENDING)
     */
    public Organisation registerOrganisation(String name, String description, String sector, String website, UUID ownerId) {
        String slug = slugify(name) + "-" + UUID.randomUUID().toString().substring(0, 8);

        Organisation org = new Organisation();
        org.setName(name);
        org.setSlug(slug);
        org.setDescription(description);
        org.setSector(sector);
        org.setWebsite(website);
        org.setStatus(OrganisationStatus.PENDING);

        Organisation saved = organisationRepository.save(org);

        // Link owner to organisation
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner != null) {
            owner.setOrganisationId(saved.getId());
            owner.setRole(Role.ADMIN_ORG);
            userRepository.save(owner);
        }

        // Notify super admins
        List<User> admins = userRepository.findByOrganisationIdAndRole(null, Role.SUPER_ADMIN);
        for (User admin : admins) {
            notificationService.sendToUser(admin.getId(),
                    "Nouvelle organisation",
                    "L'organisation '" + name + "' est en attente de validation",
                    Map.of("type", "ORG_PENDING", "orgId", saved.getId().toString()));
        }

        return saved;
    }

    /**
     * Validate organisation (SUPER_ADMIN)
     */
    public void validateOrganisation(UUID orgId, UUID validatedBy) {
        Organisation org = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        org.setStatus(OrganisationStatus.ACTIVE);
        org.setValidatedAt(LocalDateTime.now());
        org.setValidatedBy(validatedBy);
        organisationRepository.save(org);

        eventPublisher.publishEvent(new OrganisationValidatedEvent(this, orgId, validatedBy));
    }

    /**
     * Suspend organisation (SUPER_ADMIN)
     */
    public void suspendOrganisation(UUID orgId) {
        Organisation org = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        org.setStatus(OrganisationStatus.SUSPENDED);
        organisationRepository.save(org);
    }

    /**
     * Get public profile
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPublicProfile(UUID orgId) {
        Organisation org = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        return Map.of(
                "id", org.getId(),
                "name", org.getName(),
                "slug", org.getSlug(),
                "logoKey", org.getLogoKey() != null ? org.getLogoKey() : "",
                "description", org.getDescription() != null ? org.getDescription() : "",
                "sector", org.getSector() != null ? org.getSector() : ""
        );
    }

    /**
     * UC-07: Invite formateur to organisation (ADMIN_ORG)
     */
    public void inviteFormateur(UUID orgId, String email, UUID currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (currentUser.getOrganisationId() == null || !currentUser.getOrganisationId().equals(orgId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à gérer cette organisation");
        }

        Organisation org = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        if (org.getStatus() != OrganisationStatus.ACTIVE) {
            throw new AccessDeniedException("L'organisation n'est pas active");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec cet email"));

        if (user.getRole() != Role.FORMATEUR) {
            throw new ValidationException("L'utilisateur n'est pas un formateur");
        }

        if (user.getOrganisationId() != null && !user.getOrganisationId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce formateur appartient déjà à une organisation");
        }

        user.setOrganisationId(orgId);
        userRepository.save(user);

        notificationService.sendToUser(user.getId(),
                "Invitation organisation",
                "Vous avez été rattaché à " + org.getName(),
                Map.of("type", "ORG_INVITATION", "orgId", orgId.toString()));
    }

    /**
     * Remove formateur from organisation (ADMIN_ORG)
     */
    public void removeFormateur(UUID orgId, UUID formateurId, UUID currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!currentUser.getOrganisationId().equals(orgId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à gérer cette organisation");
        }

        User formateur = userRepository.findById(formateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Formateur non trouvé"));

        // RB-05: Reattach to default org
        Organisation defaultOrg = organisationRepository.findByIsDefaultTrue().orElse(null);
        formateur.setOrganisationId(defaultOrg != null ? defaultOrg.getId() : null);
        userRepository.save(formateur);
    }

    /**
     * List formateurs of an organisation
     */
    @Transactional(readOnly = true)
    public List<User> listFormateurs(UUID orgId) {
        return userRepository.findByOrganisationIdAndRole(orgId, Role.FORMATEUR);
    }

    private String slugify(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[àâä]", "a").replaceAll("[éèêë]", "e")
                .replaceAll("[ïî]", "i").replaceAll("[ôö]", "o")
                .replaceAll("[ùûü]", "u").replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "").replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
