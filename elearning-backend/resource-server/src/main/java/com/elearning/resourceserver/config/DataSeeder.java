// src/main/java/com/elearning/resourceserver/config/DataSeeder.java
package com.elearning.resourceserver.config;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.*;
import com.elearning.resourceserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile({"default", "dev"}) // Active uniquement en dev/local — pas en prod
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final FormationRepository formationRepository;
    private final CourseRepository courseRepository;
    private final SeanceRepository seanceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: données déjà présentes — skip");
            return;
        }

        log.info("DataSeeder: initialisation des données de test...");

        // ── SUPER ADMIN ──
        User superAdmin = new User();
        superAdmin.setEmail("admin@elearning.com");
        superAdmin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
        superAdmin.setFirstName("Super");
        superAdmin.setLastName("Admin");
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setProvider(AuthProvider.LOCAL);
        superAdmin.setIsActive(true);
        superAdmin.setIsEmailVerified(true);
        userRepository.save(superAdmin);

        // ── ORGANISATION OWNER ──
        User orgOwner = new User();
        orgOwner.setEmail("org@techacademy.com");
        orgOwner.setPasswordHash(passwordEncoder.encode("Org@1234"));
        orgOwner.setFirstName("Ahmed");
        orgOwner.setLastName("Benali");
        orgOwner.setRole(Role.ADMIN_ORG);
        orgOwner.setProvider(AuthProvider.LOCAL);
        orgOwner.setIsActive(true);
        orgOwner.setIsEmailVerified(true);
        orgOwner = userRepository.save(orgOwner);

        // ── ORGANISATION ──
        Organisation org = new Organisation();
        org.setName("TechAcademy Maroc");
        org.setSlug("techacademy-maroc");
        org.setDescription("Plateforme de formation tech au Maroc");
        org.setSector("Technologie");
        org.setStatus(OrganisationStatus.ACTIVE);
        org = organisationRepository.save(org);

        // Lier l'owner à son org
        orgOwner.setOrganisationId(org.getId());
        userRepository.save(orgOwner);

        // ── FORMATEUR ──
        User formateur = new User();
        formateur.setEmail("formateur@techacademy.com");
        formateur.setPasswordHash(passwordEncoder.encode("Formateur@1234"));
        formateur.setFirstName("Sara");
        formateur.setLastName("Chraibi");
        formateur.setRole(Role.FORMATEUR);
        formateur.setProvider(AuthProvider.LOCAL);
        formateur.setIsActive(true);
        formateur.setIsEmailVerified(true);
        formateur.setOrganisationId(org.getId());
        formateur = userRepository.save(formateur);

        // ── APPRENANT ──
        User apprenant = new User();
        apprenant.setEmail("apprenant@gmail.com");
        apprenant.setPasswordHash(passwordEncoder.encode("Apprenant@1234"));
        apprenant.setFirstName("Youssef");
        apprenant.setLastName("Mansouri");
        apprenant.setRole(Role.APPRENANT);
        apprenant.setProvider(AuthProvider.LOCAL);
        apprenant.setIsActive(true);
        apprenant.setIsEmailVerified(true);
        apprenant = userRepository.save(apprenant);

        // ── FORMATION ──
        Formation formation = new Formation();
        formation.setTitle("Spring Boot 3 — Développement Backend Complet");
        formation.setDescription("Maîtrisez Spring Boot 3 avec OAuth2, JPA, Redis et Docker");
        formation.setOrganisationId(org.getId());
        formation.setLevel(FormationLevel.INTERMEDIAIRE);
        formation.setLanguage("Français");
        formation.setPrice(new BigDecimal("299.00"));
        formation.setStatus(FormationStatus.PUBLIEE);
        formation = formationRepository.save(formation);

        // ── COURS ──
        Course cours = new Course();
        cours.setTitle("Spring Security & OAuth2");
        cours.setDescription("Implémentez une sécurité robuste avec Spring Authorization Server");
        cours.setFormation(formation);
        cours.setOrderIndex(1);
        cours.setStatus(CoursStatus.EN_COURS);
        cours.setPresenceThreshold(70);
        cours.setQuizPassScore(65);
        cours = courseRepository.save(cours);

        // ── SÉANCE LIVE ──
        Seance seanceLive = new Seance();
        seanceLive.setTitle("Introduction à OAuth2 PKCE");
        seanceLive.setType(SeanceType.LIVE);
        seanceLive.setCourse(cours);
        seanceLive.setFormateurId(formateur.getId());
        seanceLive.setMeetingLink("https://meet.google.com/abc-defg-hij");
        seanceLive.setScheduledAt(LocalDateTime.now().plusDays(2));
        seanceLive.setDuration(90);
        seanceLive.setOrderIndex(1);
        seanceLive.setStatus(SeanceStatus.PLANIFIEE);
        seanceRepository.save(seanceLive);

        // ── SÉANCE ENREGISTRÉE ──
        Seance seanceRec = new Seance();
        seanceRec.setTitle("Démo complète Authorization Code Flow");
        seanceRec.setType(SeanceType.ENREGISTREE);
        seanceRec.setCourse(cours);
        seanceRec.setFormateurId(formateur.getId());
        seanceRec.setDuration(45);
        seanceRec.setOrderIndex(2);
        seanceRec.setStatus(SeanceStatus.CONTENU_DISPONIBLE);
        seanceRec.setVideoKey("seances/demo/video.mp4");
        seanceRepository.save(seanceRec);

        // ── INSCRIPTION ──
        Inscription inscription = new Inscription();
        inscription.setApprenantId(apprenant.getId());
        inscription.setFormationId(formation.getId());
        inscription.setStatus(InscriptionStatus.EN_COURS);
        inscriptionRepository.save(inscription);

        log.info("DataSeeder: ✅ Données de test créées !");
        log.info("  → admin@elearning.com / Admin@1234");
        log.info("  → org@techacademy.com / Org@1234");
        log.info("  → formateur@techacademy.com / Formateur@1234");
        log.info("  → apprenant@gmail.com / Apprenant@1234");
    }
}
