package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.SubmittedAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmittedAnswerRepository extends JpaRepository<SubmittedAnswer, Long> {
}
