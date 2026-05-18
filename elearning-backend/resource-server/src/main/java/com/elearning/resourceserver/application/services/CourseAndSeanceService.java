package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.SeanceStatus;
import com.elearning.resourceserver.domain.enums.SeanceType;
import com.elearning.resourceserver.domain.dto.CourseRequestDto;
import com.elearning.resourceserver.domain.dto.CourseResponseDto;
import com.elearning.resourceserver.domain.dto.SeanceRequestDto;
import com.elearning.resourceserver.domain.dto.SeanceResponseDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CourseAndSeanceService {

    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ProgressionRepository progressionRepository;
    private final MinioService minioService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    private static final Set<String> ALLOWED_VIDEO_FORMATS = Set.of(
            "video/mp4", "video/webm", "video/quicktime"
    );
    private static final long MAX_VIDEO_SIZE = 2L * 1024 * 1024 * 1024; // 2GB

    public List<CourseResponseDto> getCoursesByFormation(UUID formationId, UUID userId, String role) {
        if ("ROLE_APPRENANT".equals(role) && !inscriptionRepository.existsByApprenantIdAndFormationId(userId, formationId)) {
            throw new AccessDeniedException("Inscription requise pour accéder à ces cours");
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

    public CourseResponseDto createCourse(CourseRequestDto dto, UUID formateurId) {
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setFormationId(dto.getFormationId());
        course.setOrderIndex(dto.getOrderIndex());
        if (dto.getPresenceThreshold() != null) course.setPresenceThreshold(dto.getPresenceThreshold());
        if (dto.getQuizPassScore() != null) course.setQuizPassScore(dto.getQuizPassScore());
        if (dto.getEstimatedDuration() != null) course.setEstimatedDuration(dto.getEstimatedDuration());

        Course saved = courseRepository.save(course);

        CourseResponseDto response = new CourseResponseDto();
        response.setId(saved.getId());
        response.setTitle(saved.getTitle());
        response.setDescription(saved.getDescription());
        response.setOrderIndex(saved.getOrderIndex());
        response.setFormationId(saved.getFormationId());
        response.setStatus(saved.getStatus().name());
        return response;
    }

    public SeanceResponseDto createSeance(UUID courseId, String seanceDataJson, MultipartFile video, UUID formateurId) {
        try {
            SeanceRequestDto dto = objectMapper.readValue(seanceDataJson, SeanceRequestDto.class);

            SeanceType type = SeanceType.valueOf(dto.getType());
            if (type == SeanceType.LIVE && (dto.getMeetingLink() == null || dto.getMeetingLink().isEmpty())) {
                throw new ValidationException("Lien meeting obligatoire pour une séance live");
            }

            Seance seance = new Seance();
            seance.setTitle(dto.getTitle());
            seance.setDescription(dto.getDescription());
            seance.setType(type);
            seance.setScheduledAt(dto.getScheduledAt());
            seance.setMeetingLink(dto.getMeetingLink());
            seance.setDuration(dto.getDuration());
            seance.setOrderIndex(dto.getOrderIndex());
            seance.setCoursId(courseId);
            seance.setFormateurId(formateurId);

            Seance saved = seanceRepository.save(seance);

            SeanceResponseDto res = new SeanceResponseDto();
            res.setId(saved.getId());
            res.setTitle(saved.getTitle());
            res.setType(saved.getType().name());
            res.setStatus(saved.getStatus().name());
            res.setScheduledAt(saved.getScheduledAt());
            res.setMeetingLink(saved.getMeetingLink());
            res.setOrderIndex(saved.getOrderIndex());
            return res;

        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidationException("Erreur de création de séance : " + e.getMessage());
        }
    }

    /**
     * Start a live session (FORMATEUR)
     */
    public void startSeance(UUID seanceId, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        if (!seance.getFormateurId().equals(formateurId)) {
            throw new AccessDeniedException("Vous n'êtes pas le formateur de cette séance");
        }

        if (seance.getType() != SeanceType.LIVE) {
            throw new ValidationException("Seule une séance LIVE peut être démarrée");
        }

        // Check scheduled time window ±30min
        if (seance.getScheduledAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(seance.getScheduledAt().minusMinutes(30))
                    || now.isAfter(seance.getScheduledAt().plusMinutes(30))) {
                throw new ValidationException("La séance ne peut être démarrée que dans un intervalle de ±30 minutes de l'heure prévue");
            }
        }

        seance.setStatus(SeanceStatus.EN_COURS);
        seanceRepository.save(seance);
    }
    
    public void updateProgress(UUID seanceId, UUID userId, Integer watchedSeconds) {
        // Simple mock method for compilation
        log.info("Updating progress for seance {}, user {}, seconds {}", seanceId, userId, watchedSeconds);
    }

    /**
     * End a live session (FORMATEUR)
     */
    public void endSeance(UUID seanceId, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        if (!seance.getFormateurId().equals(formateurId)) {
            throw new AccessDeniedException("Vous n'êtes pas le formateur de cette séance");
        }

        seance.setStatus(SeanceStatus.TERMINEE);
        seanceRepository.save(seance);
    }

    /**
     * UC-03: Upload vidéo post-séance (RB-03)
     */
    public void uploadVideo(UUID seanceId, MultipartFile video, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        // Step 1: Verify ownership
        if (!seance.getFormateurId().equals(formateurId)) {
            throw new AccessDeniedException("Vous n'êtes pas le formateur de cette séance");
        }

        // Step 2: RB-03 — seance.status must be TERMINEE
        if (seance.getStatus() != SeanceStatus.TERMINEE) {
            throw new ValidationException("La séance doit être terminée avant d'uploader une vidéo");
        }

        // Step 3: Validate format
        String contentType = video.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_FORMATS.contains(contentType)) {
            throw new ValidationException("Format non supporté. Formats acceptés : mp4, webm, mov");
        }

        // Step 4: Validate size
        if (video.getSize() > MAX_VIDEO_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La vidéo ne doit pas dépasser 2 Go");
        }

        try {
            // Step 5-7: Upload and move
            String tempKey = minioService.uploadFile(video, "elearning-uploads");
            String finalKey = "seances/" + seanceId + "/video" + getExtension(video.getOriginalFilename());
            minioService.moveObject("elearning-uploads", tempKey, "elearning-media", finalKey);

            // Step 8-9: Update seance
            seance.setVideoKey(finalKey);
            seance.setStatus(SeanceStatus.CONTENU_DISPONIBLE);
            seanceRepository.save(seance);

            // Step 10: Notification
            UUID formationId = seance.getCourse().getFormation().getId();
            notificationService.sendToTopic("formation_" + formationId,
                    "Nouvel enregistrement disponible",
                    "Un nouvel enregistrement est disponible pour la séance '" + seance.getTitle() + "'",
                    Map.of("type", "VIDEO_AVAILABLE", "seanceId", seanceId.toString()));

        } catch (Exception e) {
            throw new ValidationException("Erreur lors de l'upload vidéo : " + e.getMessage());
        }
    }

    public Map<String, String> getStreamUrl(UUID seanceId, UUID userId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        UUID formationId = seance.getCourse().getFormation().getId();
        if (!inscriptionRepository.existsByApprenantIdAndFormationId(userId, formationId)) {
            throw new AccessDeniedException("Vous n'êtes pas inscrit à cette formation");
        }

        if (seance.getVideoKey() == null) {
            throw new ResourceNotFoundException("Pas de vidéo pour cette séance");
        }

        try {
            String url = minioService.generatePresignedUrl("elearning-media", seance.getVideoKey(), 15);
            return Map.of("url", url, "expiresInMinutes", "15");
        } catch (Exception e) {
            throw new ValidationException("Erreur lors de la génération de l'URL : " + e.getMessage());
        }
    }

    public Map<String, String> getPdfUrl(UUID seanceId, UUID userId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        UUID formationId = seance.getCourse().getFormation().getId();
        if (!inscriptionRepository.existsByApprenantIdAndFormationId(userId, formationId)) {
            throw new AccessDeniedException("Vous n'êtes pas inscrit à cette formation");
        }

        // Assume PDF resources are stored alongside video
        try {
            String url = minioService.generatePresignedUrl("elearning-media",
                    "seances/" + seanceId + "/resources.pdf", 60);
            return Map.of("url", url, "expiresInMinutes", "60");
        } catch (Exception e) {
            throw new ResourceNotFoundException("Pas de ressource PDF pour cette séance");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".mp4";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".mp4";
    }
}
