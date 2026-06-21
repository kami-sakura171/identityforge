package com.identityforge.scheduler;

import com.identityforge.service.auth.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupScheduler {

    private final SessionService sessionService;

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredSessions() {
        try {
            sessionService.cleanupExpiredSessions();
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions", e);
        }
    }
}
