package com.eduassess.controller;

import com.eduassess.entity.*;
import com.eduassess.repository.*;
import com.eduassess.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRepository testRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private JwtService jwtService;

    private String studentAToken;
    private String adminToken;
    private com.eduassess.entity.Test testEntity;
    private Result studentBResult;

    @BeforeEach
    void setUp() {
        resultRepository.deleteAll();
        testRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder().name("Admin").email("admin@test.com").password("pass").role(Role.ADMIN).build();
        User studentA = User.builder().name("StudentA").email("studentA@test.com").password("pass").role(Role.STUDENT).build();
        User studentB = User.builder().name("StudentB").email("studentB@test.com").password("pass").role(Role.STUDENT).build();
        
        userRepository.save(admin);
        userRepository.save(studentA);
        userRepository.save(studentB);

        adminToken = "Bearer " + jwtService.generateToken(admin);
        studentAToken = "Bearer " + jwtService.generateToken(studentA);

        testEntity = com.eduassess.entity.Test.builder()
                .title("Sample")
                .subject("Math")
                .duration(60)
                .totalMarks(10)
                .passingMarks(5)
                .status(TestStatus.PUBLISHED)
                .build();
        testEntity = testRepository.save(testEntity);

        studentBResult = Result.builder()
                .user(studentB)
                .test(testEntity)
                .score(10)
                .totalMarks(10)
                .percentage(100.0)
                .correctAnswers(1)
                .answeredQuestions(1)
                .passed(true)
                .submittedAt("2026-08-25T12:00:00")
                .build();
        studentBResult = resultRepository.save(studentBResult);
    }

    @Test
    @DisplayName("Cross-User Result Access Blocked (Mandatory)")
    void studentACannotAccessStudentBResult() throws Exception {
        // Student A attempts to access Student B's Result directly by ID
        mockMvc.perform(get("/api/results/my/" + studentBResult.getId())
                .header("Authorization", studentAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin receives correct result data")
    void adminCanAccessAnyResult() throws Exception {
        mockMvc.perform(get("/api/results/" + studentBResult.getId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentEmail").value("studentB@test.com"))
                .andExpect(jsonPath("$.score").value(10));
    }

    @Test
    @DisplayName("Student cannot access Admin results list")
    void studentCannotAccessAdminList() throws Exception {
        mockMvc.perform(get("/api/results")
                .header("Authorization", studentAToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
