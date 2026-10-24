package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.ExaminationRequest;
import com.lms.lms_backend.dto.response.ExaminationResponse;
import com.lms.lms_backend.enums.EnrollmentStatus;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.Course;
import com.lms.lms_backend.model.Examination;
import com.lms.lms_backend.repository.ExaminationRepository;
import com.lms.lms_backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;

    public ExaminationService(ExaminationRepository examinationRepository,
                              CourseService courseService,
                              EnrollmentRepository enrollmentRepository) {
        this.examinationRepository = examinationRepository;
        this.courseService = courseService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public ExaminationResponse createExamination(ExaminationRequest request) {
        validate(request);
        Course course = courseService.getCourseById(request.getCourseId());
        return ExaminationResponse.from(examinationRepository.save(toEntity(new Examination(), request, course)));
    }

    @Transactional(readOnly = true)
    public List<ExaminationResponse> getAllExaminations() {
        return examinationRepository.findAllWithCourse().stream().map(ExaminationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ExaminationResponse getExaminationById(Long id) {
        return ExaminationResponse.from(
            examinationRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id))
        );
    }

    @Transactional(readOnly = true)
    public List<ExaminationResponse> getExaminationsByCourse(Long courseId) {
        courseService.getCourseById(courseId);
        return examinationRepository.findByCourseId(courseId).stream().map(ExaminationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ExaminationResponse> getAvailableExaminations(Long studentId) {
        LocalDateTime now = LocalDateTime.now();
        return examinationRepository.findAvailableForStudent(studentId).stream()
                .filter(exam -> exam.getEndTime() == null || !now.isAfter(exam.getEndTime()))
                .map(ExaminationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Examination assertStudentCanAccess(Long studentId, Long examinationId) {
        Examination examination = findOrThrow(examinationId);
        if (!examination.isActive() || !examination.getCourse().isActive()
                || !enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                        studentId, examination.getCourse().getId(), EnrollmentStatus.ENROLLED)
                || !isWithinSchedule(examination, LocalDateTime.now())) {
            throw new BadRequestException("This examination is not currently available to you");
        }
        return examination;
    }

    @Transactional
    public ExaminationResponse updateExamination(Long id, ExaminationRequest request) {
        validate(request);
        Examination existing = findOrThrow(id);
        Course course = courseService.getCourseById(request.getCourseId());
        return ExaminationResponse.from(examinationRepository.save(toEntity(existing, request, course)));
    }

    @Transactional
    public void deleteExamination(Long id) {
        findOrThrow(id);
        examinationRepository.deleteById(id);
    }

    // Package-visible so ExaminationAttemptService can load the entity directly
    Examination findOrThrow(Long id) {
        return examinationRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
    }

    private void validate(ExaminationRequest r) {
        if (r.getPassingMarks() > r.getTotalMarks()) {
            throw new BadRequestException("Passing marks cannot exceed total marks");
        }
        if (r.getStartTime() != null && r.getEndTime() != null
                && !r.getEndTime().isAfter(r.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private boolean isWithinSchedule(Examination examination, LocalDateTime now) {
        return (examination.getStartTime() == null || !now.isBefore(examination.getStartTime()))
                && (examination.getEndTime() == null || !now.isAfter(examination.getEndTime()));
    }

    private Examination toEntity(Examination e, ExaminationRequest r, Course course) {
        e.setTitle(r.getTitle());
        e.setDescription(r.getDescription());
        e.setCourse(course);
        e.setDurationMinutes(r.getDurationMinutes());
        e.setTotalMarks(r.getTotalMarks());
        e.setPassingMarks(r.getPassingMarks());
        e.setStartTime(r.getStartTime());
        e.setEndTime(r.getEndTime());
        e.setActive(r.isActive());
        return e;
    }
}
