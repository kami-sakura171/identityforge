package com.identityforge.controller.auth;

import com.identityforge.dto.request.LoginRequest;
import com.identityforge.dto.request.RegistrationRequest;
import com.identityforge.dto.request.TokenRefreshRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.dto.response.JwtResponse;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtResponse>> register(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        JwtResponse response = authService.register(request, ipAddress, userAgent);
        setTokenCookies(httpResponse, response);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        JwtResponse response = authService.login(request, ipAddress, userAgent);
        setTokenCookies(httpResponse, response);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    private void setTokenCookies(HttpServletResponse response, JwtResponse jwt) {
        Cookie accessCookie = new Cookie("accessToken", jwt.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(3600);
        response.addCookie(accessCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        JwtResponse response = authService.refreshToken(request.getRefreshToken(), ipAddress, userAgent);
        setTokenCookies(httpResponse, response);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest,
                                                     HttpServletResponse httpResponse) {
        String bearerToken = httpRequest.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Long userId = SecurityUtils.getCurrentUserId();
            authService.logout(token, userId);
        }
        clearTokenCookies(httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validate() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Token is valid", userId != null));
    }
}
