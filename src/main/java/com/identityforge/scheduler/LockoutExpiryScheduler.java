package com.identityforge.scheduler;

import com.identityforge.service.auth.LockoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockoutExpiryScheduler {

    private final LockoutService lockoutService;

    @Scheduled(fixedRate = 60000)
    public void unlockExpiredLockouts() {
        try {
            lockoutService.unlockExpiredAccounts();
        } catch (Exception e) {
            log.error("Error unlocking expired lockouts", e);
        }
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupOldFailedEvents() {
        try {
            lockoutService.cleanupOldFailedEvents();
        } catch (Exception e) {
            log.error("Error cleaning up old failed login events", e);
        }
    }
}
