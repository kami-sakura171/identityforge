package com.identityforge.service.customer;

import com.identityforge.exception.BadRequestException;
import com.identityforge.exception.FileStorageException;
import com.identityforge.model.Avatar;
import com.identityforge.model.User;
import com.identityforge.repository.AvatarRepository;
import com.identityforge.repository.UserRepository;
import com.identityforge.service.common.AuditLogService;
import com.identityforge.service.common.FileStorageService;
import com.identityforge.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @Value("${app.avatar.max-size:2097152}")
    private long maxSize;

    @Value("${app.avatar.allowed-types:image/jpeg,image/png}")
    private String allowedTypes;

    @Transactional
    public Avatar uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Validate file type
        String contentType = file.getContentType();
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        if (!allowed.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Only JPEG and PNG are allowed.");
        }

        // Validate file size
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File too large. Maximum size is " + (maxSize / 1024 / 1024) + " MB.");
        }

        // Compute SHA-256 hash
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new FileStorageException("Failed to read file");
        }
        String hashHex = HashUtils.sha256Hex(fileBytes);

        // Determine extension
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String storedFilename = hashHex + extension;

        // Store file (dedup if same hash already exists)
        if (!fileStorageService.exists(storedFilename)) {
            fileStorageService.store(storedFilename, file);
        }

        // Create or update avatar record
        Avatar avatar = avatarRepository.findByUserId(userId)
                .orElse(Avatar.builder().user(user).build());

        avatar.setOriginalFilename(file.getOriginalFilename());
        avatar.setStoredFilename(storedFilename);
        avatar.setFileSize(file.getSize());
        avatar.setContentType(contentType);

        avatar = avatarRepository.save(avatar);
        auditLogService.log(userId, "AVATAR_UPLOAD", "Avatar", avatar.getId(), "Avatar uploaded");
        log.info("Avatar uploaded for user {}: {}", user.getUsername(), storedFilename);

        return avatar;
    }

    @Transactional(readOnly = true)
    public Avatar getAvatar(Long userId) {
        return avatarRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public void deleteAvatar(Long userId) {
        Avatar avatar = avatarRepository.findByUserId(userId).orElse(null);
        if (avatar != null) {
            avatarRepository.deleteByUserId(userId);
            auditLogService.log(userId, "AVATAR_DELETE", "Avatar", avatar.getId(), "Avatar deleted");
            log.info("Avatar deleted for user {}", userId);
        }
    }
}
