package com.leonardo.helpdesk.exception;

public class TicketAlreadyAssignedException extends RuntimeException {
    public TicketAlreadyAssignedException(String message) {
        super(message);
    }
}
