package com.lms.lms_backend;

import com.lms.lms_backend.repository.UserRepository;
import com.lms.lms_backend.repository.CourseRepository;
import com.lms.lms_backend.repository.ExaminationAttemptRepository;
import com.lms.lms_backend.repository.EnrollmentRepository;
import com.lms.lms_backend.repository.ExaminationRepository;
import com.lms.lms_backend.repository.QuestionRepository;
import com.lms.lms_backend.repository.LearningMaterialRepository;
import com.lms.lms_backend.service.AuthService;
import com.lms.lms_backend.model.Course;
import com.lms.lms_backend.model.Enrollment;
import com.lms.lms_backend.model.Examination;
import com.lms.lms_backend.model.Question;
import com.lms.lms_backend.model.QuestionOption;
import com.lms.lms_backend.model.LearningMaterial;
import com.lms.lms_backend.model.User;
import com.lms.lms_backend.enums.QuestionType;
import com.lms.lms_backend.enums.MaterialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ExaminationAttemptRepository examinationAttemptRepository;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private LearningMaterialRepository learningMaterialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Test
    @Transactional
    void signupAndLoginUseHashedPasswordsAndCreateAuthenticatedSessions() throws Exception {
        String email = "signup-" + UUID.randomUUID() + "@example.com";
        String password = "strong-password-123";
        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn();
        var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("""
                                {"name":"Test Student","email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, user.getPasswordHash())).isTrue();

        MockHttpSession session = (MockHttpSession) signupResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .session(session)
                        .contentType("application/json")
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        MockHttpSession loginSession = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(loginSession).isNotNull();
        mockMvc.perform(get("/api/courses").session(loginSession))
                .andExpect(status().isOk());
    }

    @Test
    void protectedApiRejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void studentCanAttemptEnrolledExamButCannotAccessAnotherStudentsData() throws Exception {
        String firstEmail = "student-" + UUID.randomUUID() + "@example.com";
        String secondEmail = "student-" + UUID.randomUUID() + "@example.com";
        String password = "strong-password-123";
        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn();
        var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        MockHttpSession firstSession = signup(firstEmail, password, csrfCookie, null);
        User firstStudent = userRepository.findByEmailIgnoreCase(firstEmail).orElseThrow();
        User preRegisteredStudent = new User();
        preRegisteredStudent.setName("Existing Student");
        preRegisteredStudent.setEmail(secondEmail);
        preRegisteredStudent.setUserType(com.lms.lms_backend.enums.UserType.STUDENT);
        preRegisteredStudent.setActive(true);
        userRepository.save(preRegisteredStudent);
        mockMvc.perform(post("/api/users/" + preRegisteredStudent.getId() + "/setup-code")
                        .session(firstSession)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
        String setupCode = authService.issuePasswordSetupCode(preRegisteredStudent.getId()).code();
        assertThat(preRegisteredStudent.getPasswordSetupCodeHash()).isNotEqualTo(setupCode);
        MockHttpSession secondSession = signup(secondEmail, password, csrfCookie, setupCode);
        assertThat(passwordEncoder.matches(password,
                userRepository.findByEmailIgnoreCase(secondEmail).orElseThrow().getPasswordHash())).isTrue();
        assertThat(userRepository.findByEmailIgnoreCase(secondEmail).orElseThrow().getPasswordSetupCodeHash())
                .isNull();

        Course course = new Course();
        course.setTitle("Exam course");
        course.setDescription("Course for exam test");
        course.setDurationWeeks(4);
        course.setActive(true);
        course = courseRepository.save(course);

        Examination examination = new Examination();
        examination.setTitle("Practice exam");
        examination.setCourse(course);
        examination.setDurationMinutes(15);
        examination.setTotalMarks(15);
        examination.setPassingMarks(5);
        examination.setActive(true);
        examination = examinationRepository.save(examination);

        Examination scheduledExam = new Examination();
        scheduledExam.setTitle("Scheduled exam");
        scheduledExam.setCourse(course);
        scheduledExam.setDurationMinutes(15);
        scheduledExam.setTotalMarks(10);
        scheduledExam.setPassingMarks(5);
        scheduledExam.setStartTime(LocalDateTime.now().plusHours(2));
        scheduledExam.setActive(true);
        examinationRepository.save(scheduledExam);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(firstStudent);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);

        LearningMaterial material = new LearningMaterial();
        material.setTitle("Course guide");
        material.setDescription("Study guide");
        material.setMaterialType(MaterialType.DOCUMENT);
        material.setResourceUrl("https://example.com/guide");
        material.setCourse(course);
        learningMaterialRepository.save(material);

        Question question = new Question();
        question.setQuestionText("Which option is correct?");
        question.setMarks(10);
        question.setQuestionType(QuestionType.MCQ);
        question.setExamination(examination);
        QuestionOption correctOption = new QuestionOption();
        correctOption.setOptionText("Correct");
        correctOption.setCorrect(true);
        correctOption.setQuestion(question);
        question.getOptions().add(correctOption);
        QuestionOption wrongOption = new QuestionOption();
        wrongOption.setOptionText("Wrong");
        wrongOption.setCorrect(false);
        wrongOption.setQuestion(question);
        question.getOptions().add(wrongOption);
        questionRepository.save(question);

        Question secondQuestion = new Question();
        secondQuestion.setQuestionText("Which option is also correct?");
        secondQuestion.setMarks(5);
        secondQuestion.setQuestionType(QuestionType.MCQ);
        secondQuestion.setExamination(examination);
        QuestionOption secondCorrectOption = new QuestionOption();
        secondCorrectOption.setOptionText("Second correct");
        secondCorrectOption.setCorrect(true);
        secondCorrectOption.setQuestion(secondQuestion);
        secondQuestion.getOptions().add(secondCorrectOption);
        QuestionOption secondWrongOption = new QuestionOption();
        secondWrongOption.setOptionText("Second wrong");
        secondWrongOption.setCorrect(false);
        secondWrongOption.setQuestion(secondQuestion);
        secondQuestion.getOptions().add(secondWrongOption);
        questionRepository.save(secondQuestion);

        mockMvc.perform(get("/api/users").session(firstSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/materials").session(firstSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/materials/mine").session(firstSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Course guide"));
        mockMvc.perform(get("/api/materials/mine").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/attempts").session(firstSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/examinations/available").session(firstSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/examinations/" + scheduledExam.getId() + "/student-questions")
                        .session(firstSession))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/examinations/" + examination.getId() + "/student-questions")
                        .session(firstSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].options[0].correct").doesNotExist());

        mockMvc.perform(post("/api/attempts")
                        .session(firstSession)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("""
                                {"studentId":999999,"examinationId":%d}
                                """.formatted(examination.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(firstStudent.getId()));
        long attemptId = examinationAttemptRepository
                .findByStudentIdAndExaminationId(firstStudent.getId(), examination.getId())
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/attempts/" + attemptId).session(secondSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/examinations/" + examination.getId() + "/student-questions")
                        .session(secondSession))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/attempts/mine").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(post("/api/attempts/" + attemptId + "/submit")
                        .session(secondSession)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("""
                                {"answers":[{"questionId":%d,"selectedOptionId":%d}]}
                                """.formatted(question.getId(), correctOption.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/attempts/" + attemptId + "/submit")
                        .session(firstSession)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("""
                                {"answers":[
                                  {"questionId":%d,"selectedOptionId":%d},
                                  {"questionId":%d,"selectedOptionId":%d}
                                ]}
                                """.formatted(question.getId(), correctOption.getId(),
                                secondQuestion.getId(), secondWrongOption.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obtainedMarks").value(10))
                .andExpect(jsonPath("$.result").value("PASS"))
                .andExpect(jsonPath("$.submittedAnswers.length()").value(2))
                .andExpect(jsonPath("$.submittedAnswers[0].correct").value(true))
                .andExpect(jsonPath("$.submittedAnswers[0].correctOptionText").value("Correct"))
                .andExpect(jsonPath("$.submittedAnswers[1].correct").value(false))
                .andExpect(jsonPath("$.submittedAnswers[1].selectedOptionText").value("Second wrong"))
                .andExpect(jsonPath("$.submittedAnswers[1].correctOptionText").value("Second correct"));
    }

    private MockHttpSession signup(String email, String password, jakarta.servlet.http.Cookie csrfCookie,
                                   String setupCode)
            throws Exception {
        String optionalSetupCode = setupCode == null ? "" : ",\"setupCode\":\"" + setupCode + "\"";
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType("application/json")
                        .content("""
                                {"name":"Test Student","email":"%s","password":"%s"%s}
                                """.formatted(email, password, optionalSetupCode)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
