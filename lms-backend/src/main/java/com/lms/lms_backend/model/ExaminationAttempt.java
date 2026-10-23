package com.lms.lms_backend.model;

import com.lms.lms_backend.enums.AttemptResult;
import com.lms.lms_backend.enums.AttemptStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "examination_attempts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "examination_id"})
)
public class ExaminationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id", nullable = false)
    private Examination examination;

    @Column(updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private Integer obtainedMarks;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    // Null until the attempt is submitted and evaluated
    @Enumerated(EnumType.STRING)
    private AttemptResult result;

    @OneToMany(mappedBy = "examinationAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmittedAnswer> submittedAnswers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }

    public ExaminationAttempt() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Examination getExamination() { return examination; }
    public void setExamination(Examination examination) { this.examination = examination; }

    public LocalDateTime getStartedAt() { return startedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(Integer obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public AttemptResult getResult() { return result; }
    public void setResult(AttemptResult result) { this.result = result; }

    public List<SubmittedAnswer> getSubmittedAnswers() { return submittedAnswers; }
}
