package com.elearning.resourceserver.api;

import com.elearning.resourceserver.domain.Formation;
import com.elearning.resourceserver.domain.Course;
import com.elearning.resourceserver.domain.PedagogicalResource;
import com.elearning.resourceserver.domain.Seance;
import com.elearning.resourceserver.domain.dto.PedagogicalResourceDto;
import com.elearning.resourceserver.domain.enums.ResourceType;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.FormationRepository;
import com.elearning.resourceserver.repository.CourseRepository;
import com.elearning.resourceserver.repository.InscriptionRepository;
import com.elearning.resourceserver.repository.PedagogicalResourceRepository;
import com.elearning.resourceserver.repository.SeanceRepository;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PedagogicalResourceController {

    private static final String MEDIA_BUCKET = "elearning-media";

    private final PedagogicalResourceRepository resourceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final MinioService minioService;
    private final SeanceRepository seanceRepository;
    private final CourseRepository courseRepository;
    private final FormationRepository formationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/seances/{seanceId}/resources")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<List<PedagogicalResourceDto>> listSeanceResources(@PathVariable UUID seanceId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<PedagogicalResource> resources = resourceRepository.findBySeanceIdOrderByCreatedAtAsc(seanceId);
        resources.forEach(resource -> ensureCanRead(userId, resource.getFormationId()));
        return ResponseEntity.ok(resources.stream().map(this::toDto).toList());
    }

    @PostMapping("/seances/{seanceId}/resources")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<PedagogicalResourceDto> uploadSeanceResource(
            @PathVariable UUID seanceId,
            @RequestPart("data") String dataJson,
            @RequestPart("file") MultipartFile file) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
        Course course = courseRepository.findById(seance.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
        Formation formation = formationRepository.findById(course.getFormationId())
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManage(formation);

        try {
            Map<String, Object> data = dataJson == null || dataJson.isBlank()
                    ? Map.of()
                    : objectMapper.readValue(dataJson, new TypeReference<>() {});
            String title = Objects.toString(data.getOrDefault("title", file.getOriginalFilename()), "Ressource");
            String objectKey = minioService.uploadFile(file, MEDIA_BUCKET);

            PedagogicalResource resource = new PedagogicalResource();
            resource.setFormationId(formation.getId());
            resource.setCourseId(course.getId());
            resource.setSeanceId(seance.getId());
            resource.setType(resolveResourceType(Objects.toString(data.get("type"), null), file.getContentType()));
            resource.setTitle(title);
            resource.setObjectKey(objectKey);
            resource.setBucketName(MEDIA_BUCKET);
            resource.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            resource.setSizeBytes(file.getSize());
            resource.setIsDownloadable(!Boolean.FALSE.equals(data.get("isDownloadable")));

            return ResponseEntity.ok(toDto(resourceRepository.save(resource)));
        } catch (Exception e) {
            throw new ValidationException("Erreur upload ressource : " + e.getMessage());
        }
    }

    @PostMapping("/seances/{seanceId}/links")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<PedagogicalResourceDto> addExternalLink(
            @PathVariable UUID seanceId,
            @RequestBody Map<String, Object> data) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
        Course course = courseRepository.findById(seance.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
        Formation formation = formationRepository.findById(course.getFormationId())
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManage(formation);

        String url = Objects.toString(data.get("url"), "").trim();
        if (url.isBlank()) {
            throw new ValidationException("URL obligatoire");
        }
        String title = Objects.toString(data.getOrDefault("title", url), url);

        PedagogicalResource resource = new PedagogicalResource();
        resource.setFormationId(formation.getId());
        resource.setCourseId(course.getId());
        resource.setSeanceId(seance.getId());
        resource.setType(ResourceType.LINK);
        resource.setTitle(title);
        resource.setObjectKey(url);
        resource.setBucketName("external");
        resource.setMimeType("text/uri-list");
        resource.setSizeBytes(0L);
        resource.setIsDownloadable(false);
        return ResponseEntity.ok(toDto(resourceRepository.save(resource)));
    }

    @PutMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<PedagogicalResourceDto> replaceResource(
            @PathVariable UUID id,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        PedagogicalResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));
        Formation formation = formationRepository.findById(resource.getFormationId())
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManage(formation);

        try {
            Map<String, Object> data = dataJson == null || dataJson.isBlank()
                    ? Map.of()
                    : objectMapper.readValue(dataJson, new TypeReference<>() {});
            if (data.containsKey("title")) {
                resource.setTitle(Objects.toString(data.get("title"), resource.getTitle()));
            }
            if (file != null && !file.isEmpty()) {
                String previousBucket = resource.getBucketName();
                String previousKey = resource.getObjectKey();
                String objectKey = minioService.uploadFile(file, MEDIA_BUCKET);
                resource.setObjectKey(objectKey);
                resource.setBucketName(MEDIA_BUCKET);
                resource.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
                resource.setSizeBytes(file.getSize());
                resource.setType(resolveResourceType(Objects.toString(data.get("type"), null), resource.getMimeType()));
                resource.setVersion(resource.getVersion() == null ? 1 : resource.getVersion() + 1);
                deleteObjectBestEffort(previousBucket, previousKey);
            }
            if (data.containsKey("isDownloadable")) {
                resource.setIsDownloadable(!Boolean.FALSE.equals(data.get("isDownloadable")));
            }
            return ResponseEntity.ok(toDto(resourceRepository.save(resource)));
        } catch (Exception e) {
            throw new ValidationException("Erreur remplacement ressource : " + e.getMessage());
        }
    }

    @GetMapping("/resources/{id}/download-url")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable UUID id) {
        PedagogicalResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));
        ensureCanRead(SecurityUtils.getCurrentUserId(), resource.getFormationId());
        if (!Boolean.TRUE.equals(resource.getIsDownloadable())) {
            throw new AccessDeniedException("Cette ressource n'est pas téléchargeable");
        }
        try {
            String url = minioService.generatePresignedUrl(resource.getBucketName(), resource.getObjectKey(), 60);
            return ResponseEntity.ok(Map.of(
                    "download_url", url,
                    "url", url,
                    "expires_at", LocalDateTime.now().plusMinutes(60).toString(),
                    "object_key", resource.getObjectKey()
            ));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Fichier non disponible dans MinIO");
        }
    }

    @DeleteMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        PedagogicalResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));
        Formation formation = formationRepository.findById(resource.getFormationId())
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManage(formation);
        try {
            minioService.deleteObject(resource.getBucketName(), resource.getObjectKey());
        } catch (Exception ignored) {
            // Database deletion remains authoritative if the object was already absent.
        }
        resourceRepository.delete(resource);
        return ResponseEntity.noContent().build();
    }

    private void deleteObjectBestEffort(String bucketName, String objectKey) {
        if (bucketName == null || bucketName.isBlank() || objectKey == null || objectKey.isBlank() || "external".equals(bucketName)) {
            return;
        }
        try {
            minioService.deleteObject(bucketName, objectKey);
        } catch (Exception ignored) {
        }
    }

    private void ensureCanRead(UUID userId, UUID formationId) {
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_APPRENANT".equals(role)) {
            if (!inscriptionRepository.existsByApprenantIdAndFormationId(userId, formationId)) {
                throw new AccessDeniedException("Inscription requise pour accéder à cette ressource");
            }
            return;
        }

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));
        ensureCanManage(formation);
    }

    private void ensureCanManage(Formation formation) {
        UUID userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_FORMATEUR".equals(role) && Objects.equals(formation.getFormateurId(), userId)) {
            return;
        }
        if ("ROLE_ADMIN_ORG".equals(role)) {
            UUID organisationId = SecurityUtils.getCurrentOrganisationId();
            if (organisationId == null) {
                organisationId = userRepository.findById(userId).map(user -> user.getOrganisationId()).orElse(null);
            }
            if (Objects.equals(formation.getOrganisationId(), organisationId)) {
                return;
            }
        }
        throw new AccessDeniedException("Vous n'êtes pas autorisé à gérer cette ressource");
    }

    private ResourceType resolveResourceType(String requestedType, String mimeType) {
        if (requestedType != null && !requestedType.isBlank()) {
            try {
                return ResourceType.valueOf(requestedType.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Fall through to MIME detection.
            }
        }
        if (mimeType == null) return ResourceType.FILE;
        if (mimeType.equals("application/pdf")) return ResourceType.PDF;
        if (mimeType.startsWith("image/")) return ResourceType.IMAGE;
        if (mimeType.startsWith("video/")) return ResourceType.VIDEO;
        return ResourceType.FILE;
    }

    private PedagogicalResourceDto toDto(PedagogicalResource resource) {
        PedagogicalResourceDto dto = new PedagogicalResourceDto();
        dto.setId(resource.getId());
        dto.setFormationId(resource.getFormationId());
        dto.setCourseId(resource.getCourseId());
        dto.setSeanceId(resource.getSeanceId());
        dto.setType(resource.getType().name());
        dto.setTitle(resource.getTitle());
        dto.setFileName(resource.getTitle());
        dto.setObjectKey(resource.getObjectKey());
        dto.setFileKey(resource.getObjectKey());
        dto.setMimeType(resource.getMimeType());
        dto.setSizeBytes(resource.getSizeBytes());
        dto.setFileSize(resource.getSizeBytes());
        dto.setIsDownloadable(resource.getIsDownloadable());
        dto.setVersion(resource.getVersion());
        dto.setCreatedAt(resource.getCreatedAt());
        if (resource.getType() == ResourceType.LINK) {
            dto.setFileUrl(resource.getObjectKey());
            return dto;
        }
        try {
            dto.setFileUrl(minioService.generatePresignedUrl(resource.getBucketName(), resource.getObjectKey(), 60));
        } catch (Exception ignored) {
            dto.setFileUrl(null);
        }
        return dto;
    }
}
