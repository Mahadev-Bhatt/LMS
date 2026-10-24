package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.EnrollmentRequest;
import com.lms.lms_backend.dto.request.EnrollmentStatusRequest;
import com.lms.lms_backend.dto.response.EnrollmentResponse;
import com.lms.lms_backend.enums.UserType;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.DuplicateResourceException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.Course;
import com.lms.lms_backend.model.Enrollment;
import com.lms.lms_backend.model.User;
import com.lms.lms_backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final CourseService courseService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             UserService userService,
                             CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        User student = userService.getUserById(request.getStudentId());

        // Only users of type STUDENT can be enrolled
        if (student.getUserType() != UserType.STUDENT) {
            throw new BadRequestException("Only students can be enrolled in courses");
        }

        Course course = courseService.getCourseById(request.getCourseId());

        // Cannot enroll in an inactive course
        if (!course.isActive()) {
            throw new BadRequestException("Cannot enroll in an inactive course");
        }

        // Prevent duplicate enrollment
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAllWithDetails()
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        return EnrollmentResponse.from(
            enrollmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id))
        );
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        userService.getUserById(studentId);
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        courseService.getCourseById(courseId);
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional
    public EnrollmentResponse updateStatus(Long id, EnrollmentStatusRequest request) {
        Enrollment enrollment = findOrThrow(id);
        enrollment.setStatus(request.getStatus());
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        findOrThrow(id);
        enrollmentRepository.deleteById(id);
    }

    private Enrollment findOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }
}
