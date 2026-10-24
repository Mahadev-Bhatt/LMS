package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.OptionRequest;
import com.lms.lms_backend.dto.request.QuestionRequest;
import com.lms.lms_backend.dto.response.QuestionResponse;
import com.lms.lms_backend.dto.response.StudentQuestionResponse;
import com.lms.lms_backend.enums.QuestionType;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.Examination;
import com.lms.lms_backend.model.Question;
import com.lms.lms_backend.model.QuestionOption;
import com.lms.lms_backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExaminationService examinationService;

    public QuestionService(QuestionRepository questionRepository,
                           ExaminationService examinationService) {
        this.questionRepository = questionRepository;
        this.examinationService = examinationService;
    }

    @Transactional
    public QuestionResponse addQuestion(Long examId, QuestionRequest request) {
        Examination examination = examinationService.findOrThrow(examId);
        validateQuestion(request, examId, null, examination.getTotalMarks());

        Question question = new Question();
        question.setExamination(examination);
        applyRequest(question, request);

        return QuestionResponse.from(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByExamination(Long examId) {
        examinationService.findOrThrow(examId);
        return questionRepository.findByExaminationId(examId)
                .stream().map(QuestionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentQuestionResponse> getStudentQuestions(Long examId, Long studentId) {
        examinationService.assertStudentCanAccess(studentId, examId);
        return questionRepository.findByExaminationId(examId).stream()
                .map(StudentQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        return QuestionResponse.from(findOrThrow(id));
    }

    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question existing = findOrThrow(id);
        validateQuestion(request, existing.getExamination().getId(), id,
                existing.getExamination().getTotalMarks());
        applyRequest(existing, request);
        return QuestionResponse.from(questionRepository.save(existing));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        findOrThrow(id);
        questionRepository.deleteById(id);
    }

    // Package-visible for use in ExaminationAttemptService
    Question findOrThrow(Long id) {
        return questionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    private void validateQuestion(QuestionRequest request, Long examId,
                                   Long excludeQuestionId, int totalMarks) {
        List<OptionRequest> options = request.getOptions();

        // Must have exactly one correct answer
        long correctCount = options.stream().filter(OptionRequest::isCorrect).count();
        if (correctCount != 1) {
            throw new BadRequestException("Exactly one correct option must be specified");
        }

        // MCQ must have at least 2 options
        if (request.getQuestionType() == QuestionType.MCQ && options.size() < 2) {
            throw new BadRequestException("MCQ questions must have at least 2 options");
        }

        // TRUE_FALSE must have exactly 2 options
        if (request.getQuestionType() == QuestionType.TRUE_FALSE && options.size() != 2) {
            throw new BadRequestException("True/False questions must have exactly 2 options");
        }

        // Check marks budget: existing sum minus this question's current marks (on update) + new marks
        int usedMarks = questionRepository.sumMarksByExaminationId(examId);
        if (excludeQuestionId != null) {
            // On update, subtract the current marks of this question before checking
            usedMarks -= findOrThrow(excludeQuestionId).getMarks();
        }
        if (usedMarks + request.getMarks() > totalMarks) {
            throw new BadRequestException(
                    "Adding this question would exceed the examination's total marks of " + totalMarks);
        }
    }

    private void applyRequest(Question question, QuestionRequest request) {
        question.setQuestionText(request.getQuestionText());
        question.setMarks(request.getMarks());
        question.setQuestionType(request.getQuestionType());

        // Clear existing options and rebuild — simplest correct approach for updates
        question.getOptions().clear();
        for (OptionRequest or : request.getOptions()) {
            QuestionOption option = new QuestionOption();
            option.setOptionText(or.getOptionText());
            option.setCorrect(or.isCorrect());
            option.setQuestion(question);
            question.getOptions().add(option);
        }
    }
}
