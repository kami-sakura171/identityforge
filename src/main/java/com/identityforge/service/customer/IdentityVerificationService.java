package com.identityforge.service.customer;

import com.identityforge.dto.request.VerificationSubmitRequest;
import com.identityforge.exception.BadRequestException;
import com.identityforge.model.IdentityVerification;
import com.identityforge.model.User;
import com.identityforge.model.VerificationHistory;
import com.identityforge.model.enums.DocumentType;
import com.identityforge.model.enums.VerificationStatus;
import com.identityforge.repository.IdentityVerificationRepository;
import com.identityforge.repository.UserRepository;
import com.identityforge.repository.VerificationHistoryRepository;
import com.identityforge.service.common.AuditLogService;
import com.identityforge.service.common.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityVerificationService {

    private final IdentityVerificationRepository verificationRepository;
    private final VerificationHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    @Transactional
    public IdentityVerification submitVerification(Long userId, VerificationSubmitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Validate document type
        DocumentType docType;
        try {
            docType = DocumentType.valueOf(request.getDocumentType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid document type");
        }

        IdentityVerification verification = verificationRepository.findByUserId(userId)
                .orElse(IdentityVerification.builder().user(user).build());

        verification.setDocumentType(docType);
        verification.setDocumentNumberEncrypted(encryptionService.encrypt(request.getDocumentNumber()));
        verification.setRealName(request.getRealName());
        verification.setStatus(VerificationStatus.PENDING);
        verification.setSubmittedAt(LocalDateTime.now());

        verification = verificationRepository.save(verification);

        // Record history
        recordHistory(verification, null, VerificationStatus.PENDING, user, "Verification submitted");

        auditLogService.log(userId, "VERIFICATION_SUBMITTED", "IdentityVerification",
                verification.getId(), "Document type: " + docType);
        log.info("Identity verification submitted for user: {}", user.getUsername());

        return verification;
    }

    @Transactional(readOnly = true)
    public IdentityVerification getVerification(Long userId) {
        return verificationRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public IdentityVerification reviewVerification(Long verificationId, VerificationStatus newStatus,
                                                    String comment, User reviewer) {
        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new BadRequestException("Verification not found"));

        VerificationStatus oldStatus = verification.getStatus();
        verification.setStatus(newStatus);
        verification.setReviewedBy(reviewer);
        verification.setReviewedAt(LocalDateTime.now());
        verification = verificationRepository.save(verification);

        recordHistory(verification, oldStatus, newStatus, reviewer, comment);

        auditLogService.log(reviewer.getId(), "VERIFICATION_REVIEWED", "IdentityVerification",
                verification.getId(), "Status: " + oldStatus + " -> " + newStatus);
        log.info("Verification {} reviewed: {} by admin {}",
                verificationId, newStatus, reviewer.getUsername());

        return verification;
    }

    private void recordHistory(IdentityVerification verification, VerificationStatus from,
                                VerificationStatus to, User changedBy, String comment) {
        VerificationHistory history = VerificationHistory.builder()
                .verification(verification)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .comment(comment)
                .build();
        historyRepository.save(history);
    }
}
