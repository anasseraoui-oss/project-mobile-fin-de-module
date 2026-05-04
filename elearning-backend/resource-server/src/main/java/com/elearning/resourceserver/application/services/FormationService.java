package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Enrollment;
import com.elearning.resourceserver.domain.Formation;
import com.elearning.resourceserver.domain.dto.EnrollmentResponseDto;
import com.elearning.resourceserver.domain.dto.FormationRequestDto;
import com.elearning.resourceserver.domain.dto.FormationResponseDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.EnrollmentRepository;
import com.elearning.resourceserver.repository.FormationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    @Value("${minio.bucket-public}")
    private String bucketPublic;

    public Page<FormationResponseDto> searchFormations(String level, String language, String search, Pageable pageable) {
        return formationRepository.findByFilters(level, language, search, pageable)
                .map(this::mapToResponseDto);
    }

    public FormationResponseDto getFormationDetails(UUID id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        
        // Count would typically be fetched via custom queries or mapped fields. 
        // For strict compliance, mapping basic fields.
        return mapToResponseDto(formation);
    }

    public FormationResponseDto createFormation(String dataJson, MultipartFile thumbnail, UUID organisationId) {
        try {
            FormationRequestDto dto = objectMapper.readValue(dataJson, FormationRequestDto.class);
            String thumbnailKey = null;

            if (thumbnail != null && !thumbnail.isEmpty()) {
                thumbnailKey = minioService.uploadFile(thumbnail, bucketPublic, "thumbnails");
            }

            Formation formation = new Formation();
            formation.setTitle(dto.getTitle());
            formation.setDescription(dto.getDescription());
            formation.setLevel(dto.getLevel());
            formation.setLanguage(dto.getLanguage());
            formation.setPrice(dto.getPrice());
            formation.setOrganisationId(organisationId);
            formation.setThumbnailKey(thumbnailKey);
            formation.setPublished(false);
            formation.setCreatedAt(LocalDateTime.now());
            // prerequisites mapping would go here if defined in entity

            Formation saved = formationRepository.save(formation);
            return mapToResponseDto(saved);
        } catch (Exception e) {
            throw new ValidationException("Erreur lors de la création de la formation : " + e.getMessage());
        }
    }

    public void updateFormation(UUID id, FormationRequestDto dto, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        if (!formation.getOrganisationId().equals(requesterId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette formation");
        }

        formation.setTitle(dto.getTitle());
        formation.setDescription(dto.getDescription());
        formation.setLevel(dto.getLevel());
        formation.setLanguage(dto.getLanguage());
        formation.setPrice(dto.getPrice());

        formationRepository.save(formation);
    }

    public void publishFormation(UUID id, UUID requesterId) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        if (!formation.getOrganisationId().equals(requesterId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à publier cette formation");
        }

        if (formation.getCourses() == null || formation.getCourses().isEmpty()) {
            throw new ValidationException("Ajoutez au moins un cours avant de publier");
        }

        formation.setPublished(true);
        formationRepository.save(formation);
    }

    public EnrollmentResponseDto enrollUser(UUID userId, UUID formationId) {
        if (enrollmentRepository.existsByUserIdAndFormationId(userId, formationId)) {
            throw new ValidationException("Déjà inscrit à cette formation");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        enrollment.setFormation(formation);
        enrollment.setEnrolledAt(LocalDateTime.now());

        Enrollment saved = enrollmentRepository.save(enrollment);
        
        EnrollmentResponseDto response = new EnrollmentResponseDto();
        response.setId(saved.getId());
        response.setUserId(saved.getUserId());
        response.setFormationId(formation.getId());
        response.setEnrolledAt(saved.getEnrolledAt());
        return response;
    }

    private FormationResponseDto mapToResponseDto(Formation formation) {
        FormationResponseDto dto = new FormationResponseDto();
        dto.setId(formation.getId());
        dto.setTitle(formation.getTitle());
        dto.setDescription(formation.getDescription());
        dto.setLevel(formation.getLevel());
        dto.setLanguage(formation.getLanguage());
        dto.setPrice(formation.getPrice());
        dto.setPublished(formation.isPublished());
        // Populate additional fields (courses count, enrolled count) via subsequent repo calls in a real scenario
        return dto;
    }
}
