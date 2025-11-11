package com.example.purchase.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String currentStatus, String newStatus) {
        super(String.format("Cannot transition from status '%s' to '%s'", currentStatus, newStatus));
    }
}
