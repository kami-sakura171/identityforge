package com.identityforge.repository;

import com.identityforge.model.User;
import com.identityforge.model.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByAccountStatus(AccountStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countUsersCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT DATE(u.createdAt) as regDate, COUNT(u) as cnt " +
           "FROM User u WHERE u.createdAt >= :since " +
           "GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> countRegistrationsByDaySince(@Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1, u.updatedAt = NOW() WHERE u.username = :username")
    int incrementFailedAttempts(@Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.accountStatus = 'LOCKED', u.lockoutUntil = :lockoutUntil, u.updatedAt = NOW() " +
           "WHERE u.username = :username")
    int lockAccount(@Param("username") String username, @Param("lockoutUntil") LocalDateTime lockoutUntil);

    @Modifying
    @Query("UPDATE User u SET u.accountStatus = 'ACTIVE', u.failedAttempts = 0, u.lockoutUntil = NULL, " +
           "u.updatedAt = NOW() WHERE u.accountStatus = 'LOCKED' AND u.lockoutUntil <= :now")
    int unlockExpiredAccounts(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.updatedAt = NOW() WHERE u.username = :username")
    int resetFailedAttempts(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:query% OR u.displayName LIKE %:query%")
    Page<User> searchByUsernameOrDisplayName(@Param("query") String query, Pageable pageable);

    List<User> findByAccountStatus(AccountStatus status);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'LOCKED' AND u.lockoutUntil IS NOT NULL AND u.lockoutUntil > :now")
    List<User> findCurrentlyLockedUsers(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.id = :userId")
    int setAccountStatus(@Param("userId") Long userId, @Param("status") AccountStatus status);

    @Modifying
    @Query("UPDATE User u SET u.forcePasswordReset = :force WHERE u.id = :userId")
    int setForcePasswordReset(@Param("userId") Long userId, @Param("force") boolean force);
}
