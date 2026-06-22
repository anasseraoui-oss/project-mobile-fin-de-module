package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.QuizStatus;
import com.elearning.resourceserver.domain.enums.QuizQuestionType;
import com.elearning.resourceserver.domain.enums.TentativeQuizStatus;
import com.elearning.resourceserver.domain.events.QuizSubmittedEvent;
import com.elearning.resourceserver.domain.events.QuizValidatedEvent;
import com.elearning.resourceserver.domain.dto.QuizRequestDto;
import com.elearning.resourceserver.domain.dto.QuizResponseDto;
import com.elearning.resourceserver.domain.dto.QuizResultDto;
import com.elearning.resourceserver.domain.dto.QuizSubmitDto;
import com.elearning.resourceserver.domain.dto.QuizHistoryItemDto;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.QuizSessionExpiredException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.repository.*;
import com.elearning.resourceserver.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
    private final CertificatRepository certificatRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

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
                throw new AccessDeniedException("Ce cours n'est pas encore dÃ©bloquÃ©. Validez le quiz du cours prÃ©cÃ©dent.");
            }

            // RB-02: Check presenceRate >= presenceThreshold
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvÃ©"));

            if (progression.getPresenceRate() < course.getPresenceThreshold()) {
                throw new AccessDeniedException(
                        String.format("PrÃ©sence insuffisante: %.0f%% / %d%% requis",
                                progression.getPresenceRate(), course.getPresenceThreshold()));
            }

            // RB-07: Check attempts
            int attempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quiz.getId());
            if (attempts >= quiz.getMaxAttempts()) {
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "Tentatives Ã©puisÃ©es. Contactez votre formateur.");
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
            throw new AccessDeniedException("Ce cours n'est pas encore dÃ©bloquÃ©.");
        }

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null && progression.getPresenceRate() < course.getPresenceThreshold()) {
            throw new AccessDeniedException(
                    String.format("PrÃ©sence insuffisante: %.0f%% / %d%% requis",
                            progression.getPresenceRate(), course.getPresenceThreshold()));
        }

        int attempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quizId);
        if (attempts >= quiz.getMaxAttempts()) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Tentatives Ã©puisÃ©es. Contactez votre formateur.");
        }

        // Check no active session
        String redisKey = "quiz:session:" + userId + ":" + quizId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new ValidationException("Une session quiz est dÃ©jÃ  active");
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
        TentativeQuiz tentative = resolveTentativeForSubmit(userId, quizId, dto, tentativeIdStr);

        if (tentative.getStatus() != TentativeQuizStatus.EN_COURS) {
            return buildResultFromAttempt(quiz, tentative);
        }

        int earnedPoints = 0;
        int totalPoints = 0;
        Map<String, String> corrections = new HashMap<>();

        for (QuizQuestion question : quiz.getQuestions()) {
            totalPoints += question.getPoints();
            String userAnswer = dto.getAnswers() != null ? dto.getAnswers().get(question.getId().toString()) : null;

            List<QuizReponse> correctReponses = reponseRepository.findCorrectByQuestionId(question.getId());
            String correctAnswerText = correctReponses.isEmpty() ? "" :
                    correctReponses.stream().map(QuizReponse::getText).reduce((a, b) -> a + ", " + b).orElse("");

            if (isAnswerCorrect(userAnswer, correctReponses)) {
                earnedPoints += question.getPoints();
            }
            corrections.put(question.getId().toString(), correctAnswerText);
        }

        BigDecimal score = totalPoints > 0 ?
                BigDecimal.valueOf(earnedPoints * 100.0 / totalPoints).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        TentativeQuizStatus status = score.doubleValue() >= quiz.getPassScore() ?
                TentativeQuizStatus.VALIDEE : TentativeQuizStatus.ECHOUEE;

        tentative.setScore(score);
        tentative.setStatus(status);
        tentative.setSubmittedAt(LocalDateTime.now());
        try {
            tentative.setAnswersSnapshot(objectMapper.writeValueAsString(dto.getAnswers()));
        } catch (JsonProcessingException e) {
            tentative.setAnswersSnapshot("{}");
        }
        tentativeRepository.save(tentative);
        redisTemplate.delete(redisKey);

        UUID coursId = quiz.getCourse().getId();
        UUID formationId = quiz.getCourse().getFormation().getId();

        eventPublisher.publishEvent(new QuizSubmittedEvent(
                this, userId, quizId, coursId, formationId, score, status));

        if (status == TentativeQuizStatus.VALIDEE) {
            eventPublisher.publishEvent(new QuizValidatedEvent(
                    this, userId, coursId, formationId, score));
        }

        int newAttempts = tentativeRepository.countByApprenantIdAndQuizId(userId, quizId);
        QuizResultDto result = new QuizResultDto();
        result.setScore(score.intValue());
        result.setPassed(status == TentativeQuizStatus.VALIDEE);
        result.setCorrections(corrections);
        result.setAttemptId(tentative.getId());
        result.setRemainingAttempts(Math.max(0, quiz.getMaxAttempts() - newAttempts));
        return result;
    }

    @Transactional(readOnly = true)
    public QuizResultDto getAttemptResults(UUID quizId, UUID attemptId, UUID userId) {
        TentativeQuiz tentative = tentativeRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative non trouvÃ©e"));

        if (!tentative.getApprenantId().equals(userId)) {
            throw new AccessDeniedException("AccÃ¨s non autorisÃ©");
        }

        QuizResultDto result = new QuizResultDto();
        result.setScore(tentative.getScore() != null ? tentative.getScore().intValue() : 0);
        result.setPassed(tentative.getStatus() == TentativeQuizStatus.VALIDEE);
        result.setAttemptId(attemptId);
        return result;
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryItemDto> getQuizHistory(UUID userId) {
        return tentativeRepository.findCompletedHistoryByApprenantId(userId).stream()
                .map(tentative -> {
                    Quiz quiz = tentative.getQuiz();
                    Course course = quiz != null ? quiz.getCourse() : null;
                    Formation formation = course != null ? course.getFormation() : null;
                    UUID formationId = formation != null ? formation.getId() : null;
                    boolean certificateAvailable = formationId != null
                            && certificatRepository.existsByApprenantIdAndFormationId(userId, formationId);

                    return QuizHistoryItemDto.builder()
                            .attemptId(tentative.getId())
                            .quizId(tentative.getQuizId())
                            .quizTitle(quiz != null ? quiz.getTitle() : "Quiz")
                            .courseId(course != null ? course.getId() : null)
                            .courseTitle(course != null ? course.getTitle() : null)
                            .formationId(formationId)
                            .formationTitle(formation != null ? formation.getTitle() : null)
                            .submittedAt(tentative.getSubmittedAt())
                            .score(tentative.getScore())
                            .status(tentative.getStatus())
                            .attemptNumber(tentative.getAttemptNumber())
                            .passed(tentative.getStatus() == TentativeQuizStatus.VALIDEE)
                            .certificateAvailable(certificateAvailable)
                            .build();
                })
                .toList();
    }

    public QuizResponseDto createQuiz(QuizRequestDto quizDto, UUID formateurId) {
        Course course = courseRepository.findById(quizDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvÃ©"));
        ensureCanManageCourse(course, formateurId);

        if (quizRepository.findByCourseId(quizDto.getCourseId()).isPresent()) {
            throw new ValidationException("Un quiz existe dÃ©jÃ  pour ce cours");
        }

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle(quizDto.getTitle() != null ? quizDto.getTitle() : "Quiz - " + course.getTitle());
        quiz.setPassScore(quizDto.getPassScore() != null ? quizDto.getPassScore() : 70);
        quiz.setMaxAttempts(quizDto.getMaxAttempts() != null ? quizDto.getMaxAttempts() : 3);
        quiz.setTimeLimit(quizDto.getTimeLimit());
        quiz.setIsPublished(false);
        quiz.setQuestions(buildQuestions(quiz, quizDto.getQuestions()));

        Quiz saved = quizRepository.save(quiz);
        return mapToDto(saved, saved.getMaxAttempts(), true);
    }

    public QuizResponseDto updateQuiz(UUID quizId, QuizRequestDto quizDto, UUID formateurId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz non trouvÃ©"));
        ensureCanManageCourse(quiz.getCourse(), formateurId);

        if (quizDto.getTitle() != null && !quizDto.getTitle().isBlank()) quiz.setTitle(quizDto.getTitle());
        if (quizDto.getPassScore() != null) quiz.setPassScore(quizDto.getPassScore());
        if (quizDto.getMaxAttempts() != null) quiz.setMaxAttempts(quizDto.getMaxAttempts());
        if (quizDto.getTimeLimit() != null) quiz.setTimeLimit(quizDto.getTimeLimit());
        if (quizDto.getQuestions() != null) {
            deleteQuestions(quiz.getId());
            quiz.setQuestions(buildQuestions(quiz, quizDto.getQuestions()));
        }
        Quiz saved = quizRepository.save(quiz);
        return mapToDto(saved, saved.getMaxAttempts(), true);
    }

    public void deleteQuiz(UUID quizId, UUID formateurId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz non trouvÃ©"));
        ensureCanManageCourse(quiz.getCourse(), formateurId);
        entityManager.createNativeQuery("DELETE FROM quiz_attempts WHERE quiz_id = :id")
                .setParameter("id", quizId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentatives_quiz WHERE quiz_id = :id")
                .setParameter("id", quizId)
                .executeUpdate();
        deleteQuestions(quizId);
        quizRepository.delete(quiz);
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
    private TentativeQuiz resolveTentativeForSubmit(UUID userId, UUID quizId, QuizSubmitDto dto, String redisTentativeId) {
        TentativeQuiz tentative = null;

        if (dto != null && dto.getTentativeId() != null) {
            tentative = tentativeRepository.findById(dto.getTentativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tentative non trouvee"));
        } else if (redisTentativeId != null && !redisTentativeId.isBlank()) {
            tentative = tentativeRepository.findById(UUID.fromString(redisTentativeId))
                    .orElseThrow(() -> new ResourceNotFoundException("Tentative non trouvee"));
        }

        if (tentative == null) {
            TentativeQuiz active = tentativeRepository.findFirstByApprenantIdAndQuizIdAndStatus(
                    userId, quizId, TentativeQuizStatus.EN_COURS).orElse(null);
            if (active != null) {
                active.setScore(BigDecimal.ZERO);
                active.setStatus(TentativeQuizStatus.ECHOUEE);
                active.setSubmittedAt(LocalDateTime.now());
                tentativeRepository.save(active);
                throw new QuizSessionExpiredException("La session du quiz a expire.");
            }
            return tentativeRepository.findFirstByApprenantIdAndQuizIdOrderByAttemptNumberDesc(userId, quizId)
                    .filter(saved -> saved.getStatus() != TentativeQuizStatus.EN_COURS)
                    .orElseThrow(() -> new QuizSessionExpiredException("La session du quiz a expire."));
        }

        if (!tentative.getApprenantId().equals(userId) || !tentative.getQuizId().equals(quizId)) {
            throw new AccessDeniedException("Tentative quiz non autorisee");
        }
        return tentative;
    }

    private QuizResultDto buildResultFromAttempt(Quiz quiz, TentativeQuiz tentative) {
        int attempts = tentativeRepository.countByApprenantIdAndQuizId(tentative.getApprenantId(), quiz.getId());
        QuizResultDto result = new QuizResultDto();
        result.setScore(tentative.getScore() != null ? tentative.getScore().intValue() : 0);
        result.setPassed(tentative.getStatus() == TentativeQuizStatus.VALIDEE);
        result.setAttemptId(tentative.getId());
        result.setRemainingAttempts(Math.max(0, quiz.getMaxAttempts() - attempts));
        result.setCorrections(Map.of());
        return result;
    }

    private boolean isAnswerCorrect(String userAnswer, List<QuizReponse> correctReponses) {
        Set<String> submitted = parseSubmittedAnswers(userAnswer);
        if (submitted.isEmpty() || correctReponses == null || correctReponses.isEmpty()) {
            return false;
        }

        Set<String> correctIds = new HashSet<>();
        Set<String> correctTexts = new HashSet<>();
        Set<String> allCorrectTokens = new HashSet<>();
        for (QuizReponse correct : correctReponses) {
            String id = normalizeAnswerToken(correct.getId().toString());
            String text = normalizeAnswerToken(correct.getText());
            correctIds.add(id);
            correctTexts.add(text);
            allCorrectTokens.add(id);
            allCorrectTokens.add(text);
        }

        return submitted.equals(correctIds)
                || submitted.equals(correctTexts)
                || (submitted.size() == correctIds.size() && submitted.stream().allMatch(allCorrectTokens::contains));
    }

    private Set<String> parseSubmittedAnswers(String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return Set.of();
        }
        try {
            Object parsed = objectMapper.readValue(userAnswer, Object.class);
            if (parsed instanceof List<?> list) {
                Set<String> answers = new HashSet<>();
                for (Object item : list) {
                    String token = normalizeAnswerToken(String.valueOf(item));
                    if (!token.isBlank()) answers.add(token);
                }
                return answers;
            }
            return Set.of(normalizeAnswerToken(String.valueOf(parsed)));
        } catch (Exception ignored) {
            Set<String> answers = new HashSet<>();
            for (String item : userAnswer.split(",")) {
                String token = normalizeAnswerToken(item);
                if (!token.isBlank()) answers.add(token);
            }
            return answers;
        }
    }

    private String normalizeAnswerToken(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private List<QuizQuestion> buildQuestions(Quiz quiz, List<QuizRequestDto.QuestionDto> questionDtos) {
        if (questionDtos == null || questionDtos.isEmpty()) {
            throw new ValidationException("Un quiz doit contenir au moins une question");
        }
        List<QuizQuestion> questions = new ArrayList<>();
        int index = 0;
        for (QuizRequestDto.QuestionDto questionDto : questionDtos) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setText(questionDto.getQuestion());
            question.setType(parseQuestionType(questionDto.getType()));
            question.setPoints(questionDto.getPoints() != null ? questionDto.getPoints() : 1);
            question.setOrderIndex(questionDto.getOrderIndex() != null ? questionDto.getOrderIndex() : index++);

            List<String> options = parseStringList(questionDto.getOptions());
            Set<String> correctAnswers = new HashSet<>(parseStringList(questionDto.getCorrectAnswer()));
            if (options.isEmpty() && !correctAnswers.isEmpty()) {
                options = new ArrayList<>(correctAnswers);
            }
            if (options.isEmpty()) {
                throw new ValidationException("Chaque question doit contenir au moins une option");
            }
            List<QuizReponse> responses = new ArrayList<>();
            for (String option : options) {
                QuizReponse response = new QuizReponse();
                response.setQuestion(question);
                response.setText(option);
                response.setIsCorrect(correctAnswers.contains(option));
                responses.add(response);
            }
            if (responses.stream().noneMatch(response -> Boolean.TRUE.equals(response.getIsCorrect()))) {
                responses.get(0).setIsCorrect(true);
            }
            question.setReponses(responses);
            questions.add(question);
        }
        return questions;
    }

    private QuizQuestionType parseQuestionType(String value) {
        if (value == null || value.isBlank()) {
            return QuizQuestionType.QCM;
        }
        return switch (value.trim().toUpperCase()) {
            case "MCQ", "MULTIPLE_CHOICE", "QCM" -> QuizQuestionType.QCM;
            case "TRUE_FALSE", "VRAI_FAUX" -> QuizQuestionType.VRAI_FAUX;
            case "SHORT", "OPEN", "REPONSE_COURTE" -> QuizQuestionType.REPONSE_COURTE;
            default -> throw new ValidationException("Type de question invalide : " + value);
        };
    }

    private List<String> parseStringList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            Object parsed = objectMapper.readValue(value, Object.class);
            if (parsed instanceof List<?> list) {
                return list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList();
            }
            return List.of(String.valueOf(parsed));
        } catch (Exception ignored) {
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
    }

    private void deleteQuestions(UUID quizId) {
        entityManager.createNativeQuery("DELETE FROM quiz_reponses WHERE question_id IN (SELECT id FROM quiz_questions WHERE quiz_id = :id)")
                .setParameter("id", quizId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM quiz_questions WHERE quiz_id = :id")
                .setParameter("id", quizId)
                .executeUpdate();
        entityManager.flush();
    }

    private void ensureCanManageCourse(Course course, UUID requesterId) {
        Formation formation = course.getFormation();
        if (Objects.equals(formation.getFormateurId(), requesterId)) {
            return;
        }
        if ("ROLE_ADMIN_ORG".equals(SecurityUtils.getCurrentUserRole())) {
            UUID organisationId = SecurityUtils.getCurrentOrganisationId();
            if (organisationId == null) {
                organisationId = userRepository.findById(requesterId).map(User::getOrganisationId).orElse(null);
            }
            if (Objects.equals(formation.getOrganisationId(), organisationId)) {
                return;
            }
        }
        throw new AccessDeniedException("Vous n'Ãªtes pas autorisÃ© Ã  gÃ©rer ce quiz");
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


