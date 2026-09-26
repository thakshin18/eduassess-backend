package com.eduassess.controller;

import com.eduassess.dto.ResultAdminDto;
import com.eduassess.dto.ResultStudentDto;
import com.eduassess.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    // --- STUDENT ENDPOINTS ---

    @GetMapping("/my")
    public ResponseEntity<List<ResultStudentDto>> getMyResults(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(resultService.getMyResults(userEmail));
    }

    @GetMapping("/my/{resultId}")
    public ResponseEntity<ResultStudentDto> getMyResultById(@PathVariable String resultId, Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(resultService.getMyResultById(resultId, userEmail));
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping
    public ResponseEntity<List<ResultAdminDto>> getAllResults() {
        return ResponseEntity.ok(resultService.getAllResults());
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ResultAdminDto> getResultByIdAdmin(@PathVariable String resultId) {
        return ResponseEntity.ok(resultService.getResultByIdAdmin(resultId));
    }

    @DeleteMapping("/{resultId}")
    public ResponseEntity<Void> deleteResult(@PathVariable String resultId, Authentication authentication) {
        // Results are immutable; deletion is not allowed.
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<List<ResultAdminDto>> getResultsByTest(@PathVariable String testId) {
        return ResponseEntity.ok(resultService.getResultsByTest(testId));
    }
}
