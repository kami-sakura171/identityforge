package com.identityforge.repository;

import com.identityforge.model.ToSVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToSVersionRepository extends JpaRepository<ToSVersion, Long> {

    Optional<ToSVersion> findByIsActiveTrue();

    Optional<ToSVersion> findByVersion(String version);

    @Modifying
    @Query("UPDATE ToSVersion t SET t.isActive = false WHERE t.isActive = true")
    int deactivateCurrentVersion();
}
