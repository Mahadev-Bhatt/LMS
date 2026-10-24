package com.lms.lms_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class StartAttemptRequest {

    @NotNull(message = "Examination ID is required")
    private Long examinationId;

    public StartAttemptRequest() {}

    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
}
