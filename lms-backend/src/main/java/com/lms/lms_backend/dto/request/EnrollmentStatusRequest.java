package com.lms.lms_backend.dto.request;

import com.lms.lms_backend.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public class EnrollmentStatusRequest {

    @NotNull(message = "Status is required")
    private EnrollmentStatus status;

    public EnrollmentStatusRequest() {}

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
}
