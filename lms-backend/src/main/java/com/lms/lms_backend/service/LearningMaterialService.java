package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.MaterialRequest;
import com.lms.lms_backend.dto.response.MaterialResponse;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.Course;
import com.lms.lms_backend.model.LearningMaterial;
import com.lms.lms_backend.repository.LearningMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LearningMaterialService {

    private final LearningMaterialRepository materialRepository;
    private final CourseService courseService;

    public LearningMaterialService(LearningMaterialRepository materialRepository,
                                   CourseService courseService) {
        this.materialRepository = materialRepository;
        this.courseService = courseService;
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

    @Transactional(readOnly = true)
    public List<MaterialResponse> getAllMaterials() {
        return materialRepository.findAllWithCourse().stream().map(MaterialResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsForStudent(Long studentId) {
        return materialRepository.findAvailableForStudent(studentId).stream()
                .map(MaterialResponse::from)
                .toList();
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
        findOrThrow(id);
        materialRepository.deleteById(id);
    }

    private LearningMaterial findOrThrow(Long id) {
        return materialRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning material not found with id: " + id));
    }
}
