package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Quiz;
import com.elearning.resourceserver.domain.QuizAttempt;
import com.elearning.resourceserver.domain.QuizQuestion;
import com.elearning.resourceserver.domain.User;
import com.elearning.resourceserver.domain.dto.QuizResponseDto;
import com.elearning.resourceserver.domain.dto.QuizResultDto;
import com.elearning.resourceserver.domain.dto.QuizSubmitDto;
import com.elearning.resourceserver.exceptions.QuizSessionExpiredException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.repository.EnrollmentRepository;
import com.elearning.resourceserver.repository.ProgressRepository;
import com.elearning.resourceserver.repository.QuizAttemptRepository;
import com.elearning.resourceserver.repository.QuizRepository;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final StringRedisTemplate redisTemplate;
    private final CertificateService certificateService;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public QuizResponseDto getQuizDefinition(UUID courseId, UUID userId) {
        Quiz quiz = quizRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun quiz pour ce cours"));

        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_APPRENANT".equals(role)) {
            UUID formationId = quiz.getCourse().getFormation().getId();
            if (!enrollmentRepository.existsByUserIdAndFormationId(userId, formationId)) {
                throw new ValidationException("Inscription requise");
            }
        }

        int attempts = attemptRepository.countByUserIdAndQuizId(userId, quiz.getId());
        int remainingAttempts = quiz.getMaxAttempts() - attempts;

        if ("ROLE_APPRENANT".equals(role) && remainingAttempts <= 0) {
            throw new ValidationException("Nombre maximum de tentatives atteint");
        }

        return mapToDto(quiz, remainingAttempts, "ROLE_FORMATEUR".equals(role) || "ROLE_SUPER_ADMIN".equals(role));
    }

    public void startQuiz(UUID userId, UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));
        
        int attempts = attemptRepository.countByUserIdAndQuizId(userId, quizId);
        if (attempts >= quiz.getMaxAttempts()) {
            throw new ValidationException("Nombre maximum de tentatives atteint");
        }

        String redisKey = "quizSession:" + userId + ":" + quizId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new ValidationException("Une session quiz est déjà active");
        }
        
        redisTemplate.opsForValue().set(redisKey, "ACTIVE", quiz.getTimerSeconds(), TimeUnit.SECONDS);
    }

    @Transactional
    public QuizResultDto submitQuiz(UUID userId, UUID quizId, QuizSubmitDto dto) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));
        String redisKey = "quizSession:" + userId + ":" + quizId;
        
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            throw new QuizSessionExpiredException("La session du quiz a expiré ou n'a jamais été commencée.");
        }
        
        int score = 0;
        int totalPoints = 0;
        Map<String, String> corrections = new HashMap<>();

        for (QuizQuestion q : quiz.getQuestions()) {
            totalPoints += q.getPoints();
            String userAnswer = dto.getAnswers().get(q.getId().toString());
            
            // Clean quotes if present due to JSON
            String cleanCorrect = q.getCorrectAnswer().replaceAll("^\"|\"$", "");
            String cleanUser = userAnswer != null ? userAnswer.replaceAll("^\"|\"$", "") : "";

            if (cleanCorrect.equalsIgnoreCase(cleanUser)) {
                score += q.getPoints();
            }
            corrections.put(q.getId().toString(), cleanCorrect);
        }

        int finalScore = (int) (((double) score / totalPoints) * 100);
        boolean passed = finalScore >= quiz.getPassScore();

        // Enregistrer la tentative
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(userRepository.getReferenceById(userId));
        attempt.setQuiz(quiz);
        attempt.setScore(finalScore);
        attempt.setPassed(passed);
        try {
            attempt.setAnswers(objectMapper.writeValueAsString(dto.getAnswers()));
        } catch (JsonProcessingException e) {
            attempt.setAnswers("{}");
        }
        attemptRepository.save(attempt);
        redisTemplate.delete(redisKey);

        if (passed) {
            UUID courseId = quiz.getCourse().getId();
            long totalSeances = quiz.getCourse().getSeances().size();
            long completedSeances = progressRepository.countCompletedByUserIdAndCourseId(userId, courseId);
            
            if (completedSeances >= totalSeances) {
                User user = userRepository.getReferenceById(userId);
                try {
                    certificateService.generateAndUploadCertificate(user, quiz.getCourse().getFormation(), finalScore);
                } catch (Exception e) {
                    // Log uniquement pour ne pas faire échouer le quiz
                }
            }
        }

        int newAttemptsCount = attemptRepository.countByUserIdAndQuizId(userId, quizId);
        
        QuizResultDto result = new QuizResultDto();
        result.setScore(finalScore);
        result.setPassed(passed);
        result.setCorrections(corrections);
        result.setAttemptId(attempt.getId());
        result.setRemainingAttempts(quiz.getMaxAttempts() - newAttemptsCount);
        
        return result;
    }

    private QuizResponseDto mapToDto(Quiz quiz, int remainingAttempts, boolean includeAnswers) {
        QuizResponseDto dto = new QuizResponseDto();
        dto.setId(quiz.getId());
        dto.setCourseId(quiz.getCourse().getId());
        dto.setPassScore(quiz.getPassScore());
        dto.setMaxAttempts(quiz.getMaxAttempts());
        dto.setTimerSeconds(quiz.getTimerSeconds());
        dto.setRemainingAttempts(remainingAttempts);

        List<QuizResponseDto.QuestionResponseDto> qDtos = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (QuizQuestion q : quiz.getQuestions()) {
                QuizResponseDto.QuestionResponseDto qd = new QuizResponseDto.QuestionResponseDto();
                qd.setId(q.getId());
                qd.setQuestion(q.getQuestion());
                qd.setType(q.getType());
                qd.setOptions(q.getOptions());
                qd.setPoints(q.getPoints());
                qd.setOrderIndex(q.getOrderIndex());
                if (includeAnswers) {
                    qd.setCorrectAnswer(q.getCorrectAnswer());
                }
                qDtos.add(qd);
            }
        }
        dto.setQuestions(qDtos);
        return dto;
    }
}
