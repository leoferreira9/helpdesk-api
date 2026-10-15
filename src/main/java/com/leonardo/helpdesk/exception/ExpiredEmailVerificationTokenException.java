package com.leonardo.helpdesk.exception;

public class ExpiredEmailVerificationTokenException extends RuntimeException {
    public ExpiredEmailVerificationTokenException(String message) {
        super(message);
    }
}
