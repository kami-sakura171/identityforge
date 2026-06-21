package com.identityforge.service.auth;

import com.identityforge.model.Session;
import com.identityforge.model.User;
import com.identityforge.repository.SessionRepository;
import com.identityforge.service.common.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final AuditLogService auditLogService;

    @Value("${app.session.max-concurrent-devices:3}")
    private int maxConcurrentDevices;

    @Transactional
    public Session createSession(String jti, User user, LocalDateTime expiresAt,
                                  String ipAddress, String userAgent, boolean isRefreshToken) {
        // Enforce concurrent session limit
        long activeCount = sessionRepository.countByUserIdAndIsActiveTrue(user.getId());
        if (activeCount >= maxConcurrentDevices && !isRefreshToken) {
            // Invalidate oldest session(s)
            List<Session> oldestSessions = sessionRepository
                    .findByUserIdAndIsActiveTrueOrderByCreatedAtAsc(user.getId());
            int toRemove = (int) (activeCount - maxConcurrentDevices + 1);
            for (int i = 0; i < Math.min(toRemove, oldestSessions.size()); i++) {
                Session oldSession = oldestSessions.get(i);
                oldSession.setIsActive(false);
                sessionRepository.save(oldSession);
                auditLogService.log(user.getId(), "SESSION_INVALIDATED_DEVICE_LIMIT",
                        "Session", null, "Oldest session invalidated due to device limit");
                log.info("Invalidated oldest session {} for user {} due to device limit",
                        oldSession.getId(), user.getUsername());
            }
        }

        Session session = Session.builder()
                .id(jti)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isActive(true)
                .isRefreshToken(isRefreshToken)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public void invalidateSession(String jti) {
        sessionRepository.deactivateSession(jti);
        log.debug("Invalidated session: {}", jti);
    }

    @Transactional
    public void invalidateAllSessionsForUser(Long userId) {
        int count = sessionRepository.deactivateAllSessionsForUser(userId);
        log.info("Invalidated {} sessions for user {}", count, userId);
    }

    @Transactional
    public int cleanupExpiredSessions() {
        int cleaned = sessionRepository.deactivateExpiredSessions(LocalDateTime.now());
        if (cleaned > 0) {
            log.debug("Cleaned up {} expired sessions", cleaned);
        }
        return cleaned;
    }

    public long getActiveSessionCountForUser(Long userId) {
        return sessionRepository.countByUserIdAndIsActiveTrue(userId);
    }

    public long getTotalActiveSessions() {
        // This is a rough count; for production, add a dedicated query
        return 0; // Will use dashboard service
    }
}
