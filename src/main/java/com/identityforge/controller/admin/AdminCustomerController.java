package com.identityforge.controller.admin;

import com.identityforge.dto.response.ApiResponse;
import com.identityforge.dto.response.PagedResponse;
import com.identityforge.dto.response.UserProfileResponse;
import com.identityforge.model.AuditLog;
import com.identityforge.model.LoginHistory;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.admin.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService customerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserProfileResponse>>> listCustomers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(customerService.searchCustomers(q, page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCustomerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerDetail(id)));
    }

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> lockAccount(@PathVariable Long id) {
        Long adminId = SecurityUtils.getCurrentUserId();
        customerService.lockAccount(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Account locked", null));
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long id) {
        Long adminId = SecurityUtils.getCurrentUserId();
        customerService.unlockAccount(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked", null));
    }

    @PostMapping("/{id}/force-password-reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> forcePasswordReset(@PathVariable Long id) {
        Long adminId = SecurityUtils.getCurrentUserId();
        customerService.forcePasswordReset(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Password reset forced", null));
    }

    @GetMapping("/{id}/login-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoginHistory>>> getLoginHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getLoginHistory(id)));
    }

    @GetMapping("/{id}/audit-log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLog(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getAuditLog(id, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/custom-fields")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomFieldValues(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomFieldValues(id)));
    }

    @PutMapping("/{id}/custom-fields")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateCustomFieldValues(
            @PathVariable Long id,
            @RequestBody Map<String, String> fieldValues) {
        Long adminId = SecurityUtils.getCurrentUserId();
        customerService.updateCustomFieldValues(id, fieldValues, adminId);
        return ResponseEntity.ok(ApiResponse.success("Custom fields updated", null));
    }
}
