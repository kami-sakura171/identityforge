package com.identityforge.service.auth;

import com.identityforge.dto.request.LoginRequest;
import com.identityforge.dto.request.RegistrationRequest;
import com.identityforge.dto.response.JwtResponse;
import com.identityforge.exception.AccountLockedException;
import com.identityforge.exception.AuthenticationException;
import com.identityforge.exception.BadRequestException;
import com.identityforge.config.JwtConfig;
import com.identityforge.model.*;
import com.identityforge.model.enums.AccountStatus;
import com.identityforge.model.enums.ContextualRole;
import com.identityforge.repository.*;
import com.identityforge.security.JwtTokenProvider;
import com.identityforge.service.common.AuditLogService;
import com.identityforge.util.DateUtils;
import com.identityforge.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final ToSVersionRepository tosVersionRepository;
    private final ConsentRepository consentRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final LockoutService lockoutService;
    private final SessionService sessionService;
    private final LoginHistoryService loginHistoryService;
    private final AuditLogService auditLogService;

    @Transactional
    public JwtResponse register(RegistrationRequest request, String ipAddress, String userAgent) {
        // Validate password
        List<String> passwordErrors = PasswordValidator.validate(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            throw new BadRequestException("Password validation failed: " + String.join("; ", passwordErrors));
        }

        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }

        // Validate security question exists
        SecurityQuestion sq = securityQuestionRepository.findById(request.getSecurityQuestionId())
                .orElseThrow(() -> new BadRequestException("Invalid security question"));

        // Parse date of birth
        LocalDate dob;
        try {
            dob = DateUtils.parseDate(request.getDateOfBirth());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Use MM/DD/YYYY");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
                .dateOfBirth(dob)
                .securityQuestionId(request.getSecurityQuestionId())
                .securityAnswerHash(passwordEncoder.encode(request.getSecurityAnswer().toLowerCase().trim()))
                .accountStatus(AccountStatus.ACTIVE)
                .failedAttempts(0)
                .rolesBitmask(1) // CUSTOMER only
                .forcePasswordReset(false)
                .build();
        user = userRepository.save(user);

        // Assign STANDARD_USER contextual role
        UserRole userRole = UserRole.builder()
                .user(user)
                .contextualRole(ContextualRole.STANDARD_USER)
                .isActive(true)
                .build();
        userRoleRepository.save(userRole);

        // Initialize notification preferences (6 default categories)
        for (String category : NotificationPreference.DEFAULT_CATEGORIES) {
            NotificationPreference pref = NotificationPreference.builder()
                    .user(user)
                    .category(category)
                    .enabled(true)
                    .build();
            notificationPreferenceRepository.save(pref);
        }

        // Auto-accept current ToS
        final User savedUser = user;
        tosVersionRepository.findByIsActiveTrue().ifPresent(tos -> {
            Consent consent = Consent.builder()
                    .user(savedUser)
                    .tosVersion(tos)
                    .acceptedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .build();
            consentRepository.save(consent);
        });

        auditLogService.log(user.getId(), "USER_REGISTERED", "User", user.getId(),
                "User registered: " + user.getUsername());
        log.info("New user registered: {}", user.getUsername());

        // Generate tokens
        String roles = "CUSTOMER";
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roles, ContextualRole.STANDARD_USER.name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // Create sessions
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(jwtConfig.getExpirationMinutes());
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(accessToken), user,
                expiresAt, ipAddress, userAgent, false);
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(refreshToken), user,
                expiresAt.plusDays(jwtConfig.getRefreshExpirationDays()), ipAddress, userAgent, true);

        // Record successful login
        loginHistoryService.recordLogin(user, ipAddress, userAgent, true, null);

        return buildJwtResponse(accessToken, refreshToken, user, ContextualRole.STANDARD_USER.name());
    }

    @Transactional
    public JwtResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            lockoutService.recordFailedAttemptForUnknownUser(request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        // Check if account is disabled
        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            throw new AuthenticationException("Account is disabled");
        }

        // Check if locked
        if (user.isLocked()) {
            loginHistoryService.recordLogin(user, ipAddress, userAgent, false, "account_locked");
            throw new AccountLockedException("Account is locked. Please try again later.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            lockoutService.recordFailedAttempt(request.getUsername(), ipAddress);
            loginHistoryService.recordLogin(user, ipAddress, userAgent, false, "bad_credentials");
            throw new AuthenticationException("Invalid username or password");
        }

        // Login successful
        lockoutService.resetFailedAttempts(request.getUsername());

        // Determine active contextual role
        List<UserRole> activeRoles = userRoleRepository.findByUserAndIsActiveTrue(user);
        ContextualRole contextualRole = activeRoles.isEmpty() ? ContextualRole.STANDARD_USER :
                activeRoles.get(0).getContextualRole();

        // Generate tokens
        String roles = (user.isAdmin() ? "ADMIN," : "") + "CUSTOMER";
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roles, contextualRole.name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // Create sessions
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(jwtConfig.getExpirationMinutes());
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(accessToken), user,
                expiresAt, ipAddress, userAgent, false);
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(refreshToken), user,
                expiresAt.plusDays(jwtConfig.getRefreshExpirationDays()), ipAddress, userAgent, true);

        loginHistoryService.recordLogin(user, ipAddress, userAgent, true, null);
        auditLogService.log(user.getId(), "LOGIN_SUCCESS", "User", user.getId(), "User logged in");

        return buildJwtResponse(accessToken, refreshToken, user, contextualRole.name());
    }

    @Transactional
    public JwtResponse refreshToken(String refreshTokenStr, String ipAddress, String userAgent) {
        if (!jwtTokenProvider.validateToken(refreshTokenStr) || !jwtTokenProvider.isRefreshToken(refreshTokenStr)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String jti = jwtTokenProvider.getJtiFromToken(refreshTokenStr);
        // Verify refresh token session is active
        sessionService.invalidateSession(jti); // Invalidate old refresh session

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        List<UserRole> activeRoles = userRoleRepository.findByUserAndIsActiveTrue(user);
        ContextualRole contextualRole = activeRoles.isEmpty() ? ContextualRole.STANDARD_USER :
                activeRoles.get(0).getContextualRole();

        String roles = (user.isAdmin() ? "ADMIN," : "") + "CUSTOMER";
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roles, contextualRole.name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(jwtConfig.getExpirationMinutes());
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(newAccessToken), user,
                expiresAt, ipAddress, userAgent, false);
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(newRefreshToken), user,
                expiresAt.plusDays(jwtConfig.getRefreshExpirationDays()), ipAddress, userAgent, true);

        log.info("Tokens refreshed for user: {}", user.getUsername());
        return buildJwtResponse(newAccessToken, newRefreshToken, user, contextualRole.name());
    }

    @Transactional
    public void logout(String accessTokenJti, Long userId) {
        sessionService.invalidateSession(accessTokenJti);
        auditLogService.log(userId, "LOGOUT", "Session", null, "User logged out");
        log.info("User {} logged out, session {} invalidated", userId, accessTokenJti);
    }

    private JwtResponse buildJwtResponse(String accessToken, String refreshToken, User user, String contextualRole) {
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .contextualRole(contextualRole)
                .isAdmin(user.isAdmin())
                .build();
    }
}
