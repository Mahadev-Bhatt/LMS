package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.enums.QuestionType;
import com.lms.lms_backend.model.Question;

import java.util.List;

public record StudentQuestionResponse(
        Long id,
        String questionText,
        Integer marks,
        QuestionType questionType,
        List<Option> options) {

    public static StudentQuestionResponse from(Question question) {
        return new StudentQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getMarks(),
                question.getQuestionType(),
                question.getOptions().stream()
                        .map(option -> new Option(option.getId(), option.getOptionText()))
                        .toList());
    }

    public record Option(Long id, String optionText) {}
}
