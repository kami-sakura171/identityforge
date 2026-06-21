package com.identityforge.util;

public final class SecurityQuestionProvider {

    public static final String[] QUESTIONS = {
        "What was the name of your first pet?",
        "What is your mother's maiden name?",
        "What was the name of your elementary school?",
        "What was your childhood nickname?",
        "In what city were you born?",
        "What is the name of your favorite childhood friend?",
        "What was the make and model of your first car?",
        "What is your favorite movie?"
    };

    private SecurityQuestionProvider() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static int getQuestionCount() {
        return QUESTIONS.length;
    }

    public static String getQuestion(int index) {
        if (index < 0 || index >= QUESTIONS.length) {
            throw new IllegalArgumentException("Invalid question index: " + index);
        }
        return QUESTIONS[index];
    }
}
