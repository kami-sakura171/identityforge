package com.identityforge.repository;

import com.identityforge.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    long countByUserIdAndIsActiveTrue(Long userId);

    List<Session> findByUserIdAndIsActiveTrueOrderByCreatedAtAsc(Long userId);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.expiresAt <= :now AND s.isActive = true")
    int deactivateExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.id = :sessionId")
    int deactivateSession(@Param("sessionId") String sessionId);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.user.id = :userId AND s.isActive = true AND s.isRefreshToken = false")
    int deactivateAllSessionsForUser(@Param("userId") Long userId);
}
