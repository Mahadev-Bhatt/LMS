package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q JOIN FETCH q.examination LEFT JOIN FETCH q.options WHERE q.examination.id = :examinationId")
    List<Question> findByExaminationId(@Param("examinationId") Long examinationId);

    @Query("SELECT q FROM Question q JOIN FETCH q.examination LEFT JOIN FETCH q.options WHERE q.id = :id")
    Optional<Question> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(q.marks), 0) FROM Question q WHERE q.examination.id = :examId")
    Integer sumMarksByExaminationId(@Param("examId") Long examId);
}
