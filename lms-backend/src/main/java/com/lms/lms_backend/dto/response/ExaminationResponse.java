package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.model.Examination;

import java.time.LocalDateTime;

public class ExaminationResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseTitle;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer passingMarks;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private LocalDateTime createdAt;

    public static ExaminationResponse from(Examination e) {
        ExaminationResponse r = new ExaminationResponse();
        r.id = e.getId();
        r.title = e.getTitle();
        r.description = e.getDescription();
        r.courseId = e.getCourse().getId();
        r.courseTitle = e.getCourse().getTitle();
        r.durationMinutes = e.getDurationMinutes();
        r.totalMarks = e.getTotalMarks();
        r.passingMarks = e.getPassingMarks();
        r.startTime = e.getStartTime();
        r.endTime = e.getEndTime();
        r.active = e.isActive();
        r.createdAt = e.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Integer getTotalMarks() { return totalMarks; }
    public Integer getPassingMarks() { return passingMarks; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
