package com.eduassess.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .exceptionHandling(exception -> { exception.authenticationEntryPoint(jwtAuthenticationEntryPoint); exception.accessDeniedHandler((request, response, accessDeniedException) -> { response.setStatus(HttpServletResponse.SC_FORBIDDEN); response.setContentType("application/json"); response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}"); }); })
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Require ADMIN role for user modifications and deletions
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                // Question Endpoints under Tests
                .requestMatchers(HttpMethod.POST, "/api/tests/*/questions").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tests/*/questions/admin").hasRole("ADMIN")
                // Submission Endpoint
                .requestMatchers(HttpMethod.POST, "/api/tests/*/submit").hasRole("STUDENT")
                // Protect tests API: creation/modification require ADMIN
                .requestMatchers(HttpMethod.POST, "/api/tests/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tests").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tests/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tests").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tests/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tests").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tests/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tests").hasRole("ADMIN")
                // PUT/DELETE questions
                .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")
                // Result Endpoints
                .requestMatchers(HttpMethod.GET, "/api/results/my").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/api/results/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/results/my/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/results").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/results/{resultId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/results/test/**").hasRole("ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use a configurable frontend URL or default to localhost:3000
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Hardcoded for now, could be passed from env
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
