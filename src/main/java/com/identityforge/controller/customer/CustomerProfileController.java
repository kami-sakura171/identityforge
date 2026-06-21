package com.identityforge.controller.customer;

import com.identityforge.dto.request.PasswordChangeRequest;
import com.identityforge.dto.request.ProfileUpdateRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.dto.response.UserProfileResponse;
import com.identityforge.security.SecurityUtils;
import com.identityforge.security.UserPrincipal;
import com.identityforge.service.customer.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService profileService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(principal)));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(principal, request)));
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        profileService.changePassword(principal, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
