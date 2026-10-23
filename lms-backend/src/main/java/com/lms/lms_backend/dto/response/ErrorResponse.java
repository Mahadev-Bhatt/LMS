package com.lms.lms_backend.dto.response;

import java.time.LocalDateTime;

// This is the standard error body returned for every error response.
// Clients always get the same JSON structure regardless of the error type.
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
