package com.identityforge.repository;

import com.identityforge.model.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Integer> {

    List<SecurityQuestion> findByIsActiveTrue();
}
