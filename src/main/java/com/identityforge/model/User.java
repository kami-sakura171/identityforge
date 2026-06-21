package com.identityforge.model;

import com.identityforge.model.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "security_question_id")
    private Integer securityQuestionId;

    @Column(name = "security_answer_hash", length = 255)
    private String securityAnswerHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "lockout_until")
    private LocalDateTime lockoutUntil;

    @Column(name = "roles_bitmask", nullable = false)
    @Builder.Default
    private Integer rolesBitmask = 1; // bit 0 = CUSTOMER, bit 1 = ADMIN

    @Column(name = "force_password_reset", nullable = false)
    @Builder.Default
    private Boolean forcePasswordReset = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isLocked() {
        return accountStatus == AccountStatus.LOCKED &&
               lockoutUntil != null &&
               lockoutUntil.isAfter(LocalDateTime.now());
    }

    public boolean isAdmin() {
        return (rolesBitmask & 2) != 0;
    }

    public boolean isCustomer() {
        return (rolesBitmask & 1) != 0;
    }
}
