package com.identityforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String displayName;
    private String dateOfBirth;
    private String accountStatus;
    private String contextualRole;
    private boolean forcePasswordReset;
    private LocalDateTime createdAt;

    // Avatar
    private String avatarUrl;

    // Notification prefs
    private Map<String, Boolean> notificationPreferences;

    // Verification
    private VerificationInfo verification;

    // Custom fields
    private Map<String, Object> customFields;

    // Roles
    private List<String> availableRoles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationInfo {
        private String status;
        private String documentType;
        private String realName;
        private LocalDateTime submittedAt;
    }
}
