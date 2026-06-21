package com.identityforge.scheduler;

import com.identityforge.service.admin.SecurityAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAlertScheduler {

    private final SecurityAlertService securityAlertService;

    @Scheduled(fixedRate = 60000)
    public void checkSecurityAlerts() {
        try {
            securityAlertService.checkAndNotify();
        } catch (Exception e) {
            log.error("Error checking security alerts", e);
        }
    }
}
