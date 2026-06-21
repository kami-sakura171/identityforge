package com.identityforge.repository;

import com.identityforge.model.FailedLoginEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FailedLoginEventRepository extends JpaRepository<FailedLoginEvent, Long> {

    @Query("SELECT COUNT(DISTINCT f.username) FROM FailedLoginEvent f WHERE f.attemptTime >= :since")
    long countDistinctUsernamesSince(@Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM FailedLoginEvent f WHERE f.attemptTime < :before")
    int deleteOldEvents(@Param("before") LocalDateTime before);
}
