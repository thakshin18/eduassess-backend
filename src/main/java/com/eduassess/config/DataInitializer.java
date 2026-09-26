package com.eduassess.config;

import com.eduassess.entity.Role;
import com.eduassess.entity.User;
import com.eduassess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initUsers() {
        return args -> {
            // Admin user upsert
            User admin = userRepository.findByEmail("admin@example.com").orElse(null);
            if (admin == null) {
                admin = User.builder()
                        .name("Admin")
                        .email("admin@example.com")
                        .grade(null)
                        .phone(null)
                        .joinedAt(null)
                        .build();
            }
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Student user upsert
            User student = userRepository.findByEmail("student@example.com").orElse(null);
            if (student == null) {
                student = User.builder()
                        .name("Student")
                        .email("student@example.com")
                        .grade(null)
                        .phone(null)
                        .joinedAt(null)
                        .build();
            }
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRole(Role.STUDENT);
            userRepository.save(student);
        };
    }
}
