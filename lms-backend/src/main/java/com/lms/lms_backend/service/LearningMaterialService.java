package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.MaterialRequest;
import com.lms.lms_backend.dto.response.MaterialResponse;
import com.lms.lms_backend.enums.EnrollmentStatus;
import com.lms.lms_backend.enums.MaterialType;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.Course;
import com.lms.lms_backend.model.LearningMaterial;
import com.lms.lms_backend.model.LearningMaterialProgress;
import com.lms.lms_backend.model.User;
import com.lms.lms_backend.repository.EnrollmentRepository;
import com.lms.lms_backend.repository.LearningMaterialProgressRepository;
import com.lms.lms_backend.repository.LearningMaterialRepository;
import com.lms.lms_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearningMaterialService {

    private final LearningMaterialRepository materialRepository;
    private final LearningMaterialProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final Path uploadDirectory;

    public LearningMaterialService(LearningMaterialRepository materialRepository,
                                   LearningMaterialProgressRepository progressRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   UserRepository userRepository,
                                   CourseService courseService,
                                   @Value("${lms.materials.upload-directory:uploads/materials}") String uploadDirectory) {
        this.materialRepository = materialRepository;
        this.progressRepository = progressRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        Course course = courseService.getCourseById(request.getCourseId());
        LearningMaterial material = new LearningMaterial();
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setMaterialType(request.getMaterialType());
        material.setResourceUrl(request.getResourceUrl());
        material.setCourse(course);
        return MaterialResponse.from(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponse createPdfMaterial(String title, String description, Long courseId, MultipartFile file) {
        return savePdfMaterial(null, title, description, courseId, file);
    }

    @Transactional
    public MaterialResponse replacePdfMaterial(Long materialId, String title, String description,
                                               Long courseId, MultipartFile file) {
        return savePdfMaterial(materialId, title, description, courseId, file);
    }

    private MaterialResponse savePdfMaterial(Long materialId, String title, String description,
                                              Long courseId, MultipartFile file) {
        validatePdf(file);
        Course course = courseService.getCourseById(courseId);
        LearningMaterial material = materialId == null ? new LearningMaterial() : findOrThrow(materialId);
        material.setTitle(title);
        material.setDescription(description);
        material.setMaterialType(MaterialType.DOCUMENT);
        material.setCourse(course);
        if (materialId == null) {
            material.setResourceUrl("upload://pending");
            material = materialRepository.save(material);
        }

        Path pdfPath = uploadDirectory.resolve(material.getId() + ".pdf");
        try {
            Files.createDirectories(uploadDirectory);
            file.transferTo(pdfPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store uploaded PDF", exception);
        }

        material.setResourceUrl("/api/materials/" + material.getId() + "/file");
        return MaterialResponse.from(materialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public Path getPdfForStudent(Long studentId, Long materialId) {
        LearningMaterial material = findOrThrow(materialId);
        if (!material.getCourse().isActive()
                || !enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                        studentId, material.getCourse().getId(), EnrollmentStatus.ENROLLED)) {
            throw new AccessDeniedException("You must be actively enrolled in this course to access its materials");
        }
        return getUploadedPdfPath(material);
    }

    @Transactional(readOnly = true)
    public Path getUploadedPdf(Long materialId) {
        return getUploadedPdfPath(findOrThrow(materialId));
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getAllMaterials() {
        return materialRepository.findAllWithCourse().stream().map(MaterialResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsForStudent(Long studentId) {
        Set<Long> completedMaterialIds = progressRepository.findByStudentId(studentId).stream()
                .map(progress -> progress.getMaterial().getId())
                .collect(Collectors.toSet());
        return materialRepository.findAvailableForStudent(studentId).stream()
                .map(material -> MaterialResponse.from(material, completedMaterialIds.contains(material.getId())))
                .toList();
    }

    @Transactional
    public void markCompleted(Long studentId, Long materialId) {
        LearningMaterial material = findOrThrow(materialId);
        if (!material.getCourse().isActive()
                || !enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                        studentId, material.getCourse().getId(), EnrollmentStatus.ENROLLED)) {
            throw new AccessDeniedException(
                    "You must be actively enrolled in this course to complete its materials");
        }

        if (progressRepository.findByStudentIdAndMaterialId(studentId, materialId).isPresent()) {
            return;
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        LearningMaterialProgress progress = new LearningMaterialProgress();
        progress.setStudent(student);
        progress.setMaterial(material);
        progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(Long id) {
        return MaterialResponse.from(
            materialRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning material not found with id: " + id))
        );
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsByCourse(Long courseId) {
        courseService.getCourseById(courseId);
        return materialRepository.findByCourseId(courseId).stream().map(MaterialResponse::from).toList();
    }

    @Transactional
    public MaterialResponse updateMaterial(Long id, MaterialRequest request) {
        LearningMaterial material = findOrThrow(id);
        Course course = courseService.getCourseById(request.getCourseId());
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setMaterialType(request.getMaterialType());
        material.setResourceUrl(request.getResourceUrl());
        material.setCourse(course);
        return MaterialResponse.from(materialRepository.save(material));
    }

    @Transactional
    public void deleteMaterial(Long id) {
        LearningMaterial material = findOrThrow(id);
        progressRepository.deleteByMaterialId(id);
        materialRepository.deleteById(id);
        if (isUploadedPdf(material)) {
            try {
                Files.deleteIfExists(uploadDirectory.resolve(id + ".pdf"));
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to remove uploaded PDF", exception);
            }
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Select a PDF file to upload");
        }
        if (file.getSize() > 20L * 1024 * 1024) {
            throw new BadRequestException("PDF file must be 20 MB or smaller");
        }

        try (var input = file.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (!java.util.Arrays.equals(signature, "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
                throw new BadRequestException("The selected file is not a valid PDF");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read uploaded PDF", exception);
        }
    }

    private Path getUploadedPdfPath(LearningMaterial material) {
        if (!isUploadedPdf(material)) {
            throw new ResourceNotFoundException("Uploaded PDF not found for learning material: " + material.getId());
        }
        Path path = uploadDirectory.resolve(material.getId() + ".pdf");
        if (!Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Uploaded PDF file is missing");
        }
        return path;
    }

    private boolean isUploadedPdf(LearningMaterial material) {
        return ("/api/materials/" + material.getId() + "/file").equals(material.getResourceUrl());
    }

    private LearningMaterial findOrThrow(Long id) {
        return materialRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning material not found with id: " + id));
    }
}
