package com.lms.lms_backend.controller;

import com.lms.lms_backend.dto.request.ExaminationRequest;
import com.lms.lms_backend.dto.response.ExaminationResponse;
import com.lms.lms_backend.service.ExaminationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examinations")
public class ExaminationController {

    private final ExaminationService examinationService;

    public ExaminationController(ExaminationService examinationService) {
        this.examinationService = examinationService;
    }

    @PostMapping
    public ResponseEntity<ExaminationResponse> createExamination(@Valid @RequestBody ExaminationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examinationService.createExamination(request));
    }

    @GetMapping
    public ResponseEntity<List<ExaminationResponse>> getAllExaminations() {
        return ResponseEntity.ok(examinationService.getAllExaminations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExaminationResponse> getExaminationById(@PathVariable Long id) {
        return ResponseEntity.ok(examinationService.getExaminationById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ExaminationResponse>> getExaminationsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(examinationService.getExaminationsByCourse(courseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExaminationResponse> updateExamination(@PathVariable Long id,
                                                                  @Valid @RequestBody ExaminationRequest request) {
        return ResponseEntity.ok(examinationService.updateExamination(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamination(@PathVariable Long id) {
        examinationService.deleteExamination(id);
        return ResponseEntity.noContent().build();
    }
}
