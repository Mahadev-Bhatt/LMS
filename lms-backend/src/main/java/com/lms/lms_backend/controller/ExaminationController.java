package com.lms.lms_backend.controller;

import com.lms.lms_backend.dto.request.ExaminationRequest;
import com.lms.lms_backend.dto.response.ExaminationResponse;
import com.lms.lms_backend.service.ExaminationService;
import com.lms.lms_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examinations")
public class ExaminationController {

    private final ExaminationService examinationService;
    private final UserService userService;

    public ExaminationController(ExaminationService examinationService, UserService userService) {
        this.examinationService = examinationService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ExaminationResponse> createExamination(@Valid @RequestBody ExaminationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examinationService.createExamination(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<ExaminationResponse>> getAllExaminations() {
        return ResponseEntity.ok(examinationService.getAllExaminations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ExaminationResponse> getExaminationById(@PathVariable Long id) {
        return ResponseEntity.ok(examinationService.getExaminationById(id));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<ExaminationResponse>> getExaminationsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(examinationService.getExaminationsByCourse(courseId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ExaminationResponse> updateExamination(@PathVariable Long id,
                                                                  @Valid @RequestBody ExaminationRequest request) {
        return ResponseEntity.ok(examinationService.updateExamination(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> deleteExamination(@PathVariable Long id) {
        examinationService.deleteExamination(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExaminationResponse>> getAvailableExaminations(Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(examinationService.getAvailableExaminations(studentId));
    }
}
