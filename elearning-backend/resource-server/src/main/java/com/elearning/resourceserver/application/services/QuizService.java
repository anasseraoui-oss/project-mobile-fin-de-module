package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.QuizStatus;
import com.elearning.resourceserver.domain.enums.TentativeQuizStatus;
import com.elearning.resourceserver.domain.events.QuizSubmittedEvent;
import com.elearning.resourceserver.domain.events.QuizValidatedEvent;
import com.elearning.resourceserver.domain.dto.QuizRequestDto;
import com.elearning.resourceserver.domain.dto.QuizResponseDto;
import com.elearning.resourceserver.domain.dto.QuizResultDto;
import com.elearning.resourceserver.domain.dto.QuizSubmitDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.QuizSessionExpiredException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.repository.*;
import com.elearning.resourceserver.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final StringRedisTemplate redisTemplate;
    private final QuizRepository quizRepository;
    private final QuizReponseRepository reponseRepository;
    private final TentativeQuizRepository tentativeRepository;
    private final ProgressionRepository progressionRepository;
    private final InscriptionRepository inscriptionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * UC-04: GET quiz definition with all precondition checks
     */
    @Transactional(readOnly = true)
    public QuizResponseDto getQuizDefinition(UUID courseId, UUID userId) {
        Quiz quiz = quizRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun quiz pour ce cours"));

        String role = SecurityUtils.getCurrentUserRole();
        boolean isFormateur = "ROLE_FORMATEUR".equals(role) || "ROLE_ADMIN_ORG".equals(role) || "ROLE_SUPER_ADMIN".equals(role);

        if (!isFormateur) {
            // RB-01: Check progression.isUnlocked
            Progression progression = progressionRepository.findByApprenantIdAndCoursId(userId, courseId)
                    .orElseThrow(() -> new AccessDeniedException("Cours non accessible"));

            if (!progression.getIsUnlocked()) {
                throw new AccessDeniedException("Ce cours n'est pas encore débloqué. Validez le quiz du cours précédent.");
            }

            // RB-02: Check presenceRate >= presenceThreshold
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));

            if (progression.getPresenceRate() < course.getPresenceThreshold()) {
                throw new AccessDeniedException(
                        String.format("Présence insuffisante: %.0f%% / %d%% requis",
                                progression.getPresenceRate(), course.getPresenceThreshold()));
            }

            // RB-07: Check attempts
            int attempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quiz.getId());
            if (attempts >= quiz.getMaxAttempts()) {
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "Tentatives épuisées. Contactez votre formateur.");
            }
        }

        int attempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quiz.getId());
        int remainingAttempts = quiz.getMaxAttempts() - attempts;

        return mapToDto(quiz, remainingAttempts, isFormateur);
    }

    /**
     * UC-04: Start quiz session
     */
    @Transactional
    public Map<String, Object> startQuiz(UUID userId, UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));

        UUID courseId = quiz.getCourse().getId();

        // All checks from getQuizDefinition
        Progression progression = progressionRepository.findByApprenantIdAndCoursId(userId, courseId)
                .orElseThrow(() -> new AccessDeniedException("Cours non accessible"));

        if (!progression.getIsUnlocked()) {
            throw new AccessDeniedException("Ce cours n'est pas encore débloqué.");
        }

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null && progression.getPresenceRate() < course.getPresenceThreshold()) {
            throw new AccessDeniedException(
                    String.format("Présence insuffisante: %.0f%% / %d%% requis",
                            progression.getPresenceRate(), course.getPresenceThreshold()));
        }

        int attempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quizId);
        if (attempts >= quiz.getMaxAttempts()) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Tentatives épuisées. Contactez votre formateur.");
        }

        // Check no active session
        String redisKey = "quiz:session:" + userId + ":" + quizId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new ValidationException("Une session quiz est déjà active");
        }

        // Create TentativeQuiz
        int attemptNumber = attempts + 1;
        TentativeQuiz tentative = new TentativeQuiz();
        tentative.setApprenantId(userId);
        tentative.setQuizId(quizId);
        tentative.setStatus(TentativeQuizStatus.EN_COURS);
        tentative.setAttemptNumber(attemptNumber);
        tentative.setStartedAt(LocalDateTime.now());
        tentativeRepository.save(tentative);

        // Store in Redis with TTL = timeLimit
        int timeLimit = quiz.getTimeLimit() != null ? quiz.getTimeLimit() : 1800; // default 30min
        redisTemplate.opsForValue().set(redisKey, tentative.getId().toString(), timeLimit, TimeUnit.SECONDS);

        Map<String, Object> response = new HashMap<>();
        response.put("tentativeId", tentative.getId());
        response.put("timeLimit", timeLimit);
        response.put("attemptNumber", attemptNumber);
        response.put("remainingAttempts", quiz.getMaxAttempts() - attemptNumber);
        return response;
    }

    /**
     * UC-04: Submit quiz answers (complete scoring + events)
     */
    @Transactional
    public QuizResultDto submitQuiz(UUID userId, UUID quizId, QuizSubmitDto dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));

        String redisKey = "quiz:session:" + userId + ":" + quizId;
        String tentativeIdStr = redisTemplate.opsForValue().get(redisKey);

        TentativeQuiz tentative;

        if (tentativeIdStr == null) {
            // Session expired → find last EN_COURS attempt and fail it
            tentative = tentativeRepository.findFirstByApprenantIdAndQuizIdAndStatus(
                    userId, quizId, TentativeQuizStatus.EN_COURS).orElse(null);
            if (tentative != null) {
                tentative.setScore(BigDecimal.ZERO);
                tentative.setStatus(TentativeQuizStatus.ECHOUEE);
                tentative.setSubmittedAt(LocalDateTime.now());
                tentativeRepository.save(tentative);
            }
            throw new QuizSessionExpiredException("La session du quiz a expiré.");
        } else {
            tentative = tentativeRepository.findById(UUID.fromString(tentativeIdStr))
                    .orElseThrow(() -> new ResourceNotFoundException("Tentative non trouvée"));
        }

        // Calculate score
        int earnedPoints = 0;
        int totalPoints = 0;
        Map<String, String> corrections = new HashMap<>();

        for (QuizQuestion question : quiz.getQuestions()) {
            totalPoints += question.getPoints();
            String userAnswer = dto.getAnswers() != null ? dto.getAnswers().get(question.getId().toString()) : null;

            // Find correct answers for this question
            List<QuizReponse> correctReponses = reponseRepository.findCorrectByQuestionId(question.getId());
            String correctAnswerText = correctReponses.isEmpty() ? "" :
                    correctReponses.stream().map(QuizReponse::getText).reduce((a, b) -> a + ", " + b).orElse("");

            boolean isCorrect = false;
            if (userAnswer != null && !correctReponses.isEmpty()) {
                for (QuizReponse correct : correctReponses) {
                    if (correct.getId().toString().equals(userAnswer) ||
                        correct.getText().equalsIgnoreCase(userAnswer.trim())) {
                        isCorrect = true;
                        break;
                    }
                }
            }

            if (isCorrect) {
                earnedPoints += question.getPoints();
            }
            corrections.put(question.getId().toString(), correctAnswerText);
        }

        BigDecimal score = totalPoints > 0 ?
                BigDecimal.valueOf(earnedPoints * 100.0 / totalPoints).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        TentativeQuizStatus status = score.doubleValue() >= quiz.getPassScore() ?
                TentativeQuizStatus.VALIDEE : TentativeQuizStatus.ECHOUEE;

        // Save attempt
        tentative.setScore(score);
        tentative.setStatus(status);
        tentative.setSubmittedAt(LocalDateTime.now());
        try {
            tentative.setAnswersSnapshot(objectMapper.writeValueAsString(dto.getAnswers()));
        } catch (JsonProcessingException e) {
            tentative.setAnswersSnapshot("{}");
        }
        tentativeRepository.save(tentative);

        // Delete Redis session
        redisTemplate.delete(redisKey);

        UUID coursId = quiz.getCourse().getId();
        UUID formationId = quiz.getCourse().getFormation().getId();

        // Publish QuizSubmittedEvent
        eventPublisher.publishEvent(new QuizSubmittedEvent(
                this, userId, quizId, coursId, formationId, score, status));

        // If VALIDEE → publish QuizValidatedEvent
        if (status == TentativeQuizStatus.VALIDEE) {
            eventPublisher.publishEvent(new QuizValidatedEvent(
                    this, userId, coursId, formationId, score));
        }

        // Build result
        int newAttempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quizId);
        QuizResultDto result = new QuizResultDto();
        result.setScore(score.intValue());
        result.setPassed(status == TentativeQuizStatus.VALIDEE);
        result.setCorrections(corrections);
        result.setAttemptId(tentative.getId());
        result.setRemainingAttempts(quiz.getMaxAttempts() - newAttempts);
        return result;
    }

    @Transactional(readOnly = true)
    public QuizResultDto getAttemptResults(UUID quizId, UUID attemptId, UUID userId) {
        TentativeQuiz tentative = tentativeRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative non trouvée"));

        if (!tentative.getApprenantId().equals(userId)) {
            throw new AccessDeniedException("Accès non autorisé");
        }

        QuizResultDto result = new QuizResultDto();
        result.setScore(tentative.getScore() != null ? tentative.getScore().intValue() : 0);
        result.setPassed(tentative.getStatus() == TentativeQuizStatus.VALIDEE);
        result.setAttemptId(attemptId);
        return result;
    }

    public QuizResponseDto createQuiz(QuizRequestDto quizDto, UUID formateurId) {
        Course course = courseRepository.findById(quizDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));

        if (quizRepository.findByCourseId(quizDto.getCourseId()).isPresent()) {
            throw new ValidationException("Un quiz existe déjà pour ce cours");
        }

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle(quizDto.getTitle() != null ? quizDto.getTitle() : "Quiz - " + course.getTitle());
        quiz.setPassScore(quizDto.getPassScore() != null ? quizDto.getPassScore() : 70);
        quiz.setMaxAttempts(quizDto.getMaxAttempts() != null ? quizDto.getMaxAttempts() : 3);
        quiz.setTimeLimit(quizDto.getTimeLimit());
        quiz.setIsPublished(false);

        Quiz saved = quizRepository.save(quiz);

        QuizResponseDto dto = new QuizResponseDto();
        dto.setId(saved.getId());
        dto.setCourseId(course.getId());
        dto.setTitle(saved.getTitle());
        dto.setPassScore(saved.getPassScore());
        dto.setMaxAttempts(saved.getMaxAttempts());
        dto.setTimeLimit(saved.getTimeLimit());
        return dto;
    }

    /**
     * RB-07 exception: Reset attempts for a specific student (FORMATEUR)
     */
    @Transactional
    public void resetAttempts(UUID quizId, UUID apprenantId) {
        tentativeRepository.deleteByApprenantIdAndQuizId(apprenantId, quizId);
        // Also reset progression quiz status
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz != null) {
            Progression progression = progressionRepository
                    .findByApprenantIdAndCoursId(apprenantId, quiz.getCourse().getId())
                    .orElse(null);
            if (progression != null) {
                progression.setQuizStatus(QuizStatus.NON_COMMENCE);
                progressionRepository.save(progression);
            }
        }
    }

    private QuizResponseDto mapToDto(Quiz quiz, int remainingAttempts, boolean includeAnswers) {
        QuizResponseDto dto = new QuizResponseDto();
        dto.setId(quiz.getId());
        dto.setCourseId(quiz.getCourse().getId());
        dto.setTitle(quiz.getTitle());
        dto.setPassScore(quiz.getPassScore());
        dto.setMaxAttempts(quiz.getMaxAttempts());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setRemainingAttempts(remainingAttempts);

        List<QuizResponseDto.QuestionResponseDto> qDtos = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (QuizQuestion q : quiz.getQuestions()) {
                QuizResponseDto.QuestionResponseDto qd = new QuizResponseDto.QuestionResponseDto();
                qd.setId(q.getId());
                qd.setQuestion(q.getText());
                qd.setType(q.getType().name());
                qd.setPoints(q.getPoints());
                qd.setOrderIndex(q.getOrderIndex());

                // Map reponses (without isCorrect for students)
                if (q.getReponses() != null) {
                    List<Map<String, Object>> reponses = new ArrayList<>();
                    for (QuizReponse r : q.getReponses()) {
                        Map<String, Object> rMap = new HashMap<>();
                        rMap.put("id", r.getId());
                        rMap.put("text", r.getText());
                        if (includeAnswers) {
                            rMap.put("isCorrect", r.getIsCorrect());
                        }
                        reponses.add(rMap);
                    }
                    qd.setReponses(reponses);
                }

                qDtos.add(qd);
            }
        }
        dto.setQuestions(qDtos);
        return dto;
    }
}
