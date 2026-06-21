package com.identityforge.security;

import com.identityforge.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String displayName;
    private final String contextualRole;
    private final boolean isAdmin;
    private final boolean accountNonLocked;
    private final boolean forcePasswordReset;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user, String contextualRole) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.displayName = user.getDisplayName();
        this.contextualRole = contextualRole;
        this.isAdmin = user.isAdmin();
        this.accountNonLocked = !user.isLocked();
        this.forcePasswordReset = user.getForcePasswordReset();

        List<GrantedAuthority> auths = new ArrayList<>();
        if (user.isAdmin()) {
            auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        if (contextualRole != null) {
            auths.add(new SimpleGrantedAuthority("CONTEXT_" + contextualRole.toUpperCase()));
        }
        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
