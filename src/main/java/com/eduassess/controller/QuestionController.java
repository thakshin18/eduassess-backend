package com.eduassess.controller;

import com.eduassess.dto.CreateQuestionRequest;
import com.eduassess.dto.QuestionAdminDto;
import com.eduassess.dto.QuestionStudentDto;
import com.eduassess.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/tests/{testId}/questions")
    public ResponseEntity<QuestionAdminDto> createQuestion(@PathVariable String testId, @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(testId, request));
    }

    @GetMapping("/tests/{testId}/questions/admin")
    public ResponseEntity<List<QuestionAdminDto>> getAdminQuestions(@PathVariable String testId) {
        return ResponseEntity.ok(questionService.getAdminQuestions(testId));
    }

    @GetMapping("/tests/{testId}/questions")
    public ResponseEntity<List<QuestionStudentDto>> getStudentQuestions(@PathVariable String testId) {
        return ResponseEntity.ok(questionService.getStudentQuestions(testId));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionAdminDto> updateQuestion(@PathVariable String id, @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
