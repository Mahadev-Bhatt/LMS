package com.lms.lms_backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "learning_material_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "material_id"})
)
public class LearningMaterialProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private LearningMaterial material;

    @Column(nullable = false, updatable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.completedAt = LocalDateTime.now();
    }

    public LearningMaterialProgress() {}

    public Long getId() { return id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public LearningMaterial getMaterial() { return material; }
    public void setMaterial(LearningMaterial material) { this.material = material; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
