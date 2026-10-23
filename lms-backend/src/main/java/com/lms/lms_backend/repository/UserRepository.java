package com.lms.lms_backend.repository;

import com.lms.lms_backend.enums.UserType;
import com.lms.lms_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used to check for duplicate emails before saving
    boolean existsByEmail(String email);

    // Used during update to detect if another user already owns the new email
    Optional<User> findByEmail(String email);

    // Filters users by STUDENT or INSTRUCTOR
    List<User> findByUserType(UserType userType);
}
