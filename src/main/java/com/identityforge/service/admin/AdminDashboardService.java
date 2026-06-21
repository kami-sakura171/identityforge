package com.identityforge.service.admin;

import com.identityforge.dto.response.DashboardResponse;
import com.identityforge.model.enums.AccountStatus;
import com.identityforge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final FailedLoginEventRepository failedLoginEventRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long lockedAccounts = userRepository.countByAccountStatus(AccountStatus.LOCKED);
        long activeSessions = countActiveSessions();

        // 30-day registration trend
        List<DashboardResponse.RegistrationTrendPoint> trend = get30DayTrend();

        // Security alert status
        DashboardResponse.SecurityAlertInfo alert = getSecurityAlertInfo();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeSessions(activeSessions)
                .lockedAccounts(lockedAccounts)
                .registrationTrend(trend)
                .securityAlert(alert)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DashboardResponse.RegistrationTrendPoint> getRegistrationTrend() {
        return get30DayTrend();
    }

    private List<DashboardResponse.RegistrationTrendPoint> get30DayTrend() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> rawData = userRepository.countRegistrationsByDaySince(since);
        List<DashboardResponse.RegistrationTrendPoint> trend = new ArrayList<>();
        for (Object[] row : rawData) {
            trend.add(DashboardResponse.RegistrationTrendPoint.builder()
                    .date(row[0].toString())
                    .count(((Number) row[1]).longValue())
                    .build());
        }
        return trend;
    }

    private long countActiveSessions() {
        // Count active access token sessions
        return sessionRepository.findAll().stream()
                .filter(s -> s.getIsActive() && !s.getIsRefreshToken()
                        && s.getExpiresAt().isAfter(LocalDateTime.now()))
                .count();
    }

    private DashboardResponse.SecurityAlertInfo getSecurityAlertInfo() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        long distinctFailed = failedLoginEventRepository.countDistinctUsernamesSince(since);
        boolean active = distinctFailed >= 10;

        return DashboardResponse.SecurityAlertInfo.builder()
                .active(active)
                .distinctFailedUsers(distinctFailed)
                .message(active ? "Security alert: " + distinctFailed +
                         " distinct failed login attempts in the last 5 minutes" : "Normal")
                .build();
    }
}
