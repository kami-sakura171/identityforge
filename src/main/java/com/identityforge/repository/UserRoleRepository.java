package com.identityforge.repository;

import com.identityforge.model.User;
import com.identityforge.model.UserRole;
import com.identityforge.model.enums.ContextualRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);

    List<UserRole> findByUserAndIsActiveTrue(User user);

    Optional<UserRole> findByUserAndContextualRole(User user, ContextualRole contextualRole);

    @Modifying
    @Query("UPDATE UserRole ur SET ur.isActive = false WHERE ur.user = :user")
    int deactivateAllRolesForUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE UserRole ur SET ur.isActive = true WHERE ur.user = :user AND ur.contextualRole = :role")
    int activateRole(@Param("user") User user, @Param("role") ContextualRole role);

    boolean existsByUserAndContextualRole(User user, ContextualRole role);
}
