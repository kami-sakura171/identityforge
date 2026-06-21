package com.identityforge.controller.admin;

import com.identityforge.dto.response.ApiResponse;
import com.identityforge.dto.response.ImportResultResponse;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.admin.BatchImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class AdminImportController {

    private final BatchImportService batchImportService;

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importCsv(
            @RequestParam("file") MultipartFile file) {
        Long adminId = SecurityUtils.getCurrentUserId();
        ImportResultResponse result = batchImportService.importCsv(file, adminId);
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }
}
