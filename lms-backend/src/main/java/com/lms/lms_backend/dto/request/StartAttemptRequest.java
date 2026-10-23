package com.lms.lms_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class StartAttemptRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Examination ID is required")
    private Long examinationId;

    public StartAttemptRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }
}
