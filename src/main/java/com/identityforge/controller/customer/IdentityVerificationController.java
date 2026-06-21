package com.identityforge.controller.customer;

import com.identityforge.dto.request.VerificationSubmitRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.model.IdentityVerification;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.customer.IdentityVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/verification")
@RequiredArgsConstructor
public class IdentityVerificationController {

    private final IdentityVerificationService verificationService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submit(
            @Valid @RequestBody VerificationSubmitRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        IdentityVerification verification = verificationService.submitVerification(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Verification submitted", Map.of(
                "id", verification.getId(),
                "status", verification.getStatus().name()
        )));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<IdentityVerification>> getStatus() {
        Long userId = SecurityUtils.getCurrentUserId();
        IdentityVerification verification = verificationService.getVerification(userId);
        return ResponseEntity.ok(ApiResponse.success(verification));
    }
}
