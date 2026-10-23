package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.enums.QuestionType;
import com.lms.lms_backend.model.Question;

import java.util.List;

public class QuestionResponse {

    private Long id;
    private String questionText;
    private Integer marks;
    private QuestionType questionType;
    private Long examinationId;
    private List<OptionResponse> options;

    public static QuestionResponse from(Question q) {
        QuestionResponse r = new QuestionResponse();
        r.id = q.getId();
        r.questionText = q.getQuestionText();
        r.marks = q.getMarks();
        r.questionType = q.getQuestionType();
        r.examinationId = q.getExamination().getId();
        r.options = q.getOptions().stream().map(OptionResponse::from).toList();
        return r;
    }

    public Long getId() { return id; }
    public String getQuestionText() { return questionText; }
    public Integer getMarks() { return marks; }
    public QuestionType getQuestionType() { return questionType; }
    public Long getExaminationId() { return examinationId; }
    public List<OptionResponse> getOptions() { return options; }
}
