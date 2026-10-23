package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.enums.MaterialType;
import com.lms.lms_backend.model.LearningMaterial;

import java.time.LocalDateTime;

public class MaterialResponse {

    private Long id;
    private String title;
    private String description;
    private MaterialType materialType;
    private String resourceUrl;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime createdAt;

    public static MaterialResponse from(LearningMaterial m) {
        MaterialResponse r = new MaterialResponse();
        r.id = m.getId();
        r.title = m.getTitle();
        r.description = m.getDescription();
        r.materialType = m.getMaterialType();
        r.resourceUrl = m.getResourceUrl();
        r.courseId = m.getCourse().getId();
        r.courseTitle = m.getCourse().getTitle();
        r.createdAt = m.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MaterialType getMaterialType() { return materialType; }
    public String getResourceUrl() { return resourceUrl; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
