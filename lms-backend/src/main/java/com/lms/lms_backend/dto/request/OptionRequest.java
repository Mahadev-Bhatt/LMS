package com.lms.lms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OptionRequest {

    @NotBlank(message = "Option text is required")
    private String optionText;

    private boolean correct;

    public OptionRequest() {}

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
