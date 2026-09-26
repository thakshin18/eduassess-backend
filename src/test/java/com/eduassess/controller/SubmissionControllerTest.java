package com.eduassess.controller;

import com.eduassess.dto.AnswerSubmissionDto;
import com.eduassess.dto.TestSubmissionRequest;
import com.eduassess.entity.*;
import com.eduassess.repository.*;
import com.eduassess.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRepository testRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    private String studentToken;
    private com.eduassess.entity.Test testEntity;
    private Question q1;
    private Option q1CorrectOpt;
    private Option q1WrongOpt;

    @BeforeEach
    void setUp() {
        resultRepository.deleteAll();
        optionRepository.deleteAll();
        questionRepository.deleteAll();
        testRepository.deleteAll();
        userRepository.deleteAll();

        User student = User.builder().name("Student").email("student@test.com").password("pass").role(Role.STUDENT).build();
        userRepository.save(student);
        studentToken = "Bearer " + jwtService.generateToken(student);

        testEntity = com.eduassess.entity.Test.builder()
                .title("Sample")
                .subject("Math")
                .duration(60)
                .totalMarks(10)
                .passingMarks(5)
                .status(TestStatus.PUBLISHED)
                .build();
        testEntity = testRepository.save(testEntity);

        q1 = Question.builder().test(testEntity).questionText("1+1?").marks(10).questionOrder(1).build();
        q1 = questionRepository.save(q1);

        q1CorrectOpt = Option.builder().question(q1).optionText("2").optionOrder(1).isCorrect(true).build();
        q1WrongOpt = Option.builder().question(q1).optionText("3").optionOrder(2).isCorrect(false).build();
        optionRepository.save(q1CorrectOpt);
        optionRepository.save(q1WrongOpt);
    }

    @Test
    @DisplayName("Student submits correct answer and backend calculates correct score")
    void studentSubmitsCorrectAnswer() throws Exception {
        TestSubmissionRequest request = TestSubmissionRequest.builder()
                .answers(List.of(
                        AnswerSubmissionDto.builder().questionId(q1.getId()).optionId(q1CorrectOpt.getId()).build()
                )).build();

        mockMvc.perform(post("/api/tests/" + testEntity.getId() + "/submit")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(10))
                .andExpect(jsonPath("$.percentage").value(100.0))
                .andExpect(jsonPath("$.passed").value(true));
    }

    @Test
    @DisplayName("Frontend malicious payload fake score is ignored (score derived from DB)")
    void fakeScoreIsIgnored() throws Exception {
        // Constructing raw JSON to bypass DTO constraints intentionally
        String maliciousPayload = "{\n" +
                "  \"score\": 1000,\n" +
                "  \"percentage\": 999.0,\n" +
                "  \"answers\": [\n" +
                "    {\n" +
                "      \"questionId\": \"" + q1.getId() + "\",\n" +
                "      \"optionId\": \"" + q1WrongOpt.getId() + "\",\n" +
                "      \"isCorrect\": true\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/tests/" + testEntity.getId() + "/submit")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(maliciousPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0)) // Backend calculation remains 0
                .andExpect(jsonPath("$.percentage").value(0.0))
                .andExpect(jsonPath("$.passed").value(false));
    }

    @Test
    @DisplayName("Foreign question injection fails")
    void foreignQuestionFails() throws Exception {
        com.eduassess.entity.Test foreignTest = com.eduassess.entity.Test.builder()
                .title("Foreign").subject("Sub").duration(10).totalMarks(10).passingMarks(5).status(TestStatus.PUBLISHED).build();
        foreignTest = testRepository.save(foreignTest);
        
        Question foreignQ = Question.builder().test(foreignTest).questionText("Foreign").marks(1).questionOrder(1).build();
        foreignQ = questionRepository.save(foreignQ);
        
        Option foreignOpt = Option.builder().question(foreignQ).optionText("Opt").optionOrder(1).isCorrect(true).build();
        optionRepository.save(foreignOpt);

        TestSubmissionRequest request = TestSubmissionRequest.builder()
                .answers(List.of(
                        AnswerSubmissionDto.builder().questionId(foreignQ.getId()).optionId(foreignOpt.getId()).build()
                )).build();

        // Submitting foreign question to the original test
        mockMvc.perform(post("/api/tests/" + testEntity.getId() + "/submit")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // IllegalArgumentException mapped to 400
    }
}
