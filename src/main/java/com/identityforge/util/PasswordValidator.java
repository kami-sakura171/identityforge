package com.identityforge.util;

import java.util.ArrayList;
import java.util.List;

public final class PasswordValidator {

    private static final int MIN_LENGTH = 10;
    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    private static final String DIGIT_PATTERN = ".*[0-9].*";
    private static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*";

    private PasswordValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static List<String> validate(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            violations.add("Password must not be empty");
            return violations;
        }

        if (password.length() < MIN_LENGTH) {
            violations.add("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!password.matches(UPPERCASE_PATTERN)) {
            violations.add("Password must contain at least one uppercase letter");
        }
        if (!password.matches(DIGIT_PATTERN)) {
            violations.add("Password must contain at least one digit");
        }
        if (!password.matches(SPECIAL_CHAR_PATTERN)) {
            violations.add("Password must contain at least one special character");
        }

        return violations;
    }

    public static boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}
