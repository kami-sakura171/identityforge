package com.identityforge.service.auth;

import com.identityforge.model.FailedLoginEvent;
import com.identityforge.model.User;
import com.identityforge.repository.FailedLoginEventRepository;
import com.identityforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LockoutService {

    private final UserRepository userRepository;
    private final FailedLoginEventRepository failedLoginEventRepository;

    @Value("${app.security.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Transactional
    public void recordFailedAttempt(String username, String ipAddress) {
        // Atomically increment failed attempts
        userRepository.incrementFailedAttempts(username);

        // Record for security alerting
        FailedLoginEvent event = FailedLoginEvent.builder()
                .username(username)
                .ipAddress(ipAddress)
                .attemptTime(LocalDateTime.now())
                .build();
        failedLoginEventRepository.save(event);

        // Read back and check threshold
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getFailedAttempts() >= maxAttempts) {
            LocalDateTime lockoutUntil = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
            userRepository.lockAccount(username, lockoutUntil);
            log.warn("Account locked due to {} failed attempts: {}", maxAttempts, username);
        }
    }

    @Transactional
    public void recordFailedAttemptForUnknownUser(String username) {
        FailedLoginEvent event = FailedLoginEvent.builder()
                .username(username)
                .attemptTime(LocalDateTime.now())
                .build();
        failedLoginEventRepository.save(event);
        log.debug("Failed login attempt for non-existent user: {}", username);
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.resetFailedAttempts(username);
        log.debug("Reset failed attempts for user: {}", username);
    }

    @Transactional
    public int unlockExpiredAccounts() {
        int unlocked = userRepository.unlockExpiredAccounts(LocalDateTime.now());
        if (unlocked > 0) {
            log.info("Unlocked {} accounts whose lockout period expired", unlocked);
        }
        return unlocked;
    }

    @Transactional
    public int cleanupOldFailedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        int deleted = failedLoginEventRepository.deleteOldEvents(cutoff);
        if (deleted > 0) {
            log.debug("Cleaned up {} old failed login events", deleted);
        }
        return deleted;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}
