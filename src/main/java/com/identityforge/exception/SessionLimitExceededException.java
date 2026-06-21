package com.identityforge.exception;

public class SessionLimitExceededException extends RuntimeException {
    public SessionLimitExceededException(String message) {
        super(message);
    }
}
