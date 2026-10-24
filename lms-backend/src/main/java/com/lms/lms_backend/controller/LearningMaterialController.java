package com.lms.lms_backend.controller;

import com.lms.lms_backend.dto.request.MaterialRequest;
import com.lms.lms_backend.dto.response.MaterialResponse;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.service.LearningMaterialService;
import com.lms.lms_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class LearningMaterialController {

    private final LearningMaterialService materialService;
    private final UserService userService;

    public LearningMaterialController(LearningMaterialService materialService, UserService userService) {
        this.materialService = materialService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<MaterialResponse> createMaterial(@Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.createMaterial(request));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<MaterialResponse> uploadPdf(@RequestParam String title,
                                                      @RequestParam(required = false) String description,
                                                      @RequestParam Long courseId,
                                                      @RequestParam(required = false) Long materialId,
                                                      @RequestPart("file") MultipartFile file) {
        if (title.isBlank()) {
            throw new BadRequestException("Title is required");
        }
        if (materialId != null) {
            return ResponseEntity.ok(
                    materialService.replacePdfMaterial(materialId, title, description, courseId, file));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialService.createPdfMaterial(title, description, courseId, file));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<MaterialResponse>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<List<MaterialResponse>> getMaterialsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(materialService.getMaterialsByCourse(courseId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<MaterialResponse>> getMyMaterials(Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        return ResponseEntity.ok(materialService.getMaterialsForStudent(studentId));
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','STUDENT')")
    public ResponseEntity<Resource> getPdfFile(@PathVariable Long id, Authentication authentication) {
        boolean student = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));
        var pdfPath = student
                ? materialService.getPdfForStudent(userService.getActiveByEmail(authentication.getName()).getId(), id)
                : materialService.getUploadedPdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename("course-material-" + id + ".pdf", StandardCharsets.UTF_8)
                                .build().toString())
                .body(new FileSystemResource(pdfPath));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> markMaterialCompleted(@PathVariable Long id, Authentication authentication) {
        Long studentId = userService.getActiveByEmail(authentication.getName()).getId();
        materialService.markCompleted(studentId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<MaterialResponse> updateMaterial(@PathVariable Long id,
                                                           @Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.ok(materialService.updateMaterial(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
