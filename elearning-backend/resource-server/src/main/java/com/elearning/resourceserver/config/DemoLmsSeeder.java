package com.elearning.resourceserver.config;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.*;
import com.elearning.resourceserver.infrastructure.minio.MinioService;
import com.elearning.resourceserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Profile({"demo", "dev"})
@RequiredArgsConstructor
public class DemoLmsSeeder implements CommandLineRunner {

    private static final String MEDIA_BUCKET = "elearning-media";
    private static final String PUBLIC_BUCKET = "elearning-public";
    private static final String UPLOADS_BUCKET = "elearning-uploads";
    private static final String DEMO_PASSWORD = "Demo@1234";
    private static final int TARGET_INSTRUCTORS = 20;
    private static final int TARGET_STUDENTS = 100;
    private static final int TARGET_FORMATIONS = 30;
    private static final int TARGET_COURSES = 200;
    private static final int TARGET_SEANCES = 500;
    private static final int TARGET_VIDEO_RESOURCES = 100;
    private static final int TARGET_PDF_RESOURCES = 100;
    private static final List<String> CATEGORY_IDS = List.of(
            "backend", "devops", "mobile", "frontend", "cloud",
            "data", "security", "ai", "design", "business"
    );
    private static final UUID SUPER_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111001");
    private static final UUID SARA_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");
    private static final UUID YOUSSEF_ID = UUID.fromString("11111111-1111-1111-1111-111111111201");
    private static final UUID LINA_ID = UUID.fromString("11111111-1111-1111-1111-111111111202");
    private static final UUID ADMIN_ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111301");
    private static final UUID TECH_ACADEMY_ORG_ID = UUID.fromString("0a12b687-ea69-4000-b35b-a1b3feae285e");
    private static final UUID CLOUD_SKILLS_ORG_ID = UUID.fromString("22222222-2222-2222-2222-222222222002");

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final FormationRepository formationRepository;
    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final QuizRepository quizRepository;
    private final QuizReponseRepository quizReponseRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ProgressionRepository progressionRepository;
    private final ProgressRepository progressRepository;
    private final CertificatRepository certificatRepository;
    private final PedagogicalResourceRepository resourceRepository;
    private final MinioService minioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (hasCompleteDemoData()) {
            log.info("DemoLmsSeeder: requested demo LMS volume already installed");
            return;
        }

        ensureBuckets();

        Organisation techAcademy = organisation(TECH_ACADEMY_ORG_ID, "TechAcademy Maroc", "techacademy-maroc", "Technologie", true);
        Organisation cloudSkills = organisation(CLOUD_SKILLS_ORG_ID, "CloudSkills Africa", "cloudskills-africa", "Cloud & Mobile", false);

        user(SUPER_ADMIN_ID, "admin@demo.lms", "Admin", "Demo", Role.SUPER_ADMIN, null);
        user(ADMIN_ORG_ID, "admin.org@demo.lms", "Nadia", "El Amrani", Role.ADMIN_ORG, techAcademy.getId());
        List<User> instructors = seedInstructors(techAcademy.getId());
        List<User> learners = seedStudents();
        List<User> learnersForProgress = learners.subList(0, Math.min(10, learners.size()));
        User sara = instructors.get(0);

        List<FormationPlan> plans = new ArrayList<>(List.of(
                new FormationPlan("Spring Boot 3 Backend Professionnel", "demo-spring-boot", FormationLevel.INTERMEDIAIRE, techAcademy, sara,
                        "APIs REST, sécurité OAuth2, PostgreSQL, Redis et MinIO pour construire un backend LMS complet."),
                new FormationPlan("Docker pour Développeurs", "demo-docker", FormationLevel.DEBUTANT, cloudSkills, sara,
                        "Images, conteneurs, volumes, réseaux et bonnes pratiques de packaging applicatif."),
                new FormationPlan("Kubernetes Fondamentaux", "demo-kubernetes", FormationLevel.INTERMEDIAIRE, cloudSkills, sara,
                        "Déploiement, services, ingress, configmaps, secrets et observabilité sur Kubernetes."),
                new FormationPlan("DevOps CI/CD Production", "demo-devops", FormationLevel.AVANCE, cloudSkills, sara,
                        "Pipelines, quality gates, déploiements progressifs, monitoring et rollback."),
                new FormationPlan("Flutter Mobile Apps", "demo-flutter", FormationLevel.DEBUTANT, techAcademy, sara,
                        "Création d'applications mobiles multiplateformes avec Flutter, widgets et état."),
                new FormationPlan("Android Kotlin Offline-First", "demo-android", FormationLevel.INTERMEDIAIRE, techAcademy, sara,
                        "Compose, Room, Retrofit, DataStore, WorkManager et synchronisation offline-first."),
                new FormationPlan("React Native Mobile", "demo-react-native", FormationLevel.DEBUTANT, techAcademy, sara,
                        "Composants, navigation, appels API et publication d'applications React Native.")
        ));
        addGeneratedFormationPlans(plans, techAcademy, cloudSkills, instructors);

