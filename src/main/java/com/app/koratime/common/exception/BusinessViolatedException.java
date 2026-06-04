package com.app.koratime.common.exception;


/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 400.
 */
public class BusinessViolatedException extends RuntimeException {

    public BusinessViolatedException(String message) {
        super(message);
    }
}
