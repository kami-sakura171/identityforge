package com.identityforge.controller.customer;

import com.identityforge.dto.response.ApiResponse;
import com.identityforge.model.Avatar;
import com.identityforge.security.SecurityUtils;
import com.identityforge.service.customer.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/avatar")
@RequiredArgsConstructor
public class CustomerAvatarController {

    private final AvatarService avatarService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        Avatar avatar = avatarService.uploadAvatar(userId, file);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "id", avatar.getId(),
                "filename", avatar.getStoredFilename(),
                "url", "/avatars/" + avatar.getStoredFilename(),
                "size", avatar.getFileSize()
        )));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvatar() {
        Long userId = SecurityUtils.getCurrentUserId();
        Avatar avatar = avatarService.getAvatar(userId);
        if (avatar == null) {
            return ResponseEntity.ok(ApiResponse.success("No avatar", null));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "id", avatar.getId(),
                "filename", avatar.getStoredFilename(),
                "url", "/avatars/" + avatar.getStoredFilename(),
                "size", avatar.getFileSize()
        )));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar() {
        Long userId = SecurityUtils.getCurrentUserId();
        avatarService.deleteAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success("Avatar deleted", null));
    }
}
