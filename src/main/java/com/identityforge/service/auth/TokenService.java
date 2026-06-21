package com.identityforge.service.auth;

import com.identityforge.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public String generateAccessToken(Long userId, String username, String roles, String contextualRole) {
        return jwtTokenProvider.generateAccessToken(userId, username, roles, contextualRole);
    }

    public String generateRefreshToken(Long userId, String username) {
        return jwtTokenProvider.generateRefreshToken(userId, username);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    public String getJtiFromToken(String token) {
        return jwtTokenProvider.getJtiFromToken(token);
    }

    public boolean isRefreshToken(String token) {
        return jwtTokenProvider.isRefreshToken(token);
    }

}
