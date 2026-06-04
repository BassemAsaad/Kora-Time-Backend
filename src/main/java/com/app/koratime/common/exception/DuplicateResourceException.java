package com.app.koratime.common.exception;

/**
 * Thrown when a unique constraint would be violated at the service level, before hitting the DB (fail fast).
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, String value) {
        super(resource + " already exists with " + field + ": " + value);
    }
}
