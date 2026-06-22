package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.SeanceStatus;
import com.elearning.resourceserver.domain.enums.SeanceType;
import com.elearning.resourceserver.domain.dto.CourseRequestDto;
import com.elearning.resourceserver.domain.dto.CourseResponseDto;
import com.elearning.resourceserver.domain.dto.ProgressResponseDto;
import com.elearning.resourceserver.domain.dto.ProgressUpdateRequest;
import com.elearning.resourceserver.domain.dto.SeanceRequestDto;
import com.elearning.resourceserver.domain.dto.SeanceResponseDto;
import com.elearning.resourceserver.domain.dto.SeanceTextContentRequestDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.*;
import com.elearning.resourceserver.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
public class CourseAndSeanceService {

    private final CourseRepository courseRepository;
    private final FormationRepository formationRepository;
    private final SeanceRepository seanceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ProgressionRepository progressionRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final PedagogicalResourceRepository pedagogicalResourceRepository;
    private final MinioService minioService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    private static final Set<String> ALLOWED_VIDEO_FORMATS = Set.of(
            "video/mp4", "video/webm", "video/quicktime"
    );
    private static final long MAX_VIDEO_SIZE = 2L * 1024 * 1024 * 1024; // 2GB
    private static final int STREAM_URL_EXPIRY_MINUTES = 240;
    private static final int STREAM_URL_REFRESH_AFTER_SECONDS = 1800;

    public List<CourseResponseDto> getCoursesByFormation(UUID formationId, UUID userId, String role) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvee"));
        ensureCanReadFormation(formation, userId, role);
        if ("ROLE_APPRENANT".equals(role) && !inscriptionRepository.existsByApprenantIdAndFormationId(userId, formationId)) {
            throw new AccessDeniedException("Inscription requise pour accÃ©der Ã  ces cours");
        }

        return courseRepository.findByFormationIdOrderByOrderIndex(formationId)
                .stream()
                .map(course -> {
                    CourseResponseDto dto = new CourseResponseDto();
                    dto.setId(course.getId());
                    dto.setTitle(course.getTitle());
                    dto.setDescription(course.getDescription());
                    dto.setOrderIndex(course.getOrderIndex());
                    dto.setFormationId(course.getFormationId());
                    dto.setStatus(course.getStatus().name());
                    dto.setPresenceThreshold(course.getPresenceThreshold());

                    // For apprenants: include unlock status
                    if ("ROLE_APPRENANT".equals(role)) {
                        Progression prog = progressionRepository
                                .findByApprenantIdAndCoursId(userId, course.getId())
                                .orElse(null);
                        if (prog != null) {
                            dto.setIsUnlocked(prog.getIsUnlocked());
                            dto.setPresenceRate(prog.getPresenceRate());
                            dto.setQuizStatus(prog.getQuizStatus().name());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<SeanceResponseDto> getSeancesByCourse(UUID courseId, UUID userId, String role) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvÃ©"));

        ensureCanReadFormation(course.getFormation(), userId, role);

        return seanceRepository.findByCoursIdOrderByOrderIndex(courseId)
                .stream()
                .map(seance -> mapSeanceToDto(seance, userId))
                .collect(Collectors.toList());
    }

    public SeanceResponseDto getSeance(UUID seanceId, UUID userId, String role) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));
        ensureCanReadFormation(seance.getCourse().getFormation(), userId, role);
        return mapSeanceToDto(seance, userId);
    }

    public CourseResponseDto createCourse(CourseRequestDto dto, UUID formateurId) {
        if (dto.getFormationId() == null) {
            throw new ValidationException("L'ID de la formation est obligatoire");
        }

        Formation formation = formationRepository.findById(dto.getFormationId())
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvÃ©e"));
        ensureCanManageFormation(formation, formateurId);

        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setFormation(formation);
        course.setOrderIndex(dto.getOrderIndex());
        if (dto.getPresenceThreshold() != null) course.setPresenceThreshold(dto.getPresenceThreshold());
        if (dto.getQuizPassScore() != null) course.setQuizPassScore(dto.getQuizPassScore());
        if (dto.getEstimatedDuration() != null) course.setEstimatedDuration(dto.getEstimatedDuration());

        Course saved = courseRepository.save(course);

        return mapCourseToDto(saved);
    }

