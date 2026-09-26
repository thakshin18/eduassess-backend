package com.eduassess.controller;

import com.eduassess.dto.TestSubmissionRequest;
import com.eduassess.dto.TestSubmissionResponse;
import com.eduassess.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/{testId}/submit")
    public ResponseEntity<TestSubmissionResponse> submitTest(
            @PathVariable String testId,
            @Valid @RequestBody TestSubmissionRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        TestSubmissionResponse response = submissionService.submitTest(testId, userEmail, request);
        return ResponseEntity.ok(response);
    }
}
