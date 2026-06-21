package com.identityforge.controller.admin;

import com.identityforge.dto.request.ToSPublishRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.model.ToSVersion;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.admin.ToSService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tos")
@RequiredArgsConstructor
public class AdminToSController {

    private final ToSService tosService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ToSVersion>>> listVersions() {
        return ResponseEntity.ok(ApiResponse.success(tosService.getAllVersions()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ToSVersion>> getVersion(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tosService.getVersion(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ToSVersion>> publishVersion(
            @Valid @RequestBody ToSPublishRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(tosService.publishVersion(request, adminId)));
    }
}
