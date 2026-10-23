package com.lms.lms_backend.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "submitted_answers",
    uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
)
public class SubmittedAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExaminationAttempt examinationAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // The option the student selected — nullable because a student may skip a question
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    public SubmittedAnswer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExaminationAttempt getExaminationAttempt() { return examinationAttempt; }
    public void setExaminationAttempt(ExaminationAttempt examinationAttempt) {
        this.examinationAttempt = examinationAttempt;
    }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public QuestionOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(QuestionOption selectedOption) { this.selectedOption = selectedOption; }
}
