package com.identityforge.repository;

import com.identityforge.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {

    List<Consent> findByUserIdOrderByAcceptedAtDesc(Long userId);

    Optional<Consent> findByUserIdAndTosVersionId(Long userId, Long tosVersionId);

    boolean existsByUserIdAndTosVersionId(Long userId, Long tosVersionId);
}
