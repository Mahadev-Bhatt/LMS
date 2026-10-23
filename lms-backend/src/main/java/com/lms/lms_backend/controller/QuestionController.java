package com.lms.lms_backend.controller;

import com.lms.lms_backend.dto.request.QuestionRequest;
import com.lms.lms_backend.dto.response.QuestionResponse;
import com.lms.lms_backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/api/examinations/{examId}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(@PathVariable Long examId,
                                                         @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.addQuestion(examId, request));
    }

    @GetMapping("/api/examinations/{examId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByExamination(@PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExamination(examId));
    }

    @GetMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PutMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
