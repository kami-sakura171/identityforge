package com.identityforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalUsers;
    private long activeSessions;
    private long lockedAccounts;
    private List<RegistrationTrendPoint> registrationTrend;
    private SecurityAlertInfo securityAlert;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationTrendPoint {
        private String date;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityAlertInfo {
        private boolean active;
        private long distinctFailedUsers;
        private String message;
    }
}
