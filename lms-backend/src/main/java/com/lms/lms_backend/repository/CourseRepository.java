package com.lms.lms_backend.repository;

import com.lms.lms_backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Spring Data JPA generates the SQL automatically from the method name:
    // SELECT * FROM courses WHERE LOWER(title) LIKE LOWER('%keyword%')
    List<Course> findByTitleContainingIgnoreCase(String title);
}
