package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.CourseAndSeanceService;
import com.elearning.resourceserver.domain.dto.CourseRequestDto;
import com.elearning.resourceserver.domain.dto.CourseResponseDto;
import com.elearning.resourceserver.domain.dto.SeanceResponseDto;
import com.elearning.resourceserver.util.SecurityUtils;
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
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ORGANISATION')")
    public ResponseEntity<List<CourseResponseDto>> getCourses(@PathVariable UUID formationId) {
        return ResponseEntity.ok(courseService.getCoursesByFormation(
            formationId, 
            SecurityUtils.getCurrentUserId(), 
            SecurityUtils.getCurrentUserRole()
        ));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody CourseRequestDto courseDto) {
        return ResponseEntity.ok(courseService.createCourse(courseDto, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/courses/{courseId}/seances")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<SeanceResponseDto> createSeance(
            @PathVariable UUID courseId,
            @RequestPart("data") String seanceData,
            @RequestPart(value = "video", required = false) MultipartFile video) {
        return ResponseEntity.ok(courseService.createSeance(courseId, seanceData, video, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/seances/{id}/stream-url")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<Map<String, String>> getStreamUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getStreamUrl(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/progress/{seanceId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<Void> updateProgress(@PathVariable UUID seanceId, @RequestBody Map<String, Integer> payload) {
        Integer watchedSeconds = payload.get("watchedSeconds");
        if (watchedSeconds == null) watchedSeconds = 0;
        courseService.updateProgress(seanceId, SecurityUtils.getCurrentUserId(), watchedSeconds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seances/{id}/start")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<Void> startSeance(@PathVariable UUID id) {
        courseService.startSeance(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seances/{id}/end")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<Void> endSeance(@PathVariable UUID id) {
        courseService.endSeance(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seances/{id}/video")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<Void> uploadVideo(
            @PathVariable UUID id,
            @RequestPart("video") MultipartFile video) {
        courseService.uploadVideo(id, video, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/seances/{id}/pdf-url")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<Map<String, String>> getPdfUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getPdfUrl(id, SecurityUtils.getCurrentUserId()));
    }
}
