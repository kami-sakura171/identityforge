package com.identityforge.service.admin;

import com.identityforge.dto.response.PagedResponse;
import com.identityforge.dto.response.UserProfileResponse;
import com.identityforge.exception.BadRequestException;
import com.identityforge.exception.ResourceNotFoundException;
import com.identityforge.model.*;
import com.identityforge.model.enums.AccountStatus;
import com.identityforge.model.enums.ContextualRole;
import com.identityforge.model.enums.NotificationCategory;
import com.identityforge.repository.*;
import com.identityforge.service.common.AuditLogService;
import com.identityforge.service.customer.CustomerProfileService;
import com.identityforge.service.customer.NotificationService;
import com.identityforge.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final AuditLogService auditLogService;
    private final CustomerProfileService profileService;
    private final NotificationService notificationService;
    private final CustomFieldValueRepository customFieldValueRepository;
    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserProfileResponse> searchCustomers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        if (query != null && !query.isBlank()) {
            users = userRepository.searchByUsernameOrDisplayName(query, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        List<UserProfileResponse> profiles = users.getContent().stream()
                .map(u -> {
                    var activeRole = userRoleRepository.findByUserAndIsActiveTrue(u);
                    String role = activeRole.isEmpty() ? "STANDARD_USER" :
                            activeRole.get(0).getContextualRole().name();
                    return UserProfileResponse.builder()
                            .id(u.getId())
                            .username(u.getUsername())
                            .displayName(u.getDisplayName())
                            .accountStatus(u.getAccountStatus().name())
                            .contextualRole(role)
                            .createdAt(u.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(profiles, page, size,
                users.getTotalElements(), users.getTotalPages(), users.isLast(), users.isFirst());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCustomerDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return profileService.getProfile(
                new com.identityforge.security.UserPrincipal(user,
                        userRoleRepository.findByUserAndIsActiveTrue(user).stream()
                                .findFirst().map(ur -> ur.getContextualRole().name())
                                .orElse(ContextualRole.STANDARD_USER.name())));
    }

    @Transactional
    public void lockAccount(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            throw new BadRequestException("Account is already locked");
        }
        user.setAccountStatus(AccountStatus.LOCKED);
        user.setLockoutUntil(java.time.LocalDateTime.now().plusHours(24)); // Manual lock for 24h
        userRepository.save(user);

        notificationService.createNotification(userId, NotificationCategory.ACCOUNT_LOCK,
                "Account Locked", "Your account has been locked by an administrator.");
        auditLogService.log(adminId, "ADMIN_LOCK_ACCOUNT", "User", userId, "Account locked by admin");
        log.info("Admin {} locked account of user {}", adminId, userId);
    }

    @Transactional
    public void unlockAccount(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setFailedAttempts(0);
        user.setLockoutUntil(null);
        userRepository.save(user);

        notificationService.createNotification(userId, NotificationCategory.PROFILE_UPDATE,
                "Account Unlocked", "Your account has been unlocked by an administrator.");
        auditLogService.log(adminId, "ADMIN_UNLOCK_ACCOUNT", "User", userId, "Account unlocked by admin");
        log.info("Admin {} unlocked account of user {}", adminId, userId);
    }

    @Transactional
    public void forcePasswordReset(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setForcePasswordReset(true);
        userRepository.save(user);

        notificationService.createNotification(userId, NotificationCategory.FORCE_PASSWORD_RESET,
                "Password Reset Required", "An administrator has requested that you reset your password.");
        sessionRepository.deactivateAllSessionsForUser(userId);
        auditLogService.log(adminId, "ADMIN_FORCE_PW_RESET", "User", userId, "Force password reset");
        log.info("Admin {} forced password reset for user {}", adminId, userId);
    }

    @Transactional(readOnly = true)
    public List<LoginHistory> getLoginHistory(Long userId) {
        return loginHistoryRepository.findLast50ByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLog(Long userId, Pageable pageable) {
        return auditLogService.getAuditLogsForUser(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCustomFieldValues(Long userId) {
        List<CustomFieldDefinition> definitions = customFieldDefinitionRepository.findByIsActiveTrueOrderByDisplayOrder();
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (var def : definitions) {
            String value = customFieldValueRepository.findByUserIdAndFieldDefinitionId(userId, def.getId())
                    .map(CustomFieldValue::getValue)
                    .orElse("");
            result.put(def.getName(), value);
        }
        return result;
    }

    @Transactional
    public void updateCustomFieldValues(Long userId, Map<String, String> fieldValues, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            CustomFieldDefinition def = customFieldDefinitionRepository.findByIsActiveTrueOrderByDisplayOrder()
                    .stream().filter(d -> d.getName().equals(entry.getKey())).findFirst().orElse(null);
            if (def == null) continue;

            CustomFieldValue existing = customFieldValueRepository
                    .findByUserIdAndFieldDefinitionId(userId, def.getId()).orElse(null);
            if (existing != null) {
                existing.setValue(entry.getValue());
                customFieldValueRepository.save(existing);
            } else {
                CustomFieldValue newVal = CustomFieldValue.builder()
                        .user(user)
                        .fieldDefinition(def)
                        .value(entry.getValue())
                        .build();
                customFieldValueRepository.save(newVal);
            }
        }
        auditLogService.log(adminId, "ADMIN_UPDATE_CUSTOM_FIELDS", "User", userId, "Custom field values updated");
    }
}
