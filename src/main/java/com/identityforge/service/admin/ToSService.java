package com.identityforge.service.admin;

import com.identityforge.dto.request.ToSPublishRequest;
import com.identityforge.exception.BadRequestException;
import com.identityforge.model.ToSVersion;
import com.identityforge.model.User;
import com.identityforge.repository.ToSVersionRepository;
import com.identityforge.repository.UserRepository;
import com.identityforge.service.common.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToSService {

    private final ToSVersionRepository tosVersionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ToSVersion> getAllVersions() {
        return tosVersionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ToSVersion getVersion(Long id) {
        return tosVersionRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("ToS version not found"));
    }

    @Transactional
    public ToSVersion publishVersion(ToSPublishRequest request, Long adminId) {
        // Check version uniqueness
        if (tosVersionRepository.findByVersion(request.getVersion()).isPresent()) {
            throw new BadRequestException("Version " + request.getVersion() + " already exists");
        }

        // Deactivate current active version
        tosVersionRepository.deactivateCurrentVersion();

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        ToSVersion tosVersion = ToSVersion.builder()
                .version(request.getVersion())
                .title(request.getTitle())
                .content(request.getContent())
                .publishedBy(admin)
                .publishedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        tosVersion = tosVersionRepository.save(tosVersion);

        auditLogService.log(adminId, "TOS_PUBLISHED", "ToSVersion", tosVersion.getId(),
                "Published ToS v" + request.getVersion());
        log.info("ToS v{} published by admin {}", request.getVersion(), adminId);

        return tosVersion;
    }
}
