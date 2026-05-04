package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Course;
import com.elearning.resourceserver.domain.Progress;
import com.elearning.resourceserver.domain.Seance;
import com.elearning.resourceserver.domain.dto.CourseRequestDto;
import com.elearning.resourceserver.domain.dto.CourseResponseDto;
import com.elearning.resourceserver.domain.dto.SeanceRequestDto;
import com.elearning.resourceserver.domain.dto.SeanceResponseDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.CourseRepository;
import com.elearning.resourceserver.repository.EnrollmentRepository;
import com.elearning.resourceserver.repository.ProgressRepository;
import com.elearning.resourceserver.repository.SeanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseAndSeanceService {

    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    public List<CourseResponseDto> getCoursesByFormation(UUID formationId, UUID userId, String role) {
        if ("ROLE_APPRENANT".equals(role) && !enrollmentRepository.existsByUserIdAndFormationId(userId, formationId)) {
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
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CourseResponseDto createCourse(CourseRequestDto dto, UUID formateurId) {
        // Additional ownership logic might go here
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setFormationId(dto.getFormationId());
        course.setOrderIndex(dto.getOrderIndex());
        
        Course saved = courseRepository.save(course);
        
        CourseResponseDto response = new CourseResponseDto();
        response.setId(saved.getId());
        response.setTitle(saved.getTitle());
        response.setDescription(saved.getDescription());
        response.setOrderIndex(saved.getOrderIndex());
        response.setFormationId(saved.getFormationId());
        return response;
    }

    public SeanceResponseDto createSeance(UUID courseId, String seanceDataJson, MultipartFile video, UUID formateurId) {
        try {
            SeanceRequestDto dto = objectMapper.readValue(seanceDataJson, SeanceRequestDto.class);

            if ("LIVE".equals(dto.getType()) && (dto.getMeetLink() == null || dto.getMeetLink().isEmpty())) {
                throw new ValidationException("Lien Meet obligatoire pour une séance live");
            }

            Seance seance = new Seance();
            seance.setTitle(dto.getTitle());
            seance.setType(dto.getType());
            seance.setScheduledAt(dto.getScheduledAt());
            seance.setMeetLink(dto.getMeetLink());
            seance.setOrderIndex(dto.getOrderIndex());
            seance.setCourseId(courseId);

            if (video != null && !video.isEmpty()) {
                String tempKey = minioService.uploadFile(video, "elearning-uploads", "temp/" + formateurId);
                String finalKey = minioService.validateAndMoveVideoUpload(tempKey);
                seance.setVideoKey(finalKey);
            }

            Seance saved = seanceRepository.save(seance);
            
            SeanceResponseDto res = new SeanceResponseDto();
            res.setId(saved.getId());
            res.setTitle(saved.getTitle());
            res.setType(saved.getType());
            res.setScheduledAt(saved.getScheduledAt());
            res.setMeetLink(saved.getMeetLink());
            res.setOrderIndex(saved.getOrderIndex());
            return res;
            
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidationException("Erreur de création de séance : " + e.getMessage());
        }
    }

    public Map<String, String> getStreamUrl(UUID seanceId, UUID userId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
                
        // Validation enrollment (needs course -> formation relation, assumed implemented in repo)
        Course course = courseRepository.findById(seance.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
                
        if (!enrollmentRepository.existsByUserIdAndFormationId(userId, course.getFormationId())) {
             throw new AccessDeniedException("Vous n'êtes pas inscrit à cette formation");
        }

        if (seance.getVideoKey() == null) {
            throw new ResourceNotFoundException("Pas de vidéo pour cette séance");
        }

        String url = minioService.generatePresignedUrl("elearning-media", seance.getVideoKey(), 15);
        return Map.of("url", url, "expiresInMinutes", "15");
    }

    public void updateProgress(UUID seanceId, UUID userId, int watchedSeconds) {
        Progress progress = progressRepository.findByUserIdAndSeanceId(userId, seanceId)
                .orElse(new Progress(userId, seanceId));
                
        progress.setWatchedSeconds(watchedSeconds);
        
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
                
        if (seance.getDurationSeconds() != null && watchedSeconds >= seance.getDurationSeconds() * 0.9) {
            progress.setCompleted(true);
        }
        
        progressRepository.save(progress);
    }
}
