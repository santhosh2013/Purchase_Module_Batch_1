package com.example.purchase.exception;

public class DuplicateEventException extends RuntimeException {

    public DuplicateEventException(String message) {
        super(message);
    }

    public DuplicateEventException(Integer eventId) {
        super(String.format("G cross number %d already exists. Please use a different G cross number.", eventId));
    }
}
