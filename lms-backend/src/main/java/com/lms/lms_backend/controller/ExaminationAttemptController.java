package com.lms.lms_backend.controller;

import com.lms.lms_backend.dto.request.StartAttemptRequest;
import com.lms.lms_backend.dto.request.SubmitAttemptRequest;
import com.lms.lms_backend.dto.response.AttemptResponse;
import com.lms.lms_backend.service.ExaminationAttemptService;
import com.lms.lms_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class ExaminationAttemptController {

    private final ExaminationAttemptService attemptService;
    private final UserService userService;

    public ExaminationAttemptController(ExaminationAttemptService attemptService, UserService userService) {
        this.attemptService = attemptService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<AttemptResponse>> getAllAttempts() {
        return ResponseEntity.ok(attemptService.getAllAttempts());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AttemptResponse>> getMyAttempts(Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(attemptService.getAttemptsByStudent(studentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponse> startAttempt(
            @Valid @RequestBody StartAttemptRequest request, Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(attemptService.startAttempt(studentId, request));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponse> submitAttempt(@PathVariable Long id,
                                                          @Valid @RequestBody SubmitAttemptRequest request,
                                                          Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(attemptService.submitAttempt(id, studentId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','STUDENT')")
    public ResponseEntity<AttemptResponse> getAttemptById(@PathVariable Long id, Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
            return ResponseEntity.ok(attemptService.getAttemptByIdForStudent(id, studentId));
        }
        return ResponseEntity.ok(attemptService.getAttemptById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attemptService.getAttemptsByStudent(studentId));
    }

    @GetMapping("/examination/{examinationId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByExamination(@PathVariable Long examinationId) {
        return ResponseEntity.ok(attemptService.getAttemptsByExamination(examinationId));
    }
}
