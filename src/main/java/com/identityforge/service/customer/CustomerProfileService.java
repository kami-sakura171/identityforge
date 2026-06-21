package com.identityforge.service.customer;

import com.identityforge.dto.request.PasswordChangeRequest;
import com.identityforge.dto.request.ProfileUpdateRequest;
import com.identityforge.dto.response.UserProfileResponse;
import com.identityforge.exception.BadRequestException;
import com.identityforge.model.*;
import com.identityforge.model.enums.ContextualRole;
import com.identityforge.repository.*;
import com.identityforge.security.UserPrincipal;
import com.identityforge.service.common.AuditLogService;
import com.identityforge.util.DateUtils;
import com.identityforge.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AvatarRepository avatarRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final CustomFieldValueRepository customFieldValueRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final IdentityVerificationService identityVerificationService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return buildProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UserPrincipal principal, ProfileUpdateRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getDateOfBirth() != null) {
            try {
                user.setDateOfBirth(DateUtils.parseDate(request.getDateOfBirth()));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date format. Use MM/DD/YYYY");
            }
        }

        user = userRepository.save(user);
        auditLogService.log(user.getId(), "PROFILE_UPDATE", "User", user.getId(), "Profile updated");
        log.info("Profile updated for user: {}", user.getUsername());

        return buildProfileResponse(user);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, PasswordChangeRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password
        List<String> errors = PasswordValidator.validate(request.getNewPassword());
        if (!errors.isEmpty()) {
            throw new BadRequestException("Password validation failed: " + String.join("; ", errors));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordReset(false);
        userRepository.save(user);
        auditLogService.log(user.getId(), "PASSWORD_CHANGE", "User", user.getId(), "Password changed");
        log.info("Password changed for user: {}", user.getUsername());
    }

    private UserProfileResponse buildProfileResponse(User user) {
        // Get contextual role
        List<UserRole> activeRoles = userRoleRepository.findByUserAndIsActiveTrue(user);
        String contextualRole = activeRoles.isEmpty() ? ContextualRole.STANDARD_USER.name() :
                activeRoles.get(0).getContextualRole().name();
        List<String> availableRoles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getContextualRole().name())
                .collect(Collectors.toList());

        // Avatar
        String avatarUrl = avatarRepository.findByUserId(user.getId())
                .map(a -> "/avatars/" + a.getStoredFilename())
                .orElse(null);

        // Notification prefs
        Map<String, Boolean> prefs = new LinkedHashMap<>();
        notificationPreferenceRepository.findByUserId(user.getId())
                .forEach(p -> prefs.put(p.getCategory(), p.getEnabled()));

        // Verification
        UserProfileResponse.VerificationInfo verification = null;
        var verOpt = identityVerificationRepository.findByUserId(user.getId());
        if (verOpt.isPresent()) {
            var ver = verOpt.get();
            verification = UserProfileResponse.VerificationInfo.builder()
                    .status(ver.getStatus().name())
                    .documentType(ver.getDocumentType() != null ? ver.getDocumentType().name() : null)
                    .realName(ver.getRealName())
                    .submittedAt(ver.getSubmittedAt())
                    .build();
        }

        // Custom fields
        Map<String, Object> customFields = new LinkedHashMap<>();
        List<CustomFieldDefinition> definitions = customFieldDefinitionRepository.findByIsActiveTrueOrderByDisplayOrder();
        List<CustomFieldValue> values = customFieldValueRepository.findByUserId(user.getId());
        for (CustomFieldDefinition def : definitions) {
            String val = values.stream()
                    .filter(v -> v.getFieldDefinition().getId().equals(def.getId()))
                    .map(CustomFieldValue::getValue)
                    .findFirst().orElse(null);
            customFields.put(def.getName(), val != null ? val : "");
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .dateOfBirth(user.getDateOfBirth() != null ? DateUtils.formatDate(user.getDateOfBirth()) : null)
                .accountStatus(user.getAccountStatus().name())
                .contextualRole(contextualRole)
                .forcePasswordReset(user.getForcePasswordReset())
                .createdAt(user.getCreatedAt())
                .avatarUrl(avatarUrl)
                .notificationPreferences(prefs)
                .verification(verification)
                .customFields(customFields)
                .availableRoles(availableRoles)
                .build();
    }
}
