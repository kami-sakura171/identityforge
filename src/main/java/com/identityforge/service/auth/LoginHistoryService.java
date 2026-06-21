package com.identityforge.service.auth;

import com.identityforge.model.LoginHistory;
import com.identityforge.model.User;
import com.identityforge.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Transactional
    public void recordLogin(User user, String ipAddress, String userAgent, boolean success, String failureReason) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .failureReason(failureReason)
                .build();
        loginHistoryRepository.save(history);

        // Enforce 50-entry cap
        loginHistoryRepository.deleteExcessEntries(user.getId());
    }

    @Transactional(readOnly = true)
    public List<LoginHistory> getLoginHistoryForUser(Long userId) {
        return loginHistoryRepository.findLast50ByUserId(userId);
    }
}
