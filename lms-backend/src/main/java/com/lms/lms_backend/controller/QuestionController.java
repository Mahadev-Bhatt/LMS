package com.lms.lms_backend.controller;

import com.lms.lms_backend.dto.request.QuestionRequest;
import com.lms.lms_backend.dto.response.QuestionResponse;
import com.lms.lms_backend.dto.response.StudentQuestionResponse;
import com.lms.lms_backend.service.QuestionService;
import com.lms.lms_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

    public QuestionController(QuestionService questionService, UserService userService) {
        this.questionService = questionService;
        this.userService = userService;
    }

    @PostMapping("/api/examinations/{examId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<QuestionResponse> addQuestion(@PathVariable Long examId,
                                                         @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.addQuestion(examId, request));
    }

    @GetMapping("/api/examinations/{examId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByExamination(@PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExamination(examId));
    }

    @GetMapping("/api/questions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PutMapping("/api/questions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/api/questions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/examinations/{examId}/student-questions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentQuestionResponse>> getStudentQuestions(
            @PathVariable Long examId, Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(questionService.getStudentQuestions(examId, studentId));
    }
}
