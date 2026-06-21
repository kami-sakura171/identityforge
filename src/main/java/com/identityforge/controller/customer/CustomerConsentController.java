package com.identityforge.controller.customer;

import com.identityforge.dto.response.ApiResponse;
import com.identityforge.model.Consent;
import com.identityforge.model.ToSVersion;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.customer.ConsentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/consents")
@RequiredArgsConstructor
public class CustomerConsentController {

    private final ConsentService consentService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Consent>>> getHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(consentService.getConsentHistory(userId)));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Long userId = SecurityUtils.getCurrentUserId();
        ToSVersion current = consentService.getCurrentToS();
        boolean accepted = consentService.hasAcceptedCurrentToS(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "currentVersion", current != null ? current.getVersion() : null,
                "accepted", accepted
        )));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> accept(
            HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String ipAddress = request.getRemoteAddr();
        Consent consent = consentService.acceptToS(userId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Terms of Service accepted", Map.of(
                "version", consent.getTosVersion().getVersion(),
                "acceptedAt", consent.getAcceptedAt().toString()
        )));
    }
}
