package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.LearningMaterialProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningMaterialProgressRepository extends JpaRepository<LearningMaterialProgress, Long> {

    List<LearningMaterialProgress> findByStudentId(Long studentId);

    Optional<LearningMaterialProgress> findByStudentIdAndMaterialId(Long studentId, Long materialId);

    void deleteByStudentId(Long studentId);

    void deleteByMaterialId(Long materialId);
}
