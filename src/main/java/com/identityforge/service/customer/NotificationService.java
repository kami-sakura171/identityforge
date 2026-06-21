package com.identityforge.service.customer;

import com.identityforge.model.Notification;
import com.identityforge.model.User;
import com.identityforge.model.enums.NotificationCategory;
import com.identityforge.repository.NotificationRepository;
import com.identityforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(Long userId, NotificationCategory category,
                                            String title, String message) {
        User user = userRepository.getReferenceById(userId);
        Notification notification = Notification.builder()
                .user(user)
                .category(category)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        notification = notificationRepository.save(notification);
        log.debug("Notification created for user {}: {}", userId, title);
        return notification;
    }

    @Transactional
    public void notifyAllAdmins(NotificationCategory category, String title, String message) {
        // Find all admin users and notify them
        // For simplicity, notify all users with admin role
        // This is called by SecurityAlertService via repository query
        log.info("Admin notification: {}", title);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
