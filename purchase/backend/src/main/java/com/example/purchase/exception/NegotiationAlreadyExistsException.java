package com.example.purchase.exception;

public class NegotiationAlreadyExistsException extends RuntimeException {

    public NegotiationAlreadyExistsException(String message) {
        super(message);
    }

    public NegotiationAlreadyExistsException(Integer prid) {
        super(String.format("Negotiation already exists for Purchase Request ID: %d", prid));
    }
}
