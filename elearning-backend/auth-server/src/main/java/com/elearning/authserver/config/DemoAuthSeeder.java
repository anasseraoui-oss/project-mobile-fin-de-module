package com.elearning.authserver.config;

import com.elearning.authserver.domain.User;
import com.elearning.authserver.domain.enums.AuthProvider;
import com.elearning.authserver.domain.enums.Role;
import com.elearning.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@Profile({"demo", "dev"})
@RequiredArgsConstructor
public class DemoAuthSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo@1234";
    private static final UUID SARA_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");
    private static final UUID YOUSSEF_ID = UUID.fromString("11111111-1111-1111-1111-111111111201");
    private static final UUID LINA_ID = UUID.fromString("11111111-1111-1111-1111-111111111202");
    private static final UUID ADMIN_ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111301");
    private static final UUID SUPER_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111001");
    private static final UUID TECH_ACADEMY_ORG_ID = UUID.fromString("0a12b687-ea69-4000-b35b-a1b3feae285e");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        user(SUPER_ADMIN_ID, "admin@demo.lms", "Admin", "Demo", Role.SUPER_ADMIN, null);
        user(SARA_ID, "sara.formateur@demo.lms", "Sara", "Chraibi", Role.FORMATEUR, null);
        user(YOUSSEF_ID, "youssef.apprenant@demo.lms", "Youssef", "Mansouri", Role.APPRENANT, null);
        user(LINA_ID, "lina.apprenante@demo.lms", "Lina", "Berrada", Role.APPRENANT, null);
        user(ADMIN_ORG_ID, "admin.org@demo.lms", "Nadia", "El Amrani", Role.ADMIN_ORG, TECH_ACADEMY_ORG_ID);

        for (int i = 2; i <= 20; i++) {
            user(demoUuid("instructor-" + i), "instructor%02d@demo.lms".formatted(i),
                    "Instructor", "%02d".formatted(i), Role.FORMATEUR, null);
        }
        for (int i = 3; i <= 100; i++) {
            user(demoUuid("student-" + i), "student%03d@demo.lms".formatted(i),
                    "Student", "%03d".formatted(i), Role.APPRENANT, null);
        }
        log.info("DemoAuthSeeder: demo auth users ready. Password for all: {}", DEMO_PASSWORD);
    }

    private void user(UUID id, String email, String firstName, String lastName, Role role, UUID organisationId) {
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (organisationId != null && !organisationId.equals(user.getOrganisationId())) {
                user.setOrganisationId(organisationId);
                userRepository.save(user);
            }
            return;
        }
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setOrganisationId(organisationId);
        user.setProvider(AuthProvider.LOCAL);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setIsActive(true);
        user.setIsEmailVerified(true);
        userRepository.save(user);
    }

    private UUID demoUuid(String value) {
        return UUID.nameUUIDFromBytes(("auth-demo-" + value).getBytes(StandardCharsets.UTF_8));
    }
}
