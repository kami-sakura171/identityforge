package com.identityforge.config;

import com.identityforge.model.*;
import com.identityforge.model.enums.AccountStatus;
import com.identityforge.model.enums.ContextualRole;
import com.identityforge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final ToSVersionRepository tosVersionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@12345";
    private static final String[] SECURITY_QUESTIONS = {
        "What was the name of your first pet?",
        "What is your mother's maiden name?",
        "What was the name of your elementary school?",
        "What was your childhood nickname?",
        "In what city were you born?",
        "What is the name of your favorite childhood friend?",
        "What was the make and model of your first car?",
        "What is your favorite movie?"
    };

    @Override
    @Transactional
    public void run(String... args) {
        seedSecurityQuestions();
        seedAdminUser();
        seedDefaultToS();
        log.info("Data initialization completed.");
    }

    private void seedSecurityQuestions() {
        if (securityQuestionRepository.count() == 0) {
            for (String questionText : SECURITY_QUESTIONS) {
                SecurityQuestion sq = SecurityQuestion.builder()
                        .questionText(questionText)
                        .isActive(true)
                        .build();
                securityQuestionRepository.save(sq);
            }
            log.info("Seeded {} security questions.", SECURITY_QUESTIONS.length);
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            User admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                    .firstName("System")
                    .lastName("Administrator")
                    .displayName("Admin")
                    .accountStatus(AccountStatus.ACTIVE)
                    .failedAttempts(0)
                    .rolesBitmask(3) // Both CUSTOMER (bit 0) and ADMIN (bit 1)
                    .forcePasswordReset(false)
                    .build();
            admin = userRepository.save(admin);
            log.info("Seeded admin user: '{}' (password: '{}'). Please change immediately.", ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    private void seedDefaultToS() {
        if (tosVersionRepository.count() == 0) {
            User admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);
            ToSVersion tos = ToSVersion.builder()
                    .version("1.0")
                    .title("Terms of Service")
                    .content("Welcome to IdentityForge.\n\n" +
                             "By using this service, you agree to the following terms:\n\n" +
                             "1. You are responsible for maintaining the confidentiality of your account credentials.\n" +
                             "2. You agree not to misuse the service or attempt unauthorized access.\n" +
                             "3. The platform collects and stores profile information as per the privacy policy.\n" +
                             "4. All activity is logged for security and audit purposes.\n" +
                             "5. Continued use of the platform constitutes acceptance of these terms.\n")
                    .publishedBy(admin)
                    .publishedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            tosVersionRepository.save(tos);
            log.info("Seeded default Terms of Service v1.0.");
        }
    }
}
