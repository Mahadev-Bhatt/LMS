package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.ExaminationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExaminationAttemptRepository extends JpaRepository<ExaminationAttempt, Long> {

    boolean existsByStudentIdAndExaminationId(Long studentId, Long examinationId);

    Optional<ExaminationAttempt> findByStudentIdAndExaminationId(Long studentId, Long examinationId);

    @Query("SELECT a FROM ExaminationAttempt a JOIN FETCH a.student JOIN FETCH a.examination LEFT JOIN FETCH a.submittedAnswers WHERE a.id = :id")
    Optional<ExaminationAttempt> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM ExaminationAttempt a JOIN FETCH a.student JOIN FETCH a.examination " +
            "LEFT JOIN FETCH a.submittedAnswers WHERE a.student.id = :studentId")
    List<ExaminationAttempt> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT DISTINCT a FROM ExaminationAttempt a JOIN FETCH a.student JOIN FETCH a.examination " +
            "LEFT JOIN FETCH a.submittedAnswers WHERE a.examination.id = :examinationId")
    List<ExaminationAttempt> findByExaminationId(@Param("examinationId") Long examinationId);

    @Query("SELECT DISTINCT a FROM ExaminationAttempt a JOIN FETCH a.student JOIN FETCH a.examination " +
            "LEFT JOIN FETCH a.submittedAnswers")
    List<ExaminationAttempt> findAllWithDetails();
}
