package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.ExaminationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExaminationAttemptRepository extends JpaRepository<ExaminationAttempt, Long> {

    boolean existsByStudentIdAndExaminationId(Long studentId, Long examinationId);

    List<ExaminationAttempt> findByStudentId(Long studentId);

    List<ExaminationAttempt> findByExaminationId(Long examinationId);
}
