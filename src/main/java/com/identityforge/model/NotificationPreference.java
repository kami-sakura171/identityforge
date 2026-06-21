package com.identityforge.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    public static final String[] DEFAULT_CATEGORIES = {
        "Product Updates",
        "Account Alerts",
        "Security Notices",
        "Marketing",
        "Tips & Guides",
        "System Announcements"
    };
}
