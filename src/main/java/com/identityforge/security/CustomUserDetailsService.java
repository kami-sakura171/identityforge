package com.identityforge.security;

import com.identityforge.model.User;
import com.identityforge.model.UserRole;
import com.identityforge.repository.UserRepository;
import com.identityforge.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<UserRole> activeRoles = userRoleRepository.findByUserAndIsActiveTrue(user);
        String contextualRole = activeRoles.isEmpty() ? "STANDARD_USER" : activeRoles.get(0).getContextualRole().name();

        return new UserPrincipal(user, contextualRole);
    }
}
