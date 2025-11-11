package com.example.purchase.exception;

public class DuplicatePRNumberException extends RuntimeException {
    public DuplicatePRNumberException(Integer prNumber) {
        super("PR Number " + prNumber + " already exists. Please use a different PR Number.");
    }
}
