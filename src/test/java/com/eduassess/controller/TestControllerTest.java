package com.eduassess.controller;

import com.eduassess.dto.CreateTestRequest;
import com.eduassess.dto.TestDto;
import com.eduassess.entity.Role;
import com.eduassess.entity.TestStatus;
import com.eduassess.entity.User;
import com.eduassess.repository.TestRepository;
import com.eduassess.repository.UserRepository;
import com.eduassess.security.JwtService;
import com.eduassess.service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        testRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder().name("Admin").email("admin@test.com").password("pass").role(Role.ADMIN).build();
        User student = User.builder().name("Student").email("student@test.com").password("pass").role(Role.STUDENT).build();
        userRepository.save(admin);
        userRepository.save(student);

        adminToken = "Bearer " + jwtService.generateToken(admin);
        studentToken = "Bearer " + jwtService.generateToken(student);
    }

    @Test
    void adminCanCreateTest() throws Exception {
        CreateTestRequest request = CreateTestRequest.builder()
                .title("New Test")
                .subject("Math")
                .description("Desc")
                .duration(60)
                .totalMarks(100)
                .passingMarks(50)
                .difficulty("Hard")
                .build();

        mockMvc.perform(post("/api/tests")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Test"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void studentCannotCreateTest() throws Exception {
        CreateTestRequest request = CreateTestRequest.builder().title("Test").build();

        mockMvc.perform(post("/api/tests")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanSeeAllTests() throws Exception {
        com.eduassess.entity.Test draft = com.eduassess.entity.Test.builder().title("Draft").subject("Sub").duration(10).totalMarks(10).passingMarks(5).status(TestStatus.DRAFT).build();
        com.eduassess.entity.Test pub = com.eduassess.entity.Test.builder().title("Pub").subject("Sub").duration(10).totalMarks(10).passingMarks(5).status(TestStatus.PUBLISHED).build();
        testRepository.save(draft);
        testRepository.save(pub);

        mockMvc.perform(get("/api/tests")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void studentCanOnlySeePublishedTests() throws Exception {
        com.eduassess.entity.Test draft = com.eduassess.entity.Test.builder().title("Draft").subject("Sub").duration(10).totalMarks(10).passingMarks(5).status(TestStatus.DRAFT).build();
        com.eduassess.entity.Test pub = com.eduassess.entity.Test.builder().title("Pub").subject("Sub").duration(10).totalMarks(10).passingMarks(5).status(TestStatus.PUBLISHED).build();
        testRepository.save(draft);
        testRepository.save(pub);

        mockMvc.perform(get("/api/tests")
                .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Pub"));
    }
}
