package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.*;
import com.elearning.resourceserver.domain.events.InscriptionCreatedEvent;
import com.elearning.resourceserver.domain.dto.CategoryDto;
import com.elearning.resourceserver.domain.dto.EnrollmentResponseDto;
import com.elearning.resourceserver.domain.dto.FormationRequestDto;
import com.elearning.resourceserver.domain.dto.FormationResponseDto;
import com.elearning.resourceserver.domain.dto.InstructorDashboardDto;
import com.elearning.resourceserver.domain.dto.InstructorFormationSummaryDto;
import com.elearning.resourceserver.domain.dto.InstructorProfileDto;
import com.elearning.resourceserver.domain.dto.InstructorStatsDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ProgressionRepository progressionRepository;
    private final OrganisationRepository organisationRepository;
    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final QuizRepository quizRepository;
    private final PedagogicalResourceRepository pedagogicalResourceRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final EntityManager entityManager;

    @Value("${minio.bucket-public}")
    private String bucketPublic;

    @Value("${minio.bucket-media:elearning-media}")
    private String bucketMedia;

    public Page<FormationResponseDto> searchFormations(String level, String language, String search, String categoryId, Pageable pageable) {
        Page<Formation> formations = formationRepository.findByFilters(
                parseFormationLevel(level), language, normalizeSearch(search), normalizeBlank(categoryId), pageable);
        Map<UUID, String> organisationNames = resolveOrganisationNames(formations.getContent());
        return formations.map(formation -> mapToResponseDto(formation, null, organisationNames));
    }

    public List<FormationResponseDto> getEnrolledFormations(UUID userId) {
        return inscriptionRepository.findByApprenantId(userId)
                .stream()
                .filter(inscription -> inscription.getFormation() != null)
                .map(inscription -> mapToResponseDto(inscription.getFormation(), userId))
                .toList();
    }

    public InstructorDashboardDto getInstructorDashboard(UUID userId, UUID organisationId, String role) {
        User instructor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        List<Formation> formations = findInstructorOwnedFormations(userId, organisationId, role);

        InstructorProfileDto profile = new InstructorProfileDto();
        profile.setId(instructor.getId());
        profile.setFullName(instructor.getFullName());
        profile.setEmail(instructor.getEmail());
        profile.setAvatarUrl(generateMediaUrl(instructor.getAvatarKey(), 60));
        profile.setCertificationStatus("CERTIFIED");
        profile.setLevelLabel("Formateur Expert");
        profile.setOrganisationName(resolveOrganisationName(instructor.getOrganisationId()));

        InstructorStatsDto stats = new InstructorStatsDto();
        stats.setActiveFormations((int) formations.stream()
                .filter(formation -> formation.getStatus() == FormationStatus.PUBLIEE)
                .count());
        stats.setTotalLearners(formations.stream()
                .mapToInt(formation -> (int) inscriptionRepository.countByFormationId(formation.getId()))
                .sum());
        stats.setAverageCompletionPercent(calculateAverageCompletionPercent(formations));
        stats.setMonthlyRevenue(0.0);
        stats.setMonthlyRevenueCurrency("MAD");
        stats.setPendingActions(0);

        return new InstructorDashboardDto(profile, stats);
    }

    public Page<InstructorFormationSummaryDto> getInstructorFormations(
            UUID userId,
            UUID organisationId,
            String role,
            String status,
            Pageable pageable) {
        List<InstructorFormationSummaryDto> summaries = findInstructorOwnedFormations(userId, organisationId, role)
                .stream()
                .filter(formation -> status == null || status.isBlank() || formation.getStatus().name().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(Formation::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapToInstructorSummaryDto)
                .toList();

        int start = (int) Math.min(pageable.getOffset(), summaries.size());
        int end = Math.min(start + pageable.getPageSize(), summaries.size());
        return new PageImpl<>(summaries.subList(start, end), pageable, summaries.size());
    }

    public List<CategoryDto> getCategories() {
        return List.of(
                category("backend", "Backend", "storage"),
                category("devops", "DevOps", "deployed_code"),
                category("mobile", "Mobile", "phone_android"),
                category("frontend", "Frontend", "dashboard"),
                category("cloud", "Cloud", "cloud"),
                category("data", "Data", "analytics"),
                category("security", "Security", "shield"),
                category("ai", "AI", "smart_toy"),
                category("design", "Design", "palette"),
                category("business", "Business", "business_center")
        );
    }

    public FormationResponseDto getFormationDetails(UUID id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        return mapToResponseDto(formation);
    }

    public Map<String, String> getFormationCoverUrl(UUID id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvÃ©e"));

        String objectKey = formation.getCoverImageKey();
        if (objectKey == null || objectKey.isBlank()) {
            throw new ResourceNotFoundException("Image de couverture non trouvÃ©e");
        }

        try {
            boolean inPublicBucket = minioService.objectExists(bucketPublic, objectKey);
            boolean inMediaBucket = !inPublicBucket && minioService.objectExists(bucketMedia, objectKey);
            if (!inPublicBucket && !inMediaBucket) {
                throw new ResourceNotFoundException("Image de couverture non disponible dans MinIO");
            }

            String bucket = inPublicBucket ? bucketPublic : bucketMedia;
            String url = minioService.generatePresignedUrl(bucket, objectKey, 60);
            return Map.of(
                    "url", url,
                    "cover_url", url,
                    "object_key", objectKey,
                    "bucket", bucket,
                    "expiresInMinutes", "60"
            );
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Erreur lors de la gÃ©nÃ©ration de l'URL de couverture : " + e.getMessage());
        }
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
            formation.setCategoryId(normalizeBlank(dto.getCategoryId()));
            formation.setCertified(Boolean.TRUE.equals(dto.getCertified()));
            formation.setPrerequisitesText(serializePrerequisites(dto.getPrerequisites()));
            
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

        if (!Objects.equals(formation.getFormateurId(), requesterId) && !Objects.equals(formation.getOrganisationId(), requesterId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette formation");
        }

        if (dto.getTitle() != null) formation.setTitle(dto.getTitle());
        if (dto.getDescription() != null) formation.setDescription(dto.getDescription());
        if (dto.getLevel() != null) formation.setLevel(FormationLevel.valueOf(dto.getLevel()));
        if (dto.getLanguage() != null) formation.setLanguage(dto.getLanguage());
        if (dto.getPrice() != null) formation.setPrice(java.math.BigDecimal.valueOf(dto.getPrice()));
        if (dto.getCategoryId() != null) formation.setCategoryId(normalizeBlank(dto.getCategoryId()));
        if (dto.getCertified() != null) formation.setCertified(dto.getCertified());
        if (dto.getPrerequisites() != null) formation.setPrerequisitesText(serializePrerequisites(dto.getPrerequisites()));

        formationRepository.save(formation);
    }

    public FormationResponseDto updateCoverImage(UUID id, MultipartFile thumbnail, UUID requesterId) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new ValidationException("Image de couverture obligatoire");
        }
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManageFormation(formation, requesterId, "modifier");

        String previousKey = formation.getCoverImageKey();
        try {
            String coverImageKey = minioService.uploadFile(thumbnail, bucketPublic);
            formation.setCoverImageKey(coverImageKey);
            Formation saved = formationRepository.save(formation);
            deleteObjectBestEffort(previousKey, bucketPublic);
            return mapToResponseDto(saved);
        } catch (Exception e) {
            throw new ValidationException("Erreur upload couverture : " + e.getMessage());
        }
    }

    /**
     * UC-06 / RB-09: Publication formation complète
     */
    public void publishFormation(UUID id, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        if (!Objects.equals(formation.getFormateurId(), requesterId) && !Objects.equals(formation.getOrganisationId(), requesterId)) {
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

            // Quiz publication is optional for the current mobile publication flow.
            // The backend minimum contract matches the frontend checklist.
        }

        formation.setStatus(FormationStatus.PUBLIEE);
        formation.setPublishedAt(LocalDateTime.now());
        formationRepository.save(formation);
    }

    public void archiveFormation(UUID id, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManageFormation(formation, requesterId, "archiver");

        formation.setStatus(FormationStatus.ARCHIVEE);
        formationRepository.save(formation);
    }

    public void deleteFormation(UUID id, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManageFormation(formation, requesterId, "supprimer");

        deleteFormationMedia(formation);
        executeFormationDelete("DELETE FROM forum_posts WHERE seance_id IN (SELECT s.id FROM seances s JOIN courses c ON s.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM progress WHERE seance_id IN (SELECT s.id FROM seances s JOIN courses c ON s.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM attendances WHERE seance_id IN (SELECT s.id FROM seances s JOIN courses c ON s.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM presences WHERE seance_id IN (SELECT s.id FROM seances s JOIN courses c ON s.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM quiz_attempts WHERE quiz_id IN (SELECT q.id FROM quizzes q JOIN courses c ON q.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM tentatives_quiz WHERE quiz_id IN (SELECT q.id FROM quizzes q JOIN courses c ON q.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM quiz_reponses WHERE question_id IN (SELECT qq.id FROM quiz_questions qq JOIN quizzes q ON qq.quiz_id = q.id JOIN courses c ON q.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM quiz_questions WHERE quiz_id IN (SELECT q.id FROM quizzes q JOIN courses c ON q.course_id = c.id WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM quizzes WHERE course_id IN (SELECT c.id FROM courses c WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM pedagogical_resources WHERE formation_id = :formationId", id);
        executeFormationDelete("DELETE FROM progressions WHERE formation_id = :formationId", id);
        executeFormationDelete("DELETE FROM certificats WHERE formation_id = :formationId", id);
        executeFormationDelete("DELETE FROM inscriptions WHERE formation_id = :formationId", id);
        executeFormationDelete("DELETE FROM seances WHERE course_id IN (SELECT c.id FROM courses c WHERE c.formation_id = :formationId)", id);
        executeFormationDelete("DELETE FROM courses WHERE formation_id = :formationId", id);
        executeFormationDelete("DELETE FROM formations WHERE id = :formationId", id);
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

        EnrollmentResponseDto response = new EnrollmentResponseDto();
        response.setInscriptionId(inscription.getId());
        response.setFormationId(formationId);
        response.setStatus(inscription.getStatus().name());
        response.setEnrolledAt(inscription.getEnrolledAt());
        response.setPaymentRequired(false);
        return response;
    }

    private FormationResponseDto mapToResponseDto(Formation formation) {
        return mapToResponseDto(formation, null, Map.of());
    }

    private FormationResponseDto mapToResponseDto(Formation formation, UUID userId) {
        return mapToResponseDto(formation, userId, Map.of());
    }

    private FormationResponseDto mapToResponseDto(Formation formation, UUID userId, Map<UUID, String> organisationNames) {
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
        dto.setOrganisationName(resolveOrganisationName(formation.getOrganisationId(), organisationNames));
        dto.setFormateurId(formation.getFormateurId());
        dto.setCoverImageKey(formation.getCoverImageKey());
        dto.setThumbnailKey(formation.getCoverImageKey());
        dto.setCoverImageUrl(generateMediaUrl(formation.getCoverImageKey(), 60));
        dto.setThumbnailUrl(dto.getCoverImageUrl());
        dto.setTotalDuration(formation.getTotalDuration());
        dto.setDurationHours(toDurationHours(formation.getTotalDuration()));
        dto.setIsPublished(formation.isPublished());
        int coursesCount = (int) courseRepository.countByFormationId(formation.getId());
        dto.setCoursesCount(coursesCount);
        dto.setEnrolledCount((int) inscriptionRepository.countByFormationId(formation.getId()));
        dto.setRating(0f);
        if (userId != null) {
            boolean enrolled = inscriptionRepository.existsByApprenantIdAndFormationId(userId, formation.getId());
            dto.setIsEnrolled(enrolled);
            dto.setProgressPercent(enrolled ? calculateFormationProgressPercent(userId, formation.getId(), coursesCount) : 0);
        } else {
            dto.setIsEnrolled(false);
        dto.setProgressPercent(0);
        }
        dto.setCategoryId(formation.getCategoryId());
        dto.setPrerequisites(deserializePrerequisites(formation.getPrerequisitesText()));
        dto.setCertified(Boolean.TRUE.equals(formation.getCertified()));
        return dto;
    }

    private Map<UUID, String> resolveOrganisationNames(List<Formation> formations) {
        Set<UUID> organisationIds = formations.stream()
                .map(Formation::getOrganisationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (organisationIds.isEmpty()) {
            return Map.of();
        }
        return organisationRepository.findAllById(organisationIds)
                .stream()
                .collect(Collectors.toMap(Organisation::getId, Organisation::getName, (first, second) -> first));
    }

    private String resolveOrganisationName(UUID organisationId, Map<UUID, String> organisationNames) {
        if (organisationId == null) {
            return null;
        }
        return organisationNames.getOrDefault(organisationId, resolveOrganisationName(organisationId));
    }

    private List<Formation> findInstructorOwnedFormations(UUID userId, UUID organisationId, String role) {
        if ("ROLE_ADMIN_ORG".equals(role) && organisationId != null) {
            return formationRepository.findByOrganisationId(organisationId);
        }
        return formationRepository.findByFormateurId(userId);
    }

    private InstructorFormationSummaryDto mapToInstructorSummaryDto(Formation formation) {
        InstructorFormationSummaryDto dto = new InstructorFormationSummaryDto();
        dto.setId(formation.getId());
        dto.setTitle(formation.getTitle());
        dto.setDescription(formation.getDescription());
        dto.setStatus(formation.getStatus() != null ? formation.getStatus().name() : null);
        dto.setCoverImageUrl(generateMediaUrl(formation.getCoverImageKey(), 60));
        dto.setCoursesCount((int) courseRepository.countByFormationId(formation.getId()));
        dto.setSeancesCount((int) seanceRepository.countByFormationId(formation.getId()));
        dto.setEnrolledCount((int) inscriptionRepository.countByFormationId(formation.getId()));
        dto.setTotalDuration(formation.getTotalDuration());
        dto.setUpdatedAt(formation.getUpdatedAt());
        return dto;
    }

    private int calculateAverageCompletionPercent(List<Formation> formations) {
        if (formations.isEmpty()) {
            return 0;
        }
        double average = formations.stream()
                .mapToInt(formation -> {
                    int coursesCount = (int) courseRepository.countByFormationId(formation.getId());
                    long learnersCount = inscriptionRepository.countByFormationId(formation.getId());
                    if (coursesCount == 0 || learnersCount == 0) {
                        return 0;
                    }
                    long completedCourses = progressionRepository.countByFormationIdAndQuizStatus(formation.getId(), QuizStatus.VALIDE);
                    return (int) Math.min(100, Math.round(completedCourses * 100.0 / (coursesCount * learnersCount)));
                })
                .average()
                .orElse(0);
        return (int) Math.round(average);
    }

    private int calculateFormationProgressPercent(UUID userId, UUID formationId, int coursesCount) {
        if (coursesCount <= 0) return 0;
        long completedCourses = progressionRepository.countByFormationIdAndApprenantIdAndQuizStatus(
                formationId,
                userId,
                QuizStatus.VALIDE
        );
        return (int) Math.min(100, Math.round((completedCourses * 100.0) / coursesCount));
    }

    private String resolveOrganisationName(UUID organisationId) {
        if (organisationId == null) {
            return "Organisation inconnue";
        }
        return organisationRepository.findById(organisationId)
                .map(Organisation::getName)
                .orElse("Organisation inconnue");
    }

    private CategoryDto category(String id, String title, String icon) {
        long count = formationRepository.findByFilters(null, null, "", id, PageRequest.of(0, 1)).getTotalElements();
        return new CategoryDto(id, title, icon, (int) count);
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String serializePrerequisites(List<String> prerequisites) {
        try {
            return objectMapper.writeValueAsString(prerequisites == null ? List.of() : prerequisites);
        } catch (Exception e) {
            throw new ValidationException("Prerequis invalides");
        }
    }

    private List<String> deserializePrerequisites(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    value,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (Exception ignored) {
            return List.of(value);
        }
    }

    private String normalizeSearch(String value) {
        return value == null ? "" : value.trim();
    }

    private FormationLevel parseFormationLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return FormationLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Niveau de formation invalide : " + value);
        }
    }

    private void ensureCanManageFormation(Formation formation, UUID requesterId, String action) {
        if (!Objects.equals(formation.getFormateurId(), requesterId) && !Objects.equals(formation.getOrganisationId(), requesterId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à " + action + " cette formation");
        }
    }

    private void deleteFormationMedia(Formation formation) {
        deleteObjectBestEffort(formation.getCoverImageKey(), bucketPublic);
        pedagogicalResourceRepository.findByFormationIdOrderByCreatedAtAsc(formation.getId())
                .forEach(resource -> deleteObjectBestEffort(resource.getObjectKey(), resource.getBucketName()));
    }

    private void deleteObjectBestEffort(String objectKey, String bucketName) {
        if (objectKey == null || objectKey.isBlank() || bucketName == null || bucketName.isBlank()) {
            return;
        }
        try {
            minioService.deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            log.warn("Could not delete MinIO object {}/{}: {}", bucketName, objectKey, e.getMessage());
        }
    }

    private int executeFormationDelete(String sql, UUID formationId) {
        return entityManager.createNativeQuery(sql)
                .setParameter("formationId", formationId)
                .executeUpdate();
    }

    private String generateMediaUrl(String objectKey, int expiryMinutes) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        try {
            String bucket = minioService.objectExists(bucketPublic, objectKey) ? bucketPublic : bucketMedia;
            return minioService.generatePresignedUrl(bucket, objectKey, expiryMinutes);
        } catch (Exception e) {
            log.warn("Could not generate media URL for key {}: {}", objectKey, e.getMessage());
            return null;
        }
    }

    private Integer toDurationHours(Integer totalDurationMinutes) {
        if (totalDurationMinutes == null || totalDurationMinutes <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(totalDurationMinutes / 60.0));
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
