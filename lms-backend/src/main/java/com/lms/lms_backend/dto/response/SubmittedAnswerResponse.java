package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.model.SubmittedAnswer;

public class SubmittedAnswerResponse {

    private Long id;
    private Long questionId;
    private String questionText;
    private Long selectedOptionId;
    private String selectedOptionText;

    public static SubmittedAnswerResponse from(SubmittedAnswer sa) {
        SubmittedAnswerResponse r = new SubmittedAnswerResponse();
        r.id = sa.getId();
        r.questionId = sa.getQuestion().getId();
        r.questionText = sa.getQuestion().getQuestionText();
        if (sa.getSelectedOption() != null) {
            r.selectedOptionId = sa.getSelectedOption().getId();
            r.selectedOptionText = sa.getSelectedOption().getOptionText();
        }
        return r;
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public String getSelectedOptionText() { return selectedOptionText; }
}
