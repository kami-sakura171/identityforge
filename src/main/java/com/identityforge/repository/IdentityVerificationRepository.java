package com.identityforge.repository;

import com.identityforge.model.IdentityVerification;
import com.identityforge.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, Long> {

    Optional<IdentityVerification> findByUserId(Long userId);

    List<IdentityVerification> findByStatus(VerificationStatus status);
}
