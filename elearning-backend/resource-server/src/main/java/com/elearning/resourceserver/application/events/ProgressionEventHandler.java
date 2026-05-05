package com.elearning.resourceserver.application.events;

import com.elearning.resourceserver.application.services.CertificateService;
import com.elearning.resourceserver.application.services.NotificationService;
import com.elearning.resourceserver.domain.Course;
import com.elearning.resourceserver.domain.Progression;
import com.elearning.resourceserver.domain.Seance;
import com.elearning.resourceserver.domain.events.InscriptionCreatedEvent;
import com.elearning.resourceserver.domain.events.PresenceCreatedEvent;
import com.elearning.resourceserver.domain.events.QuizSubmittedEvent;
import com.elearning.resourceserver.domain.events.QuizValidatedEvent;
import com.elearning.resourceserver.domain.enums.QuizStatus;
import com.elearning.resourceserver.domain.enums.TentativeQuizStatus;
import com.elearning.resourceserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressionEventHandler {

    private final ProgressionRepository progressionRepository;
    private final PresenceRepository presenceRepository;
    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final NotificationService notificationService;
    private final CertificateService certificateService;

    /**
     * RB-08: PresenceCreatedEvent → recalcule presenceRate
     */
    @Async
    @EventListener
    @Transactional
    public void handlePresenceCreated(PresenceCreatedEvent event) {
        log.info("Handling PresenceCreatedEvent for apprenant={} seance={}", event.getApprenantId(), event.getSeanceId());

        UUID apprenantId = event.getApprenantId();
        UUID coursId = event.getCoursId();

        // Count total seances for this course
        long totalSeances = seanceRepository.countByCoursId(coursId);
        if (totalSeances == 0) return;

        // Count presences (PRESENT or RETARD) for this student in this course
        long presentCount = presenceRepository.countPresentByApprenantAndCours(apprenantId, coursId);

        double presenceRate = (presentCount * 100.0) / totalSeances;

        Progression progression = progressionRepository
                .findByApprenantIdAndCoursId(apprenantId, coursId)
                .orElse(null);

        if (progression != null) {
            progression.setPresenceRate(presenceRate);
            progressionRepository.save(progression);

            // Check if can now take quiz
            Course course = courseRepository.findById(coursId).orElse(null);
            if (course != null && presenceRate >= course.getPresenceThreshold()
                    && progression.getQuizStatus() == QuizStatus.NON_COMMENCE) {
                notificationService.sendToUser(apprenantId,
                        "Quiz disponible",
                        "Vous pouvez maintenant passer le quiz du cours '" + course.getTitle() + "'",
                        Map.of("type", "QUIZ_AVAILABLE", "coursId", coursId.toString()));
            }
        }
    }

    /**
     * RB-08: QuizSubmittedEvent → update Progression.quizStatus
     */
    @Async
    @EventListener
    @Transactional
    public void handleQuizSubmitted(QuizSubmittedEvent event) {
        log.info("Handling QuizSubmittedEvent for apprenant={} quiz={} status={}",
                event.getApprenantId(), event.getQuizId(), event.getStatus());

        Progression progression = progressionRepository
                .findByApprenantIdAndCoursId(event.getApprenantId(), event.getCoursId())
                .orElse(null);

        if (progression != null) {
            if (event.getStatus() == TentativeQuizStatus.VALIDEE) {
                progression.setQuizStatus(QuizStatus.VALIDE);
            } else if (event.getStatus() == TentativeQuizStatus.ECHOUEE) {
                progression.setQuizStatus(QuizStatus.ECHOUE);
            }
            progressionRepository.save(progression);
        }
    }

    /**
     * RB-08: QuizValidatedEvent → unlock next course or generate certificate
     */
    @Async
    @EventListener
    @Transactional
    public void handleQuizValidated(QuizValidatedEvent event) {
        log.info("Handling QuizValidatedEvent for apprenant={} cours={}", event.getApprenantId(), event.getCoursId());

        UUID apprenantId = event.getApprenantId();
        UUID coursId = event.getCoursId();
        UUID formationId = event.getFormationId();

        // Mark current course progression as complete
        Progression currentProgression = progressionRepository
                .findByApprenantIdAndCoursId(apprenantId, coursId)
                .orElse(null);

        if (currentProgression != null) {
            currentProgression.setQuizStatus(QuizStatus.VALIDE);
            currentProgression.setCompletionDate(LocalDateTime.now());
            progressionRepository.save(currentProgression);
        }

        // Find current course to get orderIndex
        Course currentCourse = courseRepository.findById(coursId).orElse(null);
        if (currentCourse == null) return;

        // Find next course
        List<Course> allCourses = courseRepository.findByFormationIdOrderByOrderIndex(formationId);
        Course nextCourse = null;
        for (Course c : allCourses) {
            if (c.getOrderIndex() > currentCourse.getOrderIndex()) {
                nextCourse = c;
                break;
            }
        }

        if (nextCourse != null) {
            // Unlock next course
            Progression nextProgression = progressionRepository
                    .findByApprenantIdAndCoursId(apprenantId, nextCourse.getId())
                    .orElse(null);

            if (nextProgression != null) {
                nextProgression.setIsUnlocked(true);
                nextProgression.setUnlockedAt(LocalDateTime.now());
                progressionRepository.save(nextProgression);

                notificationService.sendToUser(apprenantId,
                        "Cours suivant débloqué",
                        "Le cours '" + nextCourse.getTitle() + "' est maintenant accessible !",
                        Map.of("type", "COURSE_UNLOCKED", "coursId", nextCourse.getId().toString()));
            }
        } else {
            // Last course → check and generate certificate
            log.info("Last course completed, checking certificate generation for formation={}", formationId);
            certificateService.checkAndGenerate(apprenantId, formationId);
        }
    }

    /**
     * InscriptionCreatedEvent → create Progression for each course
     */
    @Async
    @EventListener
    @Transactional
    public void handleInscriptionCreated(InscriptionCreatedEvent event) {
        log.info("Handling InscriptionCreatedEvent for apprenant={} formation={}",
                event.getApprenantId(), event.getFormationId());

        UUID apprenantId = event.getApprenantId();
        UUID formationId = event.getFormationId();

        List<Course> courses = courseRepository.findByFormationIdOrderByOrderIndex(formationId);

        boolean isFirst = true;
        for (Course course : courses) {
            // Check if progression already exists
            if (progressionRepository.findByApprenantIdAndCoursId(apprenantId, course.getId()).isPresent()) {
                isFirst = false;
                continue;
            }

            Progression progression = new Progression();
            progression.setApprenantId(apprenantId);
            progression.setCoursId(course.getId());
            progression.setFormationId(formationId);
            progression.setIsUnlocked(isFirst);
            if (isFirst) {
                progression.setUnlockedAt(LocalDateTime.now());
            }
            progressionRepository.save(progression);
            isFirst = false;
        }

        notificationService.sendToUser(apprenantId,
                "Bienvenue !",
                "Bienvenue dans la formation. Votre premier cours est débloqué !",
                Map.of("type", "ENROLLMENT", "formationId", formationId.toString()));
    }
}
