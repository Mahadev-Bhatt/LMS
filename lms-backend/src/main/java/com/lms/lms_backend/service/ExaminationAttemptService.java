package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.AnswerRequest;
import com.lms.lms_backend.dto.request.StartAttemptRequest;
import com.lms.lms_backend.dto.request.SubmitAttemptRequest;
import com.lms.lms_backend.dto.response.AttemptResponse;
import com.lms.lms_backend.enums.AttemptResult;
import com.lms.lms_backend.enums.AttemptStatus;
import com.lms.lms_backend.enums.UserType;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.DuplicateResourceException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.*;
import com.lms.lms_backend.repository.ExaminationAttemptRepository;
import com.lms.lms_backend.repository.QuestionOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class ExaminationAttemptService {

    private final ExaminationAttemptRepository attemptRepository;
    private final UserService userService;
    private final ExaminationService examinationService;
    private final QuestionService questionService;
    private final QuestionOptionRepository optionRepository;

    public ExaminationAttemptService(ExaminationAttemptRepository attemptRepository,
                                     UserService userService,
                                     ExaminationService examinationService,
                                     QuestionService questionService,
                                     QuestionOptionRepository optionRepository) {
        this.attemptRepository = attemptRepository;
        this.userService = userService;
        this.examinationService = examinationService;
        this.questionService = questionService;
        this.optionRepository = optionRepository;
    }

    @Transactional
    public AttemptResponse startAttempt(Long studentId, StartAttemptRequest request) {
        User student = userService.getUserById(studentId);

        if (student.getUserType() != UserType.STUDENT) {
            throw new BadRequestException("Only students can attempt examinations");
        }

        Examination examination = examinationService.assertStudentCanAccess(student.getId(), request.getExaminationId());
        if (questionService.getQuestionsByExamination(examination.getId()).isEmpty()) {
            throw new BadRequestException("This examination has no questions yet");
        }

        if (attemptRepository.existsByStudentIdAndExaminationId(student.getId(), examination.getId())) {
            throw new DuplicateResourceException("Student has already attempted this examination");
        }

        ExaminationAttempt attempt = new ExaminationAttempt();
        attempt.setStudent(student);
        attempt.setExamination(examination);

        return AttemptResponse.from(attemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public List<AttemptResponse> getAllAttempts() {
        return attemptRepository.findAllWithDetails().stream().map(AttemptResponse::from).toList();
    }

    @Transactional
    public AttemptResponse submitAttempt(Long attemptId, Long studentId, SubmitAttemptRequest request) {
        ExaminationAttempt attempt = findOrThrow(attemptId);

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("You cannot access another student's attempt");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new BadRequestException("This attempt has already been submitted");
        }

        Examination examination = attempt.getExamination();
        // Allow a brief network delay after the client timer before finalizing an unanswered timeout.
        if (LocalDateTime.now().isAfter(
                attempt.getStartedAt().plusMinutes(examination.getDurationMinutes()).plusSeconds(10))) {
            List<Question> examQuestions = questionService
                    .getQuestionsByExamination(examination.getId())
                    .stream()
                    .map(questionResponse -> questionService.findOrThrow(questionResponse.getId()))
                    .toList();
            for (Question question : examQuestions) {
                SubmittedAnswer unanswered = new SubmittedAnswer();
                unanswered.setExaminationAttempt(attempt);
                unanswered.setQuestion(question);
                attempt.getSubmittedAnswers().add(unanswered);
            }
            attempt.setObtainedMarks(0);
            attempt.setSubmittedAt(LocalDateTime.now());
            attempt.setStatus(AttemptStatus.SUBMITTED);
            attempt.setResult(AttemptResult.FAIL);
            return AttemptResponse.from(attemptRepository.save(attempt));
        }

        examinationService.assertStudentCanAccess(studentId, examination.getId());
        List<Question> examQuestions = questionService
                .getQuestionsByExamination(examination.getId())
                .stream()
                .map(qr -> questionService.findOrThrow(qr.getId()))
                .toList();

        Set<Long> answeredQuestionIds = new HashSet<>();
        for (AnswerRequest ar : request.getAnswers()) {
            if (!answeredQuestionIds.add(ar.getQuestionId())) {
                throw new BadRequestException("A question can only be answered once");
            }
            Question question = questionService.findOrThrow(ar.getQuestionId());

            // Ensure the question belongs to this examination
            if (!question.getExamination().getId().equals(examination.getId())) {
                throw new BadRequestException(
                        "Question " + ar.getQuestionId() + " does not belong to this examination");
            }

            QuestionOption selectedOption = null;
            if (ar.getSelectedOptionId() != null) {
                selectedOption = optionRepository.findById(ar.getSelectedOptionId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Option not found with id: " + ar.getSelectedOptionId()));

                // Ensure the option belongs to the question
                if (!selectedOption.getQuestion().getId().equals(question.getId())) {
                    throw new BadRequestException(
                            "Option " + ar.getSelectedOptionId() + " does not belong to question " + ar.getQuestionId());
                }
            }

            SubmittedAnswer submittedAnswer = new SubmittedAnswer();
            submittedAnswer.setExaminationAttempt(attempt);
            submittedAnswer.setQuestion(question);
            submittedAnswer.setSelectedOption(selectedOption);
            attempt.getSubmittedAnswers().add(submittedAnswer);
        }

        if (answeredQuestionIds.size() != examQuestions.size()) {
            throw new BadRequestException("Submit one answer entry for every examination question");
        }

        // Auto-evaluate: sum marks for each correctly answered question
        int obtained = 0;
        for (SubmittedAnswer sa : attempt.getSubmittedAnswers()) {
            if (sa.getSelectedOption() != null && sa.getSelectedOption().isCorrect()) {
                obtained += sa.getQuestion().getMarks();
            }
        }

        attempt.setObtainedMarks(obtained);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setResult(obtained >= examination.getPassingMarks() ? AttemptResult.PASS : AttemptResult.FAIL);

        return AttemptResponse.from(attemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public AttemptResponse getAttemptById(Long id) {
        return AttemptResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AttemptResponse getAttemptByIdForStudent(Long id, Long studentId) {
        ExaminationAttempt attempt = findOrThrow(id);
        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("You cannot access another student's attempt");
        }
        return AttemptResponse.from(attempt);
    }

    @Transactional(readOnly = true)
    public List<AttemptResponse> getAttemptsByStudent(Long studentId) {
        userService.getUserById(studentId);
        return attemptRepository.findByStudentId(studentId)
                .stream().map(AttemptResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AttemptResponse> getAttemptsByExamination(Long examinationId) {
        examinationService.findOrThrow(examinationId);
        return attemptRepository.findByExaminationId(examinationId)
                .stream().map(AttemptResponse::from).toList();
    }

    private ExaminationAttempt findOrThrow(Long id) {
        return attemptRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + id));
    }
}
