package com.identityforge.service.customer;

import com.identityforge.model.Consent;
import com.identityforge.model.ToSVersion;
import com.identityforge.model.User;
import com.identityforge.repository.ConsentRepository;
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
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final ToSVersionRepository tosVersionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public ToSVersion getCurrentToS() {
        return tosVersionRepository.findByIsActiveTrue().orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Consent> getConsentHistory(Long userId) {
        return consentRepository.findByUserIdOrderByAcceptedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasAcceptedCurrentToS(Long userId) {
        ToSVersion current = getCurrentToS();
        if (current == null) return true;
        return consentRepository.existsByUserIdAndTosVersionId(userId, current.getId());
    }

    @Transactional
    public Consent acceptToS(Long userId, String ipAddress) {
        ToSVersion current = getCurrentToS();
        if (current == null) {
            throw new IllegalStateException("No active Terms of Service version found");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Consent consent = Consent.builder()
                .user(user)
                .tosVersion(current)
                .acceptedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();
        consent = consentRepository.save(consent);

        auditLogService.log(userId, "TOS_ACCEPTED", "ToSVersion", current.getId(),
                "Accepted ToS v" + current.getVersion());
        log.info("User {} accepted ToS v{}", user.getUsername(), current.getVersion());

        return consent;
    }
}
