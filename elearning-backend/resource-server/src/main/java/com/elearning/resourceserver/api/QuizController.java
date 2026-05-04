package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.QuizService;
import com.elearning.resourceserver.domain.dto.QuizRequestDto;
import com.elearning.resourceserver.domain.dto.QuizResponseDto;
import com.elearning.resourceserver.domain.dto.QuizResultDto;
import com.elearning.resourceserver.domain.dto.QuizSubmitDto;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/courses/{courseId}/quiz")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<QuizResponseDto> getQuizDefinition(@PathVariable UUID courseId) {
        return ResponseEntity.ok(quizService.getQuizDefinition(courseId, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/quizzes/{quizId}/start")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<Map<String, String>> startQuizSession(@PathVariable UUID quizId) {
        quizService.startQuiz(SecurityUtils.getCurrentUserId(), quizId);
        return ResponseEntity.ok(Map.of("message", "Quiz session started. Timer is running."));
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
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<QuizResponseDto> createQuiz(@RequestBody QuizRequestDto quizDto) {
        return ResponseEntity.ok(quizService.createQuiz(quizDto, SecurityUtils.getCurrentUserId()));
    }
}
