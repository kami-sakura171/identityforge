package com.identityforge.repository;

import com.identityforge.model.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {

    List<CustomFieldValue> findByUserId(Long userId);

    Optional<CustomFieldValue> findByUserIdAndFieldDefinitionId(Long userId, Long fieldDefinitionId);

    void deleteByUserId(Long userId);
}
