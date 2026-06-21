package com.identityforge.service.admin;

import com.identityforge.dto.response.ImportResultResponse;
import com.identityforge.exception.BadRequestException;
import com.identityforge.model.NotificationPreference;
import com.identityforge.model.User;
import com.identityforge.model.UserRole;
import com.identityforge.model.enums.ContextualRole;
import com.identityforge.repository.*;
import com.identityforge.service.common.AuditLogService;
import com.identityforge.util.CsvParser;
import com.identityforge.util.DateUtils;
import com.identityforge.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchImportService {

    private static final int MAX_ROWS = 1000;
    private static final List<String> REQUIRED_HEADERS = List.of(
            "username", "password", "first_name", "last_name",
            "display_name", "date_of_birth", "security_question_id", "security_answer");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public ImportResultResponse importCsv(MultipartFile file, Long adminId) {
        try {
            var parseResult = CsvParser.parse(file.getInputStream(), REQUIRED_HEADERS);
            List<Map<String, String>> rows = parseResult.validRows();
            List<CsvParser.RowError> parseErrors = parseResult.errors();

            if (rows.size() > MAX_ROWS) {
                throw new BadRequestException("CSV contains more than " + MAX_ROWS + " rows (max: " + MAX_ROWS + ")");
            }

            List<ImportResultResponse.ImportFailure> failures = new ArrayList<>();
            parseErrors.forEach(e -> failures.add(
                    ImportResultResponse.ImportFailure.builder().row(e.row()).reason(e.reason()).build()));

            int successCount = 0;
            int rowNum = 1; // header is row 0
            for (Map<String, String> row : rows) {
                rowNum++;
                try {
                    validateAndCreateUser(row);
                    successCount++;
                } catch (Exception e) {
                    failures.add(ImportResultResponse.ImportFailure.builder()
                            .row(rowNum).reason(e.getMessage()).build());
                }
            }

            auditLogService.log(adminId, "CSV_IMPORT", "User", null,
                    "Imported " + successCount + "/" + rows.size() + " users from CSV");
            log.info("CSV import: {} success, {} failures (by admin {})", successCount, failures.size(), adminId);

            return ImportResultResponse.builder()
                    .totalRows(rows.size())
                    .successCount(successCount)
                    .failureCount(failures.size())
                    .failures(failures)
                    .build();
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse CSV: " + e.getMessage());
        }
    }

    private void validateAndCreateUser(Map<String, String> row) {
        String username = row.get("username");
        String password = row.get("password");

        // Validate username
        if (username == null || username.length() < 4) {
            throw new BadRequestException("Username must be at least 4 characters");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }

        // Validate password
        List<String> pwErrors = PasswordValidator.validate(password);
        if (!pwErrors.isEmpty()) {
            throw new BadRequestException("Password: " + String.join(", ", pwErrors));
        }

        // Validate date of birth
        LocalDate dob;
        try {
            dob = DateUtils.parseDate(row.get("date_of_birth"));
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Use MM/DD/YYYY");
        }

        // Create user
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(row.get("first_name"))
                .lastName(row.get("last_name"))
                .displayName(row.get("display_name") != null ? row.get("display_name") : username)
                .dateOfBirth(dob)
                .securityQuestionId(parseInt(row.get("security_question_id"), 1))
                .securityAnswerHash(passwordEncoder.encode(
                        (row.get("security_answer") != null ? row.get("security_answer") : "").toLowerCase().trim()))
                .build();
        user = userRepository.save(user);

        // Assign default role
        UserRole userRole = UserRole.builder()
                .user(user)
                .contextualRole(ContextualRole.STANDARD_USER)
                .isActive(true)
                .build();
        userRoleRepository.save(userRole);

        // Initialize notification preferences
        for (String category : NotificationPreference.DEFAULT_CATEGORIES) {
            NotificationPreference pref = NotificationPreference.builder()
                    .user(user)
                    .category(category)
                    .enabled(true)
                    .build();
            notificationPreferenceRepository.save(pref);
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
