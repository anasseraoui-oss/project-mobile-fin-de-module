package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.QuizService;
import com.elearning.resourceserver.domain.dto.QuizRequestDto;
import com.elearning.resourceserver.domain.dto.QuizResponseDto;
import com.elearning.resourceserver.domain.dto.QuizResultDto;
import com.elearning.resourceserver.domain.dto.QuizSubmitDto;
import com.elearning.resourceserver.domain.dto.QuizHistoryItemDto;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/quizzes/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizHistoryItemDto>> getQuizHistory() {
        return ResponseEntity.ok(quizService.getQuizHistory(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/courses/{courseId}/quiz")
    @PreAuthorize("hasAnyRole('APPRENANT', 'FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<QuizResponseDto> getQuizDefinition(@PathVariable UUID courseId) {
        return ResponseEntity.ok(quizService.getQuizDefinition(courseId, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/quizzes/{quizId}/start")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<Map<String, Object>> startQuizSession(@PathVariable UUID quizId) {
        return ResponseEntity.ok(quizService.startQuiz(SecurityUtils.getCurrentUserId(), quizId));
    }

    @PostMapping("/quizzes/{quizId}/submit")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<QuizResultDto> submitQuizAnswers(
            @PathVariable UUID quizId, 
            @RequestBody QuizSubmitDto answers) {
        return ResponseEntity.ok(quizService.submitQuiz(SecurityUtils.getCurrentUserId(), quizId, answers));
    }

    @GetMapping("/quizzes/{quizId}/results/{attemptId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<QuizResultDto> getQuizAttemptResults(@PathVariable UUID quizId, @PathVariable UUID attemptId) {
        return ResponseEntity.ok(quizService.getAttemptResults(quizId, attemptId, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/quizzes")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<QuizResponseDto> createQuiz(@RequestBody QuizRequestDto quizDto) {
        return ResponseEntity.ok(quizService.createQuiz(quizDto, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<QuizResponseDto> updateQuiz(@PathVariable UUID id, @RequestBody QuizRequestDto quizDto) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quizDto, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable UUID id) {
        quizService.deleteQuiz(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quizzes/{id}/attempts/{apprenantId}/reset")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN_ORG')")
    public ResponseEntity<Void> resetAttempts(@PathVariable UUID id, @PathVariable UUID apprenantId) {
        quizService.resetAttempts(id, apprenantId);
        return ResponseEntity.ok().build();
    }
}
