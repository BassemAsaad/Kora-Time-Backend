package com.app.koratime.common.exception;

import java.util.UUID;

/**
 * Thrown when an entity doesn't exist in the DB.
 * Maps to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, UUID id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(resourceName + " not found with " + field + ": " + value);
    }
}
