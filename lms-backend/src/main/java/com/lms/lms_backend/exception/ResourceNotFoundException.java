package com.lms.lms_backend.exception;

// RuntimeException means Spring doesn't force callers to catch it (unchecked).
// We'll map this to HTTP 404 in the GlobalExceptionHandler.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
