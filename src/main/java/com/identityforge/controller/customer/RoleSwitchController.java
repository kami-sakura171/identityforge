package com.identityforge.controller.customer;

import com.identityforge.dto.request.RoleSwitchRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.dto.response.JwtResponse;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.customer.RoleSwitchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/roles")
@RequiredArgsConstructor
public class RoleSwitchController {

    private final RoleSwitchService roleSwitchService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<String>>> getRoles() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(roleSwitchService.getAvailableRoles(userId)));
    }

    @PostMapping("/switch")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<JwtResponse>> switchRole(
            @Valid @RequestBody RoleSwitchRequest request,
            HttpServletRequest httpRequest) {
        Long userId = SecurityUtils.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        JwtResponse response = roleSwitchService.switchRole(userId,
                request.getContextualRole(), ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Role switched", response));
    }
}