        int formationIndex = 0;
        for (FormationPlan plan : plans) {
            Formation formation = createFormation(plan, formationIndex++);
            createLearningPath(formation, plan.formateur().getId(), learnersForProgress, formationIndex - 1);
        }

        createCertificate(learners.get(0), plans.get(0).organisation(), findFormation("demo-spring-boot"));

        log.info("DemoLmsSeeder: installed/updated demo data: {} formations, {} modules, {} sessions, {} quizzes",
                formationRepository.count(), courseRepository.count(), seanceRepository.count(), quizRepository.count());
        log.info("DemoLmsSeeder: accounts: admin@demo.lms, admin.org@demo.lms, sara.formateur@demo.lms, youssef.apprenant@demo.lms / {}",
                DEMO_PASSWORD);
    }

    private boolean hasCompleteDemoData() {
        return userRepository.count() >= TARGET_INSTRUCTORS + TARGET_STUDENTS + 2L
                && formationRepository.count() >= TARGET_FORMATIONS
                && courseRepository.count() >= TARGET_COURSES
                && seanceRepository.count() >= TARGET_SEANCES
                && quizRepository.count() >= TARGET_COURSES
                && resourceRepository.count() >= TARGET_VIDEO_RESOURCES + TARGET_PDF_RESOURCES;
    }

    private List<User> seedInstructors(UUID organisationId) {
        List<User> instructors = new ArrayList<>();
        instructors.add(user(SARA_ID, "sara.formateur@demo.lms", "Sara", "Chraibi", Role.FORMATEUR, organisationId));
        for (int i = 2; i <= TARGET_INSTRUCTORS; i++) {
            instructors.add(user(demoUuid("instructor-" + i), "instructor%02d@demo.lms".formatted(i),
                    "Instructor", "%02d".formatted(i), Role.FORMATEUR, organisationId));
        }
        return instructors;
    }

    private List<User> seedStudents() {
        List<User> learners = new ArrayList<>();
        learners.add(user(YOUSSEF_ID, "youssef.apprenant@demo.lms", "Youssef", "Mansouri", Role.APPRENANT, null));
        learners.add(user(LINA_ID, "lina.apprenante@demo.lms", "Lina", "Berrada", Role.APPRENANT, null));
        for (int i = 3; i <= TARGET_STUDENTS; i++) {
            learners.add(user(demoUuid("student-" + i), "student%03d@demo.lms".formatted(i),
                    "Student", "%03d".formatted(i), Role.APPRENANT, null));
        }
        return learners;
    }

    private void addGeneratedFormationPlans(List<FormationPlan> plans, Organisation techAcademy, Organisation cloudSkills, List<User> instructors) {
        String[] topics = {
                "Data Engineering", "Cybersecurity Operations", "AI Productivity",
                "Product Design", "Business Analytics", "Cloud Architecture",
                "Frontend Engineering", "Mobile Quality", "Backend Testing", "DevOps Observability"
        };
        while (plans.size() < TARGET_FORMATIONS) {
            int index = plans.size();
            String topic = topics[index % topics.length];
            String slug = "demo-scale-formation-%02d".formatted(index + 1);
            Organisation organisation = index % 2 == 0 ? techAcademy : cloudSkills;
            User instructor = instructors.get(index % instructors.size());
            FormationLevel level = switch (index % 3) {
                case 0 -> FormationLevel.DEBUTANT;
                case 1 -> FormationLevel.INTERMEDIAIRE;
                default -> FormationLevel.AVANCE;
            };
            plans.add(new FormationPlan(
                    topic + " Parcours " + (index + 1),
                    slug,
                    level,
                    organisation,
                    instructor,
                    "Parcours pratique avec modules, sessions, medias et quiz pour tests de charge fonctionnels."
            ));
        }
    }

    private void createLearningPath(Formation formation, UUID formateurId, List<User> learners, int formationIndex) {
        if (formationIndex >= 0) {
            int targetCourseCount = formationIndex < 20 ? 7 : 6;
            for (int c = 0; c < targetCourseCount; c++) {
                int globalCourseIndex = globalCourseIndex(formationIndex, c);
                Course course = createOrUpdateCourse(formation, c + 1, globalCourseIndex);
                int targetSeanceCount = globalCourseIndex < 100 ? 3 : 2;
                for (int s = 0; s < targetSeanceCount; s++) {
                    createSeanceWithAssets(formation, course, formateurId, s + 1, globalSeanceIndex(globalCourseIndex, s));
                }
                createQuiz(course);
            }

            for (User learner : learners) {
                if (!inscriptionRepository.existsByApprenantIdAndFormationId(learner.getId(), formation.getId())) {
                    Inscription inscription = new Inscription();
                    inscription.setApprenantId(learner.getId());
                    inscription.setFormationId(formation.getId());
                    inscription.setStatus(InscriptionStatus.EN_COURS);
                    inscriptionRepository.save(inscription);
                }
                createProgressionsAndWatchState(learner, formation);
            }
            return;
        }

        String[][] courseTitles = {
                {"Fondations", "Architecture et environnement", "Concepts clés et installation"},
                {"APIs et données", "Modélisation et services", "DTOs, validation et persistance"},
                {"Production", "Sécurité, observabilité et déploiement", "Bonnes pratiques pour une démo réaliste"}
        };

        for (int c = 0; c < courseTitles.length; c++) {
            Course course = new Course();
            course.setFormation(formation);
            course.setTitle(courseTitles[c][0] + " - " + formation.getTitle());
            course.setDescription(courseTitles[c][1] + ". " + courseTitles[c][2] + ".");
            course.setOrderIndex(c + 1);
            course.setStatus(c == 0 ? CoursStatus.EN_COURS : CoursStatus.A_VENIR);
            course.setPresenceThreshold(70);
            course.setQuizPassScore(70);
            course.setEstimatedDuration(45 + (c * 20));
            course = courseRepository.save(course);

            for (int s = 0; s < 4; s++) {
                createSeanceWithAssets(formation, course, formateurId, s + 1);
            }
            createQuiz(course);
        }

        for (User learner : learners) {
            if (!inscriptionRepository.existsByApprenantIdAndFormationId(learner.getId(), formation.getId())) {
                Inscription inscription = new Inscription();
                inscription.setApprenantId(learner.getId());
                inscription.setFormationId(formation.getId());
                inscription.setStatus(InscriptionStatus.EN_COURS);
                inscriptionRepository.save(inscription);
            }
            createProgressionsAndWatchState(learner, formation);
        }
    }

    private Course createOrUpdateCourse(Formation formation, int orderIndex, int globalCourseIndex) {
        Course course = courseRepository.findByFormationIdOrderByOrderIndex(formation.getId()).stream()
                .filter(existing -> Integer.valueOf(orderIndex).equals(existing.getOrderIndex()))
                .findFirst()
                .orElseGet(Course::new);
        course.setFormation(formation);
        course.setTitle("Module " + orderIndex + " - " + formation.getTitle());
        course.setDescription("Objectifs, demonstrations et exercices pratiques du module " + orderIndex + ".");
        course.setOrderIndex(orderIndex);
        course.setStatus(globalCourseIndex < 100 ? CoursStatus.EN_COURS : CoursStatus.A_VENIR);
        course.setPresenceThreshold(70);
        course.setQuizPassScore(70);
        course.setEstimatedDuration(globalCourseIndex < 100 ? 60 : 45);
        return courseRepository.save(course);
    }

    private int globalCourseIndex(int formationIndex, int courseIndex) {
        return formationIndex < 20
                ? formationIndex * 7 + courseIndex
                : 140 + ((formationIndex - 20) * 6) + courseIndex;
    }

    private int globalSeanceIndex(int globalCourseIndex, int seanceIndex) {
        return globalCourseIndex < 100
                ? globalCourseIndex * 3 + seanceIndex
                : 300 + ((globalCourseIndex - 100) * 2) + seanceIndex;
    }

    private void createSeanceWithAssets(Formation formation, Course course, UUID formateurId, int orderIndex, int globalSeanceIndex) {
        Seance seance = seanceRepository.findByCoursIdOrderByOrderIndex(course.getId()).stream()
                .filter(existing -> Integer.valueOf(orderIndex).equals(existing.getOrderIndex()))
                .findFirst()
                .orElseGet(Seance::new);
        seance.setCourse(course);
        seance.setFormateurId(formateurId);
        seance.setType(SeanceType.ENREGISTREE);
        seance.setTitle("Session " + orderIndex + " - " + course.getTitle());
        seance.setDescription("Session de pratique avec support, media et activite guidee.");
        seance.setOrderIndex(orderIndex);
        seance.setDuration(300 + orderIndex * 180);
        seance.setStatus(SeanceStatus.CONTENU_DISPONIBLE);
        seance = seanceRepository.save(seance);

        String finalBaseKey = "orgs/" + formation.getOrganisationId()
                + "/formations/" + formation.getId()
                + "/courses/" + course.getId()
                + "/seances/" + seance.getId();

        boolean withVideo = globalSeanceIndex < TARGET_VIDEO_RESOURCES;
        boolean withPdf = globalSeanceIndex < TARGET_PDF_RESOURCES;
        boolean withDocument = globalSeanceIndex < 200;
        boolean withImage = globalSeanceIndex % 10 == 0;
        boolean withLink = globalSeanceIndex % 8 == 0;

        if (withVideo) {
            String videoKey = finalBaseKey + "/video/demo.mp4";
            seance.setVideoKey(videoKey);
            uploadDemoVideo(videoKey, formation.getTitle(), seance.getTitle());
            byte[] videoMarker = ("video:" + seance.getId()).getBytes(StandardCharsets.UTF_8);
            resource(formation, course, seance, ResourceType.VIDEO, "Video - " + seance.getTitle(), videoKey, "video/mp4", videoMarker);
        }
        if (withPdf) {
            String pdfKey = finalBaseKey + "/resources/support.pdf";
            byte[] pdf = demoPdf(formation.getTitle(), course.getTitle(), seance.getTitle());
            seance.setPdfKey(pdfKey);
            uploadBytes(pdfKey, pdf, "application/pdf");
            resource(formation, course, seance, ResourceType.PDF, "Support PDF - " + seance.getTitle(), pdfKey, "application/pdf", pdf);
        }
        seanceRepository.save(seance);

        if (withDocument) {
            byte[] document = demoPdf("Document", course.getTitle(), "Fichier complementaire - " + seance.getTitle());
            String documentKey = finalBaseKey + "/resources/document.pdf";
            uploadBytes(documentKey, document, "application/pdf");
            resource(formation, course, seance, ResourceType.DOCUMENT, "Document - " + seance.getTitle(), documentKey, "application/pdf", document);
        }
        if (withImage) {
            byte[] image = png("Image " + (globalSeanceIndex + 1), colorFor(globalSeanceIndex));
            String imageKey = finalBaseKey + "/resources/image.png";
            uploadBytes(imageKey, image, "image/png");
            resource(formation, course, seance, ResourceType.IMAGE, "Image - " + seance.getTitle(), imageKey, "image/png", image);
        }
        if (withLink) {
            linkResource(formation, course, seance, "Lien externe - " + seance.getTitle(),
                    "https://demo.lms/resources/" + seance.getId());
        }
    }

    private void createSeanceWithAssets(Formation formation, Course course, UUID formateurId, int orderIndex) {
        Seance seance = new Seance();
        seance.setCourse(course);
        seance.setFormateurId(formateurId);
        seance.setType(SeanceType.ENREGISTREE);
        seance.setTitle("Séance " + orderIndex + " - " + course.getTitle());
        seance.setDescription("Démonstration guidée, exemples de code et exercices appliqués.");
        seance.setOrderIndex(orderIndex);
        seance.setDuration(300 + orderIndex * 180);
        seance.setStatus(SeanceStatus.CONTENU_DISPONIBLE);

        String baseKey = "orgs/" + formation.getOrganisationId()
                + "/formations/" + formation.getId()
                + "/courses/" + course.getId()
                + "/seances/pending-" + orderIndex;
        seance.setVideoKey(baseKey + "/video/demo.mp4");
        seance.setPdfKey(baseKey + "/resources/support.pdf");
        seance = seanceRepository.save(seance);

        String finalBaseKey = "orgs/" + formation.getOrganisationId()
                + "/formations/" + formation.getId()
                + "/courses/" + course.getId()
                + "/seances/" + seance.getId();
        seance.setVideoKey(finalBaseKey + "/video/demo.mp4");
        seance.setPdfKey(finalBaseKey + "/resources/support.pdf");
        seanceRepository.save(seance);

        uploadDemoVideo(seance.getVideoKey(), formation.getTitle(), seance.getTitle());
        byte[] pdf = demoPdf(formation.getTitle(), course.getTitle(), seance.getTitle());
        uploadBytes(seance.getPdfKey(), pdf, "application/pdf");
        resource(formation, course, seance, ResourceType.PDF, "Support PDF - " + seance.getTitle(), seance.getPdfKey(), "application/pdf", pdf);

        byte[] exercise = demoPdf("Exercices", course.getTitle(), "Travaux pratiques - " + seance.getTitle());
        String exerciseKey = finalBaseKey + "/resources/exercices.pdf";
        uploadBytes(exerciseKey, exercise, "application/pdf");
        resource(formation, course, seance, ResourceType.EXERCISE, "Exercices - " + seance.getTitle(), exerciseKey, "application/pdf", exercise);
    }

    private Formation createFormation(FormationPlan plan, int index) {
        Formation formation = formationRepository.findBySlug(plan.slug()).orElseGet(Formation::new);
        formation.setTitle(plan.title());
        formation.setSlug(plan.slug());
        formation.setDescription(plan.description());
        formation.setLevel(plan.level());
        formation.setLanguage("Français");
        formation.setPrice(BigDecimal.ZERO);
        formation.setCurrency("MAD");
        formation.setStatus(FormationStatus.PUBLIEE);
        formation.setOrganisationId(plan.organisation().getId());
        formation.setFormateurId(plan.formateur().getId());
        formation.setTotalDuration(480);
        formation.setMaxStudents(5000);
        formation.setCategoryId(categoryFor(index));
        formation.setCertified(index % 3 == 0);
        formation.setPrerequisitesText("[\"Ordinateur\",\"Connexion internet\",\"Bases du domaine\"]");
        formation.setPublishedAt(LocalDateTime.now().minusDays(10 - index));

        String coverKey = "formations/" + plan.slug() + "/cover.png";
        byte[] cover = png(plan.title(), colorFor(index));
        uploadBytes(coverKey, cover, "image/png");
        formation.setCoverImageKey(coverKey);

        return formationRepository.save(formation);
    }

    private void createQuiz(Course course) {
        if (quizRepository.findByCourseId(course.getId()).isPresent()) {
            return;
        }
        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle("Quiz - " + course.getTitle());
        quiz.setTimeLimit(900);
        quiz.setMaxAttempts(3);
        quiz.setPassScore(70);
        quiz.setIsPublished(true);

        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setText("Question " + i + " : quelle pratique améliore le plus la qualité en production ?");
            question.setType(QuizQuestionType.QCM);
            question.setOrderIndex(i);
            question.setPoints(1);
            List<QuizReponse> answers = new ArrayList<>();
            answers.add(answer(question, "Écrire des tests automatisés et surveiller les erreurs", true));
            answers.add(answer(question, "Déployer sans validation pour aller plus vite", false));
            answers.add(answer(question, "Ignorer les logs applicatifs", false));
            question.setReponses(answers);
            questions.add(question);
        }
        quiz.setQuestions(questions);
        quizRepository.save(quiz);
    }

    private QuizReponse answer(QuizQuestion question, String text, boolean correct) {
        QuizReponse reponse = new QuizReponse();
        reponse.setQuestion(question);
        reponse.setText(text);
        reponse.setIsCorrect(correct);
        return reponse;
    }

    private void createProgressionsAndWatchState(User learner, Formation formation) {
        List<Course> courses = courseRepository.findByFormationIdOrderByOrderIndex(formation.getId());
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            Progression progression = progressionRepository.findByApprenantIdAndCoursId(learner.getId(), course.getId())
                    .orElseGet(Progression::new);
            progression.setApprenantId(learner.getId());
            progression.setFormationId(formation.getId());
            progression.setCoursId(course.getId());
            progression.setIsUnlocked(i == 0);
            progression.setUnlockedAt(i == 0 ? LocalDateTime.now().minusDays(5) : null);
            progression.setPresenceRate(i == 0 ? 75.0 : 0.0);
            progressionRepository.save(progression);

            if (i == 0) {
                List<Seance> seances = seanceRepository.findByCoursIdOrderByOrderIndex(course.getId());
                for (int s = 0; s < seances.size(); s++) {
                    Seance seance = seances.get(s);
                    Progress progress = progressRepository.findByUserIdAndSeanceId(learner.getId(), seance.getId())
                            .orElseGet(Progress::new);
                    progress.setUser(learner);
                    progress.setSeance(seance);
                    progress.setWatchedSeconds(s < 2 ? seance.getDuration() : Math.min(180, seance.getDuration()));
                    progress.setIsCompleted(s < 2);
                    progress.setLastWatchedAt(LocalDateTime.now().minusHours(12 - s));
                    progressRepository.save(progress);
                }
            }
        }
    }

    private void createCertificate(User learner, Organisation org, Formation formation) {
        if (certificatRepository.existsByApprenantIdAndFormationId(learner.getId(), formation.getId())) {
            return;
        }
        byte[] pdf = demoPdf("Certificat de démonstration", formation.getTitle(), learner.getFullName());
        String key = "certificates/" + formation.getId() + "/" + learner.getId() + "/certificate.pdf";
        uploadBytes(key, pdf, "application/pdf");

        Certificat certificat = new Certificat();
        certificat.setApprenantId(learner.getId());
        certificat.setFormationId(formation.getId());
        certificat.setOrganisationId(org.getId());
        certificat.setPdfKey(key);
        certificat.setAverageScore(new BigDecimal("86.50"));
        certificatRepository.save(certificat);
    }

    private void resource(Formation formation, Course course, Seance seance, ResourceType type, String title, String key, String mimeType, byte[] bytes) {
        if (resourceRepository.existsByObjectKey(key)) return;
        PedagogicalResource resource = new PedagogicalResource();
        resource.setFormationId(formation.getId());
        resource.setCourseId(course.getId());
        resource.setSeanceId(seance.getId());
        resource.setType(type);
        resource.setTitle(title);
        resource.setObjectKey(key);
        resource.setBucketName(MEDIA_BUCKET);
        resource.setMimeType(mimeType);
        resource.setSizeBytes((long) bytes.length);
        resource.setChecksumSha256(sha256(bytes));
        resourceRepository.save(resource);
    }

    private void linkResource(Formation formation, Course course, Seance seance, String title, String url) {
        if (resourceRepository.existsByObjectKey(url)) return;
        PedagogicalResource resource = new PedagogicalResource();
        resource.setFormationId(formation.getId());
        resource.setCourseId(course.getId());
        resource.setSeanceId(seance.getId());
        resource.setType(ResourceType.LINK);
        resource.setTitle(title);
        resource.setObjectKey(url);
        resource.setBucketName("external");
        resource.setMimeType("text/uri-list");
        resource.setSizeBytes((long) url.length());
        resourceRepository.save(resource);
    }

    private Organisation organisation(UUID id, String name, String slug, String sector, boolean isDefault) {
        return organisationRepository.findBySlug(slug).orElseGet(() -> {
            Organisation org = new Organisation();
            org.setId(id);
            org.setName(name);
            org.setSlug(slug);
            org.setDescription("Organisation de démonstration LMS pour " + sector);
            org.setSector(sector);
            org.setWebsite("https://demo.lms/" + slug);
            org.setIsDefault(isDefault);
            org.setStatus(OrganisationStatus.ACTIVE);
            org.setValidatedAt(LocalDateTime.now().minusDays(30));
            return organisationRepository.save(org);
        });
    }

    private User user(UUID id, String email, String firstName, String lastName, Role role, UUID orgId) {
        return userRepository.findByEmail(email).map(existing -> {
            boolean changed = false;
            if (orgId != null && !orgId.equals(existing.getOrganisationId())) {
                existing.setOrganisationId(orgId);
                changed = true;
            }
            if (!role.equals(existing.getRole())) {
                existing.setRole(role);
                changed = true;
            }
            return changed ? userRepository.save(existing) : existing;
        }).orElseGet(() -> {
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(role);
            user.setOrganisationId(orgId);
            user.setProvider(AuthProvider.LOCAL);
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            user.setIsActive(true);
            user.setIsEmailVerified(true);
            return userRepository.save(user);
        });
    }

    private UUID demoUuid(String value) {
        return UUID.nameUUIDFromBytes(("resource-demo-" + value).getBytes(StandardCharsets.UTF_8));
    }

    private Formation findFormation(String slug) {
        return formationRepository.findBySlug(slug).orElseThrow();
    }

    private void ensureBuckets() {
        try {
            minioService.ensureBucket(MEDIA_BUCKET);
            minioService.ensureBucket(PUBLIC_BUCKET);
            minioService.ensureBucket(UPLOADS_BUCKET);
        } catch (Exception e) {
            log.warn("DemoLmsSeeder: MinIO unavailable during bucket setup: {}", e.getMessage());
        }
    }

    private void uploadDemoVideo(String key, String formationTitle, String seanceTitle) {
        byte[] bytes = demoMp4Placeholder(formationTitle, seanceTitle);
        uploadBytes(key, bytes, "video/mp4");
    }

    private void uploadBytes(String key, byte[] bytes, String contentType) {
        try {
            if (!minioService.objectExists(MEDIA_BUCKET, key)) {
                minioService.uploadBytes(bytes, MEDIA_BUCKET, key, contentType);
            }
        } catch (Exception e) {
            log.warn("DemoLmsSeeder: could not upload {} to MinIO: {}", key, e.getMessage());
        }
    }

    private byte[] demoPdf(String title, String subtitle, String body) {
        String escapedTitle = pdfEscape(title);
        String escapedSubtitle = pdfEscape(subtitle);
        String escapedBody = pdfEscape(body);
        String content = "BT /F1 22 Tf 72 760 Td (" + escapedTitle + ") Tj "
                + "/F1 14 Tf 0 -36 Td (" + escapedSubtitle + ") Tj "
                + "/F1 12 Tf 0 -32 Td (" + escapedBody + ") Tj "
                + "0 -28 Td (Document genere automatiquement pour la plateforme LMS demo.) Tj ET";
        byte[] stream = content.getBytes(StandardCharsets.US_ASCII);
        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n"
                + "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "5 0 obj << /Length " + stream.length + " >> stream\n"
                + content + "\nendstream endobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "trailer << /Root 1 0 R /Size 6 >>\nstartxref\n0\n%%EOF";
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    private byte[] png(String title, Color color) {
        try {
            BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(color);
            g.fillRect(0, 0, 1280, 720);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 52));
            g.drawString(title, 72, 350);
            g.setFont(new Font("SansSerif", Font.PLAIN, 28));
            g.drawString("Plateforme LMS Demo", 72, 410);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            return title.getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] demoMp4Placeholder(String formationTitle, String seanceTitle) {
        try (InputStream input = DemoLmsSeeder.class.getResourceAsStream("/demo-assets/video/sample-640x360.mp4")) {
            if (input != null) {
                return input.readAllBytes();
            }
        } catch (Exception e) {
            log.warn("DemoLmsSeeder: sample MP4 asset not readable, using fallback bytes: {}", e.getMessage());
        }
        String marker = "Demo LMS video placeholder for " + formationTitle + " - " + seanceTitle;
        return marker.getBytes(StandardCharsets.UTF_8);
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    private Color colorFor(int index) {
        Color[] colors = {
                new Color(30, 96, 145),
                new Color(34, 139, 87),
                new Color(120, 81, 169),
                new Color(189, 88, 38),
                new Color(18, 133, 162),
                new Color(66, 103, 178),
                new Color(199, 64, 85)
        };
        return colors[index % colors.length];
    }

    private String categoryFor(int index) {
        return CATEGORY_IDS.get(index % CATEGORY_IDS.size());
    }

    private String pdfEscape(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private record FormationPlan(
            String title,
            String slug,
            FormationLevel level,
            Organisation organisation,
            User formateur,
            String description
    ) {}
}
