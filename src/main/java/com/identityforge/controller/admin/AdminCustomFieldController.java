package com.identityforge.controller.admin;

import com.identityforge.dto.request.CustomFieldCreateRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.model.CustomFieldDefinition;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.admin.CustomFieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/custom-fields")
@RequiredArgsConstructor
public class AdminCustomFieldController {

    private final CustomFieldService customFieldService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomFieldDefinition>>> listFields() {
        return ResponseEntity.ok(ApiResponse.success(customFieldService.getAllFields()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomFieldDefinition>> createField(
            @Valid @RequestBody CustomFieldCreateRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                customFieldService.createField(request, adminId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomFieldDefinition>> updateField(
            @PathVariable Long id,
            @Valid @RequestBody CustomFieldCreateRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                customFieldService.updateField(id, request, adminId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable Long id) {
        Long adminId = SecurityUtils.getCurrentUserId();
        customFieldService.deleteField(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Field deleted", null));
    }
}
