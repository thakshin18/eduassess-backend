package com.eduassess.controller;

import com.eduassess.dto.CreateQuestionRequest;
import com.eduassess.dto.OptionAdminDto;
import com.eduassess.entity.Role;
import com.eduassess.entity.Test;
import com.eduassess.entity.TestStatus;
import com.eduassess.entity.User;
import com.eduassess.repository.QuestionRepository;
import com.eduassess.repository.TestRepository;
import com.eduassess.repository.UserRepository;
import com.eduassess.security.JwtService;
import com.eduassess.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String studentToken;
    private Test testEntity;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        testRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder().name("Admin").email("admin@test.com").password("pass").role(Role.ADMIN).build();
        User student = User.builder().name("Student").email("student@test.com").password("pass").role(Role.STUDENT).build();
        userRepository.save(admin);
        userRepository.save(student);

        adminToken = "Bearer " + jwtService.generateToken(admin);
        studentToken = "Bearer " + jwtService.generateToken(student);

        testEntity = Test.builder().title("Sample").subject("Math").duration(60).totalMarks(10).passingMarks(5).status(TestStatus.PUBLISHED).build();
        testEntity = testRepository.save(testEntity);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Admin can create question with exactly one correct option")
    void adminCanCreateQuestion() throws Exception {
        CreateQuestionRequest req = CreateQuestionRequest.builder()
                .questionText("1+1?")
                .marks(1)
                .questionOrder(1)
                .options(List.of(
                        OptionAdminDto.builder().optionText("2").optionOrder(1).isCorrect(true).build(),
                        OptionAdminDto.builder().optionText("3").optionOrder(2).isCorrect(false).build()
                )).build();

        mockMvc.perform(post("/api/tests/" + testEntity.getId() + "/questions")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionText").value("1+1?"))
                .andExpect(jsonPath("$.options[0].isCorrect").value(true)); // Admin DTO returns isCorrect
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Student cannot call create question API")
    void studentCannotCreateQuestion() throws Exception {
        mockMvc.perform(post("/api/tests/" + testEntity.getId() + "/questions")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Student fetching questions MUST NOT leak correct answers")
    void studentResponseDoesNotLeakAnswers() throws Exception {
        // Pre-create question using service
        CreateQuestionRequest req = CreateQuestionRequest.builder()
                .questionText("1+1?")
                .marks(1)
                .questionOrder(1)
                .options(List.of(
                        OptionAdminDto.builder().optionText("2").optionOrder(1).isCorrect(true).build(),
                        OptionAdminDto.builder().optionText("3").optionOrder(2).isCorrect(false).build()
                )).build();
        questionService.createQuestion(testEntity.getId(), req);

        MvcResult result = mockMvc.perform(get("/api/tests/" + testEntity.getId() + "/questions")
                .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        // Critical assertions to ensure answer data is stripped
        assertFalse(responseBody.contains("isCorrect"), "Response leaked 'isCorrect'");
        assertFalse(responseBody.contains("correctAnswer"), "Response leaked 'correctAnswer'");
        assertFalse(responseBody.contains("correctOptionId"), "Response leaked 'correctOptionId'");
        assertFalse(responseBody.contains("answerKey"), "Response leaked 'answerKey'");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Student cannot fetch questions if test is DRAFT")
    void studentCannotFetchDraftTestQuestions() throws Exception {
        Test draftTest = Test.builder().title("Draft").subject("Sub").duration(10).totalMarks(10).passingMarks(5).status(TestStatus.DRAFT).build();
        draftTest = testRepository.save(draftTest);

        mockMvc.perform(get("/api/tests/" + draftTest.getId() + "/questions")
                .header("Authorization", studentToken))
                .andExpect(status().isForbidden()); // AccessDeniedException mapped to 403
    }
}