    public CourseResponseDto updateCourse(UUID courseId, CourseRequestDto dto, UUID requesterId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvÃ©"));
        ensureCanManageFormation(course.getFormation(), requesterId);

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) course.setTitle(dto.getTitle());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getOrderIndex() != null) course.setOrderIndex(dto.getOrderIndex());
        if (dto.getPresenceThreshold() != null) course.setPresenceThreshold(dto.getPresenceThreshold());
        if (dto.getQuizPassScore() != null) course.setQuizPassScore(dto.getQuizPassScore());
        if (dto.getEstimatedDuration() != null) course.setEstimatedDuration(dto.getEstimatedDuration());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                course.setStatus(com.elearning.resourceserver.domain.enums.CoursStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Statut de module invalide");
            }
        }
        return mapCourseToDto(courseRepository.save(course));
    }

    public void deleteCourse(UUID courseId, UUID requesterId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvÃ©"));
        ensureCanManageFormation(course.getFormation(), requesterId);

        seanceRepository.findByCoursId(courseId).forEach(seance -> deleteSeanceInternal(seance));
        deleteCourseResources(courseId);
        executeDelete("DELETE FROM quiz_attempts WHERE quiz_id IN (SELECT id FROM quizzes WHERE course_id = :id)", courseId);
        executeDelete("DELETE FROM tentatives_quiz WHERE quiz_id IN (SELECT id FROM quizzes WHERE course_id = :id)", courseId);
        executeDelete("DELETE FROM quiz_reponses WHERE question_id IN (SELECT qq.id FROM quiz_questions qq JOIN quizzes q ON qq.quiz_id = q.id WHERE q.course_id = :id)", courseId);
        executeDelete("DELETE FROM quiz_questions WHERE quiz_id IN (SELECT id FROM quizzes WHERE course_id = :id)", courseId);
        executeDelete("DELETE FROM quizzes WHERE course_id = :id", courseId);
        executeDelete("DELETE FROM progressions WHERE cours_id = :id", courseId);
        courseRepository.delete(course);
    }

    public SeanceResponseDto createSeance(UUID courseId, String seanceDataJson, MultipartFile video, UUID formateurId) {
        try {
            SeanceRequestDto dto = objectMapper.readValue(seanceDataJson, SeanceRequestDto.class);

            SeanceType type = SeanceType.valueOf(dto.getType());
            if (type == SeanceType.LIVE && (dto.getMeetingLink() == null || dto.getMeetingLink().isEmpty())) {
                throw new ValidationException("Lien meeting obligatoire pour une sÃ©ance live");
            }

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvÃ©"));
            ensureCanManageFormation(course.getFormation(), formateurId);

            Seance seance = new Seance();
            seance.setTitle(dto.getTitle());
            seance.setDescription(dto.getDescription());
            seance.setType(type);
            seance.setScheduledAt(dto.getScheduledAt());
            seance.setMeetingLink(dto.getMeetingLink());
            seance.setDuration(dto.getDuration());
            seance.setOrderIndex(dto.getOrderIndex());
            seance.setCourse(course);
            seance.setFormateurId(formateurId);

            Seance saved = seanceRepository.save(seance);
            if (video != null && !video.isEmpty()) {
                uploadVideo(saved.getId(), video, formateurId);
                saved = seanceRepository.findById(saved.getId()).orElse(saved);
            }

            SeanceResponseDto res = new SeanceResponseDto();
            res.setId(saved.getId());
            res.setTitle(saved.getTitle());
            res.setCourseId(courseId);
            res.setType(saved.getType().name());
            res.setStatus(saved.getStatus().name());
            res.setScheduledAt(saved.getScheduledAt());
            res.setMeetingLink(saved.getMeetingLink());
            res.setOrderIndex(saved.getOrderIndex());
            res.setDurationSeconds(saved.getDuration());
            res.setVideoKey(saved.getVideoKey());
            return res;

        } catch (ValidationException | AccessDeniedException | ResourceNotFoundException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidationException("Erreur de crÃ©ation de sÃ©ance : " + e.getMessage());
        }
    }

    public SeanceResponseDto updateSeance(UUID seanceId, SeanceRequestDto dto, UUID requesterId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));
        ensureCanManageFormation(seance.getCourse().getFormation(), requesterId);

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) seance.setTitle(dto.getTitle());
        if (dto.getDescription() != null) seance.setDescription(dto.getDescription());
        if (dto.getType() != null && !dto.getType().isBlank()) {
            try {
                seance.setType(SeanceType.valueOf(dto.getType()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Type de sÃ©ance invalide");
            }
        }
        if (seance.getType() == SeanceType.LIVE && dto.getMeetingLink() != null && dto.getMeetingLink().isBlank()) {
            throw new ValidationException("Lien meeting obligatoire pour une sÃ©ance live");
        }
        if (dto.getDuration() != null) seance.setDuration(dto.getDuration());
        if (dto.getScheduledAt() != null) seance.setScheduledAt(dto.getScheduledAt());
        if (dto.getMeetingLink() != null) seance.setMeetingLink(dto.getMeetingLink());
        if (dto.getOrderIndex() != null) seance.setOrderIndex(dto.getOrderIndex());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                seance.setStatus(SeanceStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Statut de sÃ©ance invalide");
            }
        }
        return mapSeanceToDto(seanceRepository.save(seance), requesterId);
    }

    public void deleteSeance(UUID seanceId, UUID requesterId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));
        ensureCanManageFormation(seance.getCourse().getFormation(), requesterId);
        deleteSeanceInternal(seance);
    }

    /**
     * Start a live session (FORMATEUR)
     */
    public void startSeance(UUID seanceId, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Seance non trouvee"));

        ensureCanManageFormation(seance.getCourse().getFormation(), formateurId);

        if (seance.getType() != SeanceType.LIVE) {
            throw new ValidationException("Seule une seance LIVE peut etre demarree");
        }

        if (seance.getScheduledAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(seance.getScheduledAt().minusMinutes(30))
                    || now.isAfter(seance.getScheduledAt().plusMinutes(30))) {
                throw new ValidationException("La seance ne peut etre demarree que dans un intervalle de +/-30 minutes de l'heure prevue");
            }
        }

        seance.setStatus(SeanceStatus.EN_COURS);
        seanceRepository.save(seance);

        UUID formationId = seance.getCourse().getFormation().getId();
        notificationService.sendToFormationSubscribers(
                formationId,
                "Seance live demarree",
                "La seance '" + seance.getTitle() + "' vient de demarrer.",
                Map.of(
                        "type", "LIVE_REMINDER",
                        "seanceId", seanceId.toString(),
                        "formationId", formationId.toString(),
                        "deepLink", "player/" + seanceId
                ),
                "live-started:" + seanceId
        );
    }
    
    public ProgressResponseDto updateProgress(UUID seanceId, UUID userId, ProgressUpdateRequest request) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));

        ensureCanReadFormation(seance.getCourse().getFormation(), userId, SecurityUtils.getCurrentUserRole());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvÃ©"));

        int incomingSeconds = request.getWatchedSeconds() != null
                ? request.getWatchedSeconds()
                : request.getProgressSeconds() != null ? request.getProgressSeconds() : 0;
        incomingSeconds = Math.max(0, incomingSeconds);

        Progress progress = progressRepository.findByUserIdAndSeanceId(userId, seanceId)
                .orElseGet(() -> {
                    Progress created = new Progress();
                    created.setUser(user);
                    created.setSeance(seance);
                    return created;
                });

        int previousSeconds = progress.getWatchedSeconds() != null ? progress.getWatchedSeconds() : 0;
        int durationSeconds = seance.getDuration() != null ? seance.getDuration() : 0;
        int acceptedSeconds = Math.max(previousSeconds, incomingSeconds);
        if (durationSeconds > 0) {
            acceptedSeconds = Math.min(acceptedSeconds, durationSeconds);
        }

        boolean completedByRequest = Boolean.TRUE.equals(request.getCompleted());
        boolean completedByWatchTime = durationSeconds > 0 && acceptedSeconds >= Math.ceil(durationSeconds * 0.9);

        progress.setWatchedSeconds(acceptedSeconds);
        progress.setIsCompleted(Boolean.TRUE.equals(progress.getIsCompleted()) || completedByRequest || completedByWatchTime);
        progress.setLastWatchedAt(LocalDateTime.now());
        progressRepository.save(progress);

        int courseProgress = recalculateCourseProgress(userId, seance.getCourse().getId());

        ProgressResponseDto response = new ProgressResponseDto();
        response.setSeanceId(seanceId);
        response.setWatchedSeconds(progress.getWatchedSeconds());
        response.setCompleted(progress.getIsCompleted());
        response.setLastWatchedAt(progress.getLastWatchedAt());
        response.setCourseProgressPercent(courseProgress);
        return response;
    }

    /**
     * End a live session (FORMATEUR)
     */
    public void endSeance(UUID seanceId, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));

        ensureCanManageFormation(seance.getCourse().getFormation(), formateurId);

        seance.setStatus(SeanceStatus.TERMINEE);
        seanceRepository.save(seance);
    }

    /**
     * UC-03: Upload vidÃ©o post-sÃ©ance (RB-03)
     */
    public void uploadVideo(UUID seanceId, MultipartFile video, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));

        // Step 1: Verify ownership
        ensureCanManageFormation(seance.getCourse().getFormation(), formateurId);

        // Step 2: Validate format
        String contentType = video.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_FORMATS.contains(contentType)) {
            throw new ValidationException("Format non supportÃ©. Formats acceptÃ©s : mp4, webm, mov");
        }

        // Step 3: Validate size
        if (video.getSize() > MAX_VIDEO_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La vidÃ©o ne doit pas dÃ©passer 2 Go");
        }

        try {
            // Step 5-7: Upload and move
            String tempKey = minioService.uploadFile(video, "elearning-uploads");
            String finalKey = "seances/" + seanceId + "/video" + getExtension(video.getOriginalFilename());
            minioService.moveObject("elearning-uploads", tempKey, "elearning-media", finalKey);

            // Step 8-9: Update seance
            String previousKey = seance.getVideoKey();
            seance.setVideoKey(finalKey);
            seance.setStatus(SeanceStatus.CONTENU_DISPONIBLE);
            seanceRepository.save(seance);
            deleteObjectBestEffort("elearning-media", previousKey);

            // Step 10: Notification
            UUID formationId = seance.getCourse().getFormation().getId();
            notificationService.sendToFormationSubscribers(formationId,
                    "Nouvel enregistrement disponible",
                    "Un nouvel enregistrement est disponible pour la seance '" + seance.getTitle() + "'",
                    Map.of("type", "VIDEO_AVAILABLE", "seanceId", seanceId.toString(), "formationId", formationId.toString(), "deepLink", "player/" + seanceId),
                    "video-available:" + seanceId);

        } catch (Exception e) {
            throw new ValidationException("Erreur lors de l'upload vidÃ©o : " + e.getMessage());
        }
    }

    public void updateSeanceTextContent(UUID seanceId, SeanceTextContentRequestDto request, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));

        ensureCanManageFormation(seance.getCourse().getFormation(), formateurId);

        seance.setDescription(request != null ? request.getContent() : null);
        seanceRepository.save(seance);
    }

    public void deleteVideo(UUID seanceId, UUID requesterId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));
        ensureCanManageFormation(seance.getCourse().getFormation(), requesterId);
        deleteObjectBestEffort("elearning-media", seance.getVideoKey());
        seance.setVideoKey(null);
        if (seance.getStatus() == SeanceStatus.CONTENU_DISPONIBLE) {
            seance.setStatus(SeanceStatus.PLANIFIEE);
        }
        seanceRepository.save(seance);
    }

    public Map<String, String> getStreamUrl(UUID seanceId, UUID userId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Seance non trouvee"));

        ensureCanReadFormation(seance.getCourse().getFormation(), userId, SecurityUtils.getCurrentUserRole());

        if (seance.getVideoKey() == null) {
            throw new ResourceNotFoundException("Pas de video pour cette seance");
        }
        if (!minioService.objectExists("elearning-media", seance.getVideoKey())) {
            throw new ResourceNotFoundException("Video indisponible dans le stockage");
        }

        try {
            String url = minioService.generatePresignedUrl("elearning-media", seance.getVideoKey(), STREAM_URL_EXPIRY_MINUTES);
            return Map.of(
                    "url", url,
                    "stream_url", url,
                    "expiresInMinutes", String.valueOf(STREAM_URL_EXPIRY_MINUTES),
                    "expires_in_seconds", String.valueOf(STREAM_URL_EXPIRY_MINUTES * 60),
                    "refresh_after_seconds", String.valueOf(STREAM_URL_REFRESH_AFTER_SECONDS),
                    "expires_at", LocalDateTime.now().plusMinutes(STREAM_URL_EXPIRY_MINUTES).toString()
            );
        } catch (Exception e) {
            throw new ValidationException("Erreur lors de la generation de l'URL : " + e.getMessage());
        }
    }

    public Map<String, String> getPdfUrl(UUID seanceId, UUID userId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("SÃ©ance non trouvÃ©e"));

        ensureCanReadFormation(seance.getCourse().getFormation(), userId, SecurityUtils.getCurrentUserRole());

        // Assume PDF resources are stored alongside video
        try {
            String objectKey = seance.getPdfKey();
            if (objectKey == null) {
                objectKey = pedagogicalResourceRepository.findBySeanceIdOrderByCreatedAtAsc(seanceId)
                        .stream()
                        .filter(resource -> "application/pdf".equals(resource.getMimeType()))
                        .map(PedagogicalResource::getObjectKey)
                        .findFirst()
                        .orElse("seances/" + seanceId + "/resources.pdf");
            }
            String url = minioService.generatePresignedUrl("elearning-media", objectKey, 60);
            return Map.of(
                    "url", url,
                    "download_url", url,
                    "objectKey", objectKey,
                    "expiresInMinutes", "60"
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Pas de ressource PDF pour cette sÃ©ance");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".mp4";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".mp4";
    }

    private SeanceResponseDto mapSeanceToDto(Seance seance, UUID userId) {
        SeanceResponseDto dto = new SeanceResponseDto();
        dto.setId(seance.getId());
        dto.setTitle(seance.getTitle());
        dto.setDescription(seance.getDescription());
        dto.setCourseId(seance.getCourse() != null ? seance.getCourse().getId() : seance.getCoursId());
        dto.setType(seance.getType().name());
        dto.setVideoKey(seance.getVideoKey());
        dto.setPdfKey(seance.getPdfKey());
        dto.setDurationSeconds(seance.getDuration());
        dto.setMeetingLink(seance.getMeetingLink());
        dto.setScheduledAt(seance.getScheduledAt());
        dto.setOrderIndex(seance.getOrderIndex());
        dto.setStatus(seance.getStatus().name());
        Progress progress = progressRepository.findByUserIdAndSeanceId(userId, seance.getId()).orElse(null);
        dto.setIsCompleted(progress != null && Boolean.TRUE.equals(progress.getIsCompleted()));
        dto.setProgressSeconds(progress != null && progress.getWatchedSeconds() != null ? progress.getWatchedSeconds() : 0);
        return dto;
    }

    private int recalculateCourseProgress(UUID userId, UUID courseId) {
        long totalSeances = seanceRepository.countByCoursId(courseId);
        if (totalSeances == 0) return 0;

        long completedSeances = progressRepository.countCompletedByUserIdAndCourseId(userId, courseId);
        int percent = (int) Math.round((completedSeances * 100.0) / totalSeances);

        Progression progression = progressionRepository.findByApprenantIdAndCoursId(userId, courseId).orElse(null);
        if (progression != null && percent == 100 && progression.getCompletionDate() == null) {
            progression.setCompletionDate(LocalDateTime.now());
            progressionRepository.save(progression);
        }
        return percent;
    }

    private CourseResponseDto mapCourseToDto(Course course) {
        CourseResponseDto response = new CourseResponseDto();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setOrderIndex(course.getOrderIndex());
        response.setFormationId(course.getFormation().getId());
        response.setStatus(course.getStatus().name());
        response.setPresenceThreshold(course.getPresenceThreshold());
        response.setSeancesCount((int) seanceRepository.countByCoursId(course.getId()));
        return response;
    }

    private void ensureCanManageFormation(Formation formation, UUID requesterId) {
        if (Objects.equals(formation.getFormateurId(), requesterId)) {
            return;
        }
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_ADMIN_ORG".equals(role)) {
            UUID organisationId = SecurityUtils.getCurrentOrganisationId();
            if (organisationId == null) {
                organisationId = userRepository.findById(requesterId)
                        .map(User::getOrganisationId)
                        .orElse(null);
            }
            if (Objects.equals(formation.getOrganisationId(), organisationId)) {
                return;
            }
        }
        throw new AccessDeniedException("Vous n'Ãªtes pas autorisÃ© Ã  modifier cette formation");
    }
    private void ensureCanReadFormation(Formation formation, UUID userId, String role) {
        if ("ROLE_APPRENANT".equals(role)) {
            if (hasActiveEnrollment(userId, formation.getId())) {
                return;
            }
            throw new AccessDeniedException("Inscription active requise pour acceder a cette formation");
        }

        if ("ROLE_FORMATEUR".equals(role) && Objects.equals(formation.getFormateurId(), userId)) {
            return;
        }

        if ("ROLE_ADMIN_ORG".equals(role)) {
            UUID organisationId = SecurityUtils.getCurrentOrganisationId();
            if (organisationId == null) {
                organisationId = userRepository.findById(userId)
                        .map(User::getOrganisationId)
                        .orElse(null);
            }
            if (Objects.equals(formation.getOrganisationId(), organisationId)) {
                return;
            }
        }

        throw new AccessDeniedException("Vous n'etes pas autorise a consulter cette formation");
    }

    private boolean hasActiveEnrollment(UUID userId, UUID formationId) {
        return inscriptionRepository.existsByApprenantIdAndFormationIdAndStatus(
                userId, formationId, com.elearning.resourceserver.domain.enums.InscriptionStatus.EN_COURS)
                || inscriptionRepository.existsByApprenantIdAndFormationIdAndStatus(
                userId, formationId, com.elearning.resourceserver.domain.enums.InscriptionStatus.TERMINEE);
    }

    private void deleteSeanceInternal(Seance seance) {
        deleteObjectBestEffort("elearning-media", seance.getVideoKey());
        deleteObjectBestEffort("elearning-media", seance.getPdfKey());
        pedagogicalResourceRepository.findBySeanceIdOrderByCreatedAtAsc(seance.getId()).forEach(resource -> {
            deleteObjectBestEffort(resource.getBucketName(), resource.getObjectKey());
            pedagogicalResourceRepository.delete(resource);
        });
        executeDelete("DELETE FROM forum_posts WHERE seance_id = :id", seance.getId());
        executeDelete("DELETE FROM progress WHERE seance_id = :id", seance.getId());
        executeDelete("DELETE FROM attendances WHERE seance_id = :id", seance.getId());
        executeDelete("DELETE FROM presences WHERE seance_id = :id", seance.getId());
        seanceRepository.delete(seance);
    }

    private void deleteCourseResources(UUID courseId) {
        pedagogicalResourceRepository.findByCourseIdOrderByCreatedAtAsc(courseId).forEach(resource -> {
            deleteObjectBestEffort(resource.getBucketName(), resource.getObjectKey());
            pedagogicalResourceRepository.delete(resource);
        });
    }

    private void deleteObjectBestEffort(String bucketName, String objectKey) {
        if (bucketName == null || bucketName.isBlank() || objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioService.deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            log.warn("Could not delete MinIO object {}/{}: {}", bucketName, objectKey, e.getMessage());
        }
    }

    private int executeDelete(String sql, UUID id) {
        return entityManager.createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();
    }
}

