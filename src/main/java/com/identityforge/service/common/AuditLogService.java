package com.identityforge.service.common;

import com.identityforge.model.AuditLog;
import com.identityforge.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
        log.info("AUDIT: user={}, action={}, entityType={}, entityId={}", userId, action, entityType, entityId);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(Long userId, String action,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           Pageable pageable) {
        return auditLogRepository.searchAuditLogs(userId, action, startDate, endDate, pageable);
    }
}
