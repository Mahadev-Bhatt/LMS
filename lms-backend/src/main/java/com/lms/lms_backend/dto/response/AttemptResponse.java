package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.enums.AttemptResult;
import com.lms.lms_backend.enums.AttemptStatus;
import com.lms.lms_backend.model.ExaminationAttempt;

import java.time.LocalDateTime;
import java.util.List;

public class AttemptResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long examinationId;
    private String examinationTitle;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer obtainedMarks;
    private Integer totalMarks;
    private Integer passingMarks;
    private AttemptStatus status;
    private AttemptResult result;
    private List<SubmittedAnswerResponse> submittedAnswers;

    public static AttemptResponse from(ExaminationAttempt a) {
        AttemptResponse r = new AttemptResponse();
        r.id = a.getId();
        r.studentId = a.getStudent().getId();
        r.studentName = a.getStudent().getName();
        r.examinationId = a.getExamination().getId();
        r.examinationTitle = a.getExamination().getTitle();
        r.startedAt = a.getStartedAt();
        r.submittedAt = a.getSubmittedAt();
        r.obtainedMarks = a.getObtainedMarks();
        r.totalMarks = a.getExamination().getTotalMarks();
        r.passingMarks = a.getExamination().getPassingMarks();
        r.status = a.getStatus();
        r.result = a.getResult();
        r.submittedAnswers = a.getSubmittedAnswers().stream()
                .map(SubmittedAnswerResponse::from).toList();
        return r;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Long getExaminationId() { return examinationId; }
    public String getExaminationTitle() { return examinationTitle; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public Integer getObtainedMarks() { return obtainedMarks; }
    public Integer getTotalMarks() { return totalMarks; }
    public Integer getPassingMarks() { return passingMarks; }
    public AttemptStatus getStatus() { return status; }
    public AttemptResult getResult() { return result; }
    public List<SubmittedAnswerResponse> getSubmittedAnswers() { return submittedAnswers; }
}
