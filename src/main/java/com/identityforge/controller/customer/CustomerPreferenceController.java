package com.identityforge.controller.customer;

import com.identityforge.dto.response.ApiResponse;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.customer.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/preferences")
@RequiredArgsConstructor
public class CustomerPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getPreferences() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getPreferences(userId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> updatePreferences(
            @RequestBody Map<String, Boolean> preferences) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                preferenceService.updatePreferences(userId, preferences)));
    }
}
