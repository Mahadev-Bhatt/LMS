package com.lms.lms_backend.dto.request;

import jakarta.validation.constraints.NotNull;

// DTOs decouple the API contract from the entity structure.
// The client sends IDs; the service resolves them to real entities.
public class EnrollmentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    public EnrollmentRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}
