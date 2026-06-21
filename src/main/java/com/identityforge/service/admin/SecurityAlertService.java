package com.identityforge.service.admin;

import com.identityforge.model.Notification;
import com.identityforge.model.User;
import com.identityforge.model.enums.AccountStatus;
import com.identityforge.model.enums.NotificationCategory;
import com.identityforge.repository.*;
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
public class SecurityAlertService {

    private final FailedLoginEventRepository failedLoginEventRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.security.alert.threshold:10}")
    private int alertThreshold;

    @Value("${app.security.alert.window-minutes:5}")
    private int alertWindowMinutes;

    private LocalDateTime lastAlertTime = null;
    private static final long ALERT_COOLDOWN_MINUTES = 5;

    @Transactional
    public void checkAndNotify() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(alertWindowMinutes);
        long distinctCount = failedLoginEventRepository.countDistinctUsernamesSince(since);

        if (distinctCount >= alertThreshold) {
            // Cooldown check
            if (lastAlertTime != null &&
                lastAlertTime.isAfter(LocalDateTime.now().minusMinutes(ALERT_COOLDOWN_MINUTES))) {
                log.debug("Alert suppressed due to cooldown. Distinct failures: {}", distinctCount);
                return;
            }

            lastAlertTime = LocalDateTime.now();

            // Notify all admins
            List<User> admins = userRepository.findByAccountStatus(AccountStatus.ACTIVE).stream()
                    .filter(User::isAdmin)
                    .toList();

            for (User admin : admins) {
                Notification notification = Notification.builder()
                        .user(admin)
                        .category(NotificationCategory.SECURITY_ALERT)
                        .title("Security Alert: Suspicious Login Activity")
                        .message(distinctCount + " distinct accounts had failed login attempts in the last " +
                                 alertWindowMinutes + " minutes.")
                        .build();
                notificationRepository.save(notification);
            }

            log.warn("SECURITY ALERT: {} distinct failed login attempts in {} minutes",
                    distinctCount, alertWindowMinutes);
        }
    }
}
