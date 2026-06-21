package com.identityforge.repository;

import com.identityforge.model.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    Optional<Avatar> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
