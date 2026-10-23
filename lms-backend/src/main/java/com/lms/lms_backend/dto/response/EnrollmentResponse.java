package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.enums.EnrollmentStatus;
import com.lms.lms_backend.model.Enrollment;

import java.time.LocalDateTime;

// We use a response DTO here because the Enrollment entity holds lazy-loaded
// User and Course objects. Returning the entity directly can trigger
// unintended extra queries or serialization errors with LAZY proxies.
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;

    public static EnrollmentResponse from(Enrollment e) {
        EnrollmentResponse r = new EnrollmentResponse();
        r.id = e.getId();
        r.studentId = e.getStudent().getId();
        r.studentName = e.getStudent().getName();
        r.courseId = e.getCourse().getId();
        r.courseTitle = e.getCourse().getTitle();
        r.enrollmentDate = e.getEnrollmentDate();
        r.status = e.getStatus();
        return r;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public EnrollmentStatus getStatus() { return status; }
}
