package com.leonardo.helpdesk.exception;

public class TechnicianAlreadySignedException extends RuntimeException {
    public TechnicianAlreadySignedException(String message) {
        super(message);
    }
}
