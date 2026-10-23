package com.lms.lms_backend.dto.response;

import com.lms.lms_backend.model.QuestionOption;

// NOTE: The `correct` field is intentionally included in this response for
// admin/instructor use. When student-facing endpoints are added after security
// is implemented, a separate OptionStudentResponse (without `correct`) will be used.
public class OptionResponse {

    private Long id;
    private String optionText;
    private boolean correct;

    public static OptionResponse from(QuestionOption o) {
        OptionResponse r = new OptionResponse();
        r.id = o.getId();
        r.optionText = o.getOptionText();
        r.correct = o.isCorrect();
        return r;
    }

    public Long getId() { return id; }
    public String getOptionText() { return optionText; }
    public boolean isCorrect() { return correct; }
}
