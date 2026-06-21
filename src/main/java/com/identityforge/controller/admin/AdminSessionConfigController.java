package com.identityforge.controller.admin;

import com.identityforge.dto.request.SessionConfigRequest;
import com.identityforge.dto.response.ApiResponse;
import com.identityforge.model.SystemSetting;
import com.identityforge.repository.SystemSettingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/session-config")
@RequiredArgsConstructor
public class AdminSessionConfigController {

    private final SystemSettingRepository systemSettingRepository;

    @Value("${app.session.default-timeout-minutes:30}")
    private int defaultTimeout;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfig() {
        var setting = systemSettingRepository.findBySettingKey("session_timeout");
        int timeout = setting.map(s -> Integer.parseInt(s.getSettingValue())).orElse(defaultTimeout);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "timeoutMinutes", timeout,
                "defaultTimeoutMinutes", defaultTimeout
        )));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateConfig(
            @Valid @RequestBody SessionConfigRequest request) {
        var setting = systemSettingRepository.findBySettingKey("session_timeout")
                .orElse(SystemSetting.builder()
                        .settingKey("session_timeout")
                        .description("Session timeout in minutes")
                        .build());
        setting.setSettingValue(String.valueOf(request.getTimeoutMinutes()));
        systemSettingRepository.save(setting);

        return ResponseEntity.ok(ApiResponse.success("Session timeout updated", Map.of(
                "timeoutMinutes", request.getTimeoutMinutes()
        )));
    }
}
