package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.CourseAndSeanceService;
import com.elearning.resourceserver.domain.dto.CourseRequestDto;
import com.elearning.resourceserver.domain.dto.CourseResponseDto;
import com.elearning.resourceserver.domain.dto.ProgressResponseDto;
import com.elearning.resourceserver.domain.dto.ProgressUpdateRequest;
import com.elearning.resourceserver.domain.dto.SeanceResponseDto;
import com.elearning.resourceserver.domain.dto.SeanceTextContentRequestDto;
import com.elearning.resourceserver.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseAndSeanceController {

    private final CourseAndSeanceService courseService;

    @GetMapping("/formations/{formationId}/courses")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ORGANISATION', 'ADMIN_ORG')")
    public ResponseEntity<List<CourseResponseDto>> getCourses(@PathVariable UUID formationId) {
        return ResponseEntity.ok(courseService.getCoursesByFormation(
            formationId, 
            SecurityUtils.getCurrentUserId(), 
            SecurityUtils.getCurrentUserRole()
        ));
    }

    @GetMapping("/courses/{courseId}/seances")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ORGANISATION', 'ADMIN_ORG')")
    public ResponseEntity<List<SeanceResponseDto>> getSeances(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getSeancesByCourse(
            courseId,
            SecurityUtils.getCurrentUserId(),
            SecurityUtils.getCurrentUserRole()
        ));
    }

    @GetMapping("/seances/{id}")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ORGANISATION', 'ADMIN_ORG')")
    public ResponseEntity<SeanceResponseDto> getSeance(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getSeance(
            id,
            SecurityUtils.getCurrentUserId(),
            SecurityUtils.getCurrentUserRole()
        ));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto courseDto) {
        return ResponseEntity.ok(courseService.createCourse(courseDto, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequestDto courseDto) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDto, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/courses/{courseId}/seances")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<SeanceResponseDto> createSeance(
            @PathVariable UUID courseId,
            @RequestPart("data") String seanceData,
            @RequestPart(value = "video", required = false) MultipartFile video) {
        return ResponseEntity.ok(courseService.createSeance(courseId, seanceData, video, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/seances/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<SeanceResponseDto> updateSeance(
            @PathVariable UUID id,
            @Valid @RequestBody com.elearning.resourceserver.domain.dto.SeanceRequestDto seanceDto) {
        return ResponseEntity.ok(courseService.updateSeance(id, seanceDto, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/seances/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> deleteSeance(@PathVariable UUID id) {
        courseService.deleteSeance(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seances/{id}/stream-url")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Map<String, String>> getStreamUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getStreamUrl(id, SecurityUtils.getCurrentUserId()));
    }

    @PatchMapping("/progress/{seanceId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<ProgressResponseDto> patchProgress(
            @PathVariable UUID seanceId,
            @RequestBody ProgressUpdateRequest payload) {
        return ResponseEntity.ok(courseService.updateProgress(seanceId, SecurityUtils.getCurrentUserId(), payload));
    }

    @PostMapping("/progress/{seanceId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<ProgressResponseDto> updateProgress(
            @PathVariable UUID seanceId,
            @RequestBody ProgressUpdateRequest payload) {
        return ResponseEntity.ok(courseService.updateProgress(seanceId, SecurityUtils.getCurrentUserId(), payload));
    }

    @PostMapping("/seances/{id}/start")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> startSeance(@PathVariable UUID id) {
        courseService.startSeance(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seances/{id}/end")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> endSeance(@PathVariable UUID id) {
        courseService.endSeance(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seances/{id}/video")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> uploadVideo(
            @PathVariable UUID id,
            @RequestPart("video") MultipartFile video) {
        courseService.uploadVideo(id, video, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/seances/{id}/video")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID id) {
        courseService.deleteVideo(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/seances/{id}/text-content")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> updateTextContent(
            @PathVariable UUID id,
            @RequestBody SeanceTextContentRequestDto request) {
        courseService.updateSeanceTextContent(id, request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/seances/{id}/pdf-url")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Map<String, String>> getPdfUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getPdfUrl(id, SecurityUtils.getCurrentUserId()));
    }
}
