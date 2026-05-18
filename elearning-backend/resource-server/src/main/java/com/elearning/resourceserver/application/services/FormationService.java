package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.*;
import com.elearning.resourceserver.domain.events.InscriptionCreatedEvent;
import com.elearning.resourceserver.domain.dto.FormationRequestDto;
import com.elearning.resourceserver.domain.dto.FormationResponseDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final OrganisationRepository organisationRepository;
    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Value("${minio.bucket-public}")
    private String bucketPublic;

    public Page<FormationResponseDto> searchFormations(String level, String language, String search, Pageable pageable) {
        return formationRepository.findByFilters(level, language, search, pageable)
                .map(this::mapToResponseDto);
    }

    public FormationResponseDto getFormationDetails(UUID id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        return mapToResponseDto(formation);
    }

    /**
     * UC-06: Création formation complète
     */
    public FormationResponseDto createFormation(String dataJson, MultipartFile thumbnail, UUID currentUserId) {
        try {
            FormationRequestDto dto = objectMapper.readValue(dataJson, FormationRequestDto.class);

            // RB-05: Verify formateur has an organisation
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

            UUID organisationId = currentUser.getOrganisationId();
            if (organisationId == null) {
                // Rattachement automatique à organisation isDefault=true
                Organisation defaultOrg = organisationRepository.findByIsDefaultTrue()
                        .orElseThrow(() -> new ValidationException("Aucune organisation par défaut configurée"));
                currentUser.setOrganisationId(defaultOrg.getId());
                userRepository.save(currentUser);
                organisationId = defaultOrg.getId();
            }

            // RB-10: Verify organisation is ACTIVE
            Organisation org = organisationRepository.findById(organisationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));
            if (org.getStatus() != OrganisationStatus.ACTIVE) {
                throw new AccessDeniedException("L'organisation n'est pas active. Impossible de créer une formation.");
            }

            String coverImageKey = null;
            if (thumbnail != null && !thumbnail.isEmpty()) {
                coverImageKey = minioService.uploadFile(thumbnail, bucketPublic);
            }

            Formation formation = new Formation();
            formation.setTitle(dto.getTitle());
            formation.setDescription(dto.getDescription());
            formation.setLevel(dto.getLevel() != null ? FormationLevel.valueOf(dto.getLevel()) : null);
            formation.setLanguage(dto.getLanguage());
            formation.setPrice(dto.getPrice() != null ? BigDecimal.valueOf(dto.getPrice()) : BigDecimal.ZERO);
            formation.setCurrency("MAD");
            formation.setOrganisationId(organisationId);
            formation.setFormateurId(currentUserId);
            formation.setCoverImageKey(coverImageKey);
            
            // Generate slug
            formation.setSlug(slugify(dto.getTitle()) + "-" + UUID.randomUUID().toString().substring(0, 8));
            // Always BROUILLON at creation
            formation.setStatus(FormationStatus.BROUILLON);

            Formation saved = formationRepository.save(formation);
            return mapToResponseDto(saved);
        } catch (AccessDeniedException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Erreur lors de la création de la formation : " + e.getMessage());
        }
    }

    public void updateFormation(UUID id, FormationRequestDto dto, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        if (!formation.getFormateurId().equals(requesterId) && !formation.getOrganisationId().equals(requesterId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette formation");
        }

        if (dto.getTitle() != null) formation.setTitle(dto.getTitle());
        if (dto.getDescription() != null) formation.setDescription(dto.getDescription());
        if (dto.getLevel() != null) formation.setLevel(FormationLevel.valueOf(dto.getLevel()));
        if (dto.getLanguage() != null) formation.setLanguage(dto.getLanguage());
        if (dto.getPrice() != null) formation.setPrice(java.math.BigDecimal.valueOf(dto.getPrice()));

        formationRepository.save(formation);
    }

    /**
     * UC-06 / RB-09: Publication formation complète
     */
    public void publishFormation(UUID id, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        if (!formation.getFormateurId().equals(requesterId) && !formation.getOrganisationId().equals(requesterId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à publier cette formation");
        }

        // RB-09: Vérifications complètes
        var courses = courseRepository.findByFormationIdOrderByOrderIndex(formation.getId());
        if (courses.isEmpty()) {
            throw new ValidationException("Ajoutez au moins un cours avant de publier");
        }

        for (Course course : courses) {
            var seances = seanceRepository.findByCoursId(course.getId());
            if (seances.isEmpty()) {
                throw new ValidationException("Le cours '" + course.getTitle() + "' n'a pas de séances");
            }

            var quiz = quizRepository.findByCourseId(course.getId());
            if (quiz.isEmpty() || !quiz.get().getIsPublished()) {
                throw new ValidationException("Le cours '" + course.getTitle() + "' n'a pas de quiz publié");
            }
        }

        formation.setStatus(FormationStatus.PUBLIEE);
        formation.setPublishedAt(LocalDateTime.now());
        formationRepository.save(formation);
    }

    /**
     * UC-01: Inscription à une formation (logique complète)
     */
    public Object enrollUser(UUID userId, UUID formationId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        // Step 1: Verify formation.status = PUBLIEE
        if (formation.getStatus() != FormationStatus.PUBLIEE) {
            throw new ResourceNotFoundException("Formation non disponible");
        }

        // Step 2: Verify organisation.status = ACTIVE
        Organisation org = organisationRepository.findById(formation.getOrganisationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));
        if (org.getStatus() != OrganisationStatus.ACTIVE) {
            throw new AccessDeniedException("L'organisation de cette formation n'est pas active");
        }

        // Step 3: Verify not already enrolled
        if (inscriptionRepository.existsByApprenantIdAndFormationId(userId, formationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà inscrit à cette formation");
        }

        // Step 4: Verify maxStudents not reached
        if (formation.getMaxStudents() != null && formation.getMaxStudents() > 0) {
            long currentCount = inscriptionRepository.countByFormationId(formationId);
            if (currentCount >= formation.getMaxStudents()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nombre maximum d'étudiants est atteint");
            }
        }

        // Step 6-7: Handle price
        if (formation.getPrice() != null && formation.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("paymentRequired", true);
            response.put("amount", formation.getPrice());
            response.put("currency", formation.getCurrency());
            response.put("formationId", formationId);
            return response;
        }

        // Step 6: Free formation → create Inscription
        Inscription inscription = new Inscription();
        inscription.setApprenantId(userId);
        inscription.setFormationId(formationId);
        inscription.setStatus(InscriptionStatus.EN_COURS);
        inscriptionRepository.save(inscription);

        // Step 10: Publish InscriptionCreatedEvent → handler creates progressions
        eventPublisher.publishEvent(new InscriptionCreatedEvent(this, userId, formationId));

        // Step 11: Notification
        notificationService.sendToUser(userId,
                "Bienvenue !",
                "Bienvenue dans " + formation.getTitle(),
                Map.of("type", "ENROLLMENT", "formationId", formationId.toString()));

        Map<String, Object> response = new HashMap<>();
        response.put("inscriptionId", inscription.getId());
        response.put("formationId", formationId);
        response.put("status", "EN_COURS");
        response.put("enrolledAt", inscription.getEnrolledAt());
        return response;
    }

    private FormationResponseDto mapToResponseDto(Formation formation) {
        FormationResponseDto dto = new FormationResponseDto();
        dto.setId(formation.getId());
        dto.setTitle(formation.getTitle());
        dto.setSlug(formation.getSlug());
        dto.setDescription(formation.getDescription());
        dto.setLevel(formation.getLevel() != null ? formation.getLevel().name() : null);
        dto.setLanguage(formation.getLanguage());
        dto.setPrice(formation.getPrice() != null ? formation.getPrice().doubleValue() : null);
        dto.setStatus(formation.getStatus().name());
        dto.setOrganisationId(formation.getOrganisationId());
        dto.setFormateurId(formation.getFormateurId());
        dto.setCoverImageKey(formation.getCoverImageKey());
        return dto;
    }

    private String slugify(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[àâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[ïî]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
