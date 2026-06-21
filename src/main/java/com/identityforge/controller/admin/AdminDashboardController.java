package com.identityforge.controller.admin;

import com.identityforge.dto.response.ApiResponse;
import com.identityforge.dto.response.DashboardResponse;
import com.identityforge.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }

    @GetMapping("/dashboard/registration-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getRegistrationTrend() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRegistrationTrend()));
    }

    @GetMapping("/dashboard/security-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getSecurityAlerts() {
        var dashboard = dashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard.getSecurityAlert()));
    }
}
