package com.identityforge.controller.common;

import com.identityforge.model.SecurityQuestion;
import com.identityforge.repository.SecurityQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/security-questions")
@RequiredArgsConstructor
public class SecurityQuestionController {

    private final SecurityQuestionRepository securityQuestionRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getQuestions() {
        List<Map<String, Object>> questions = securityQuestionRepository.findByIsActiveTrue()
                .stream()
                .map(sq -> Map.<String, Object>of("id", sq.getId(), "text", sq.getQuestionText()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }
}
