package com.identityforge.repository;

import com.identityforge.model.CustomFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinition, Long> {

    List<CustomFieldDefinition> findByIsActiveTrueOrderByDisplayOrder();

    long countByIsActiveTrue();

    boolean existsByNameAndIsActiveTrue(String name);
}
