package com.lms.lms_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class AnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    // Nullable — student may skip a question
    private Long selectedOptionId;

    public AnswerRequest() {}

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
}
