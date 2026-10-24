package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {

    @Query("SELECT m FROM LearningMaterial m JOIN FETCH m.course")
    List<LearningMaterial> findAllWithCourse();

    @Query("SELECT m FROM LearningMaterial m JOIN FETCH m.course WHERE m.id = :id")
    Optional<LearningMaterial> findByIdWithCourse(@Param("id") Long id);

    @Query("SELECT m FROM LearningMaterial m JOIN FETCH m.course WHERE m.course.id = :courseId")
    List<LearningMaterial> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT m FROM LearningMaterial m JOIN FETCH m.course c WHERE c.active = true " +
            "AND EXISTS (SELECT enrollment.id FROM Enrollment enrollment " +
            "WHERE enrollment.course.id = c.id AND enrollment.student.id = :studentId " +
            "AND enrollment.status = com.lms.lms_backend.enums.EnrollmentStatus.ENROLLED)")
    List<LearningMaterial> findAvailableForStudent(@Param("studentId") Long studentId);
}
