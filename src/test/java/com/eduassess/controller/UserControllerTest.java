package com.eduassess.controller;

import com.eduassess.entity.Role;
import com.eduassess.entity.User;
import com.eduassess.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String studentToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User admin = User.builder().name("Admin").email("admin@test.com").password("pass").role(Role.ADMIN).build();
        User student = User.builder().name("Student").email("student@test.com").password("pass").role(Role.STUDENT).build();
        
        userRepository.save(admin);
        testUser = userRepository.save(student);

        adminToken = "Bearer " + jwtService.generateToken(admin);
        studentToken = "Bearer " + jwtService.generateToken(student);
    }

    @Test
    @DisplayName("Admin can get all users")
    void adminCanGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Student cannot get all users")
    void studentCannotGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can delete user")
    void adminCanDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
