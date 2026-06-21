package com.identityforge.service.customer;

import com.identityforge.config.JwtConfig;
import com.identityforge.dto.response.JwtResponse;
import com.identityforge.model.User;
import com.identityforge.model.UserRole;
import com.identityforge.model.enums.ContextualRole;
import com.identityforge.repository.UserRepository;
import com.identityforge.repository.UserRoleRepository;
import com.identityforge.security.JwtTokenProvider;
import com.identityforge.service.auth.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleSwitchService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final SessionService sessionService;

    @Transactional(readOnly = true)
    public List<String> getAvailableRoles(Long userId) {
        return userRoleRepository.findByUser(userRepository.getReferenceById(userId))
                .stream()
                .map(ur -> ur.getContextualRole().name())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getActiveRole(Long userId) {
        List<UserRole> activeRoles = userRoleRepository.findByUserAndIsActiveTrue(
                userRepository.getReferenceById(userId));
        return activeRoles.isEmpty() ? ContextualRole.STANDARD_USER.name() :
                activeRoles.get(0).getContextualRole().name();
    }

    @Transactional
    public JwtResponse switchRole(Long userId, String roleName, String ipAddress, String userAgent) {
        ContextualRole role;
        try {
            role = ContextualRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify user has this role assigned
        if (!userRoleRepository.existsByUserAndContextualRole(user, role)) {
            throw new IllegalArgumentException("User does not have role: " + roleName);
        }

        // Deactivate all roles, activate selected
        userRoleRepository.deactivateAllRolesForUser(user);
        userRoleRepository.activateRole(user, role);

        // Generate new JWT with updated contextual role
        String roles = (user.isAdmin() ? "ADMIN," : "") + "CUSTOMER";
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roles, role.name());

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(jwtConfig.getExpirationMinutes());
        sessionService.createSession(jwtTokenProvider.getJtiFromToken(accessToken), user,
                expiresAt, ipAddress, userAgent, false);

        log.info("User {} switched role to {}", user.getUsername(), roleName);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .contextualRole(role.name())
                .isAdmin(user.isAdmin())
                .build();
    }
}
