package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExaminationRepository extends JpaRepository<Examination, Long> {

    @Query("SELECT e FROM Examination e JOIN FETCH e.course")
    List<Examination> findAllWithCourse();

    @Query("SELECT e FROM Examination e JOIN FETCH e.course WHERE e.id = :id")
    Optional<Examination> findByIdWithCourse(@Param("id") Long id);

    @Query("SELECT e FROM Examination e JOIN FETCH e.course WHERE e.course.id = :courseId")
    List<Examination> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Examination e JOIN FETCH e.course c WHERE e.active = true AND c.active = true " +
            "AND EXISTS (SELECT enrollment.id FROM Enrollment enrollment " +
            "WHERE enrollment.course.id = c.id AND enrollment.student.id = :studentId " +
            "AND enrollment.status = com.lms.lms_backend.enums.EnrollmentStatus.ENROLLED)")
    List<Examination> findAvailableForStudent(@Param("studentId") Long studentId);
}
