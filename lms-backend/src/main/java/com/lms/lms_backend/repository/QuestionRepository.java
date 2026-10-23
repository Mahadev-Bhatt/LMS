package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExaminationId(Long examinationId);

    // Sum of marks for all questions belonging to an examination.
    // Used to validate that adding a new question won't exceed totalMarks.
    @Query("SELECT COALESCE(SUM(q.marks), 0) FROM Question q WHERE q.examination.id = :examId")
    Integer sumMarksByExaminationId(Long examId);
}
