package com.eduassess.controller;

import com.eduassess.dto.CreateTestRequest;
import com.eduassess.dto.TestDto;
import com.eduassess.dto.UpdateTestRequest;
import com.eduassess.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @GetMapping
    public ResponseEntity<List<TestDto>> getAllTests(Authentication authentication) {
        return ResponseEntity.ok(testService.getAllTests(isAdmin(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestDto> getTestById(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(testService.getTestById(id, isAdmin(authentication)));
    }

    @PostMapping
    public ResponseEntity<TestDto> createTest(@Valid @RequestBody CreateTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.createTest(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestDto> updateTest(@PathVariable String id, @Valid @RequestBody UpdateTestRequest request) {
        return ResponseEntity.ok(testService.updateTest(id, request));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<TestDto> publishTest(@PathVariable String id) {
        return ResponseEntity.ok(testService.publishTest(id));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<TestDto> unpublishTest(@PathVariable String id) {
        return ResponseEntity.ok(testService.unpublishTest(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable String id) {
        testService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }
}
