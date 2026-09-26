package com.eduassess.service;

import com.eduassess.dto.ResultAdminDto;
import com.eduassess.dto.ResultStudentDto;
import com.eduassess.entity.Result;
import com.eduassess.entity.User;
import com.eduassess.exception.ResourceNotFoundException;
import com.eduassess.repository.ResultRepository;
import com.eduassess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final UserRepository userRepository;

    public List<ResultStudentDto> getMyResults(String userEmail) {
        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return resultRepository.findByUserId(student.getId())
                .stream()
                .map(this::mapToStudentDto)
                .collect(Collectors.toList());
    }

    public ResultStudentDto getMyResultById(String resultId, String userEmail) {
        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found"));

        if (!result.getUser().getId().equals(student.getId())) {
            throw new AccessDeniedException("Access Denied: You do not own this result");
        }

        return mapToStudentDto(result);
    }

    public List<ResultAdminDto> getAllResults() {
        return resultRepository.findAll()
                .stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());
    }

    public ResultAdminDto getResultByIdAdmin(String resultId) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found"));
        return mapToAdminDto(result);
    }

    public List<ResultAdminDto> getResultsByTest(String testId) {
        return resultRepository.findByTestId(testId)
                .stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());
    }

    private ResultStudentDto mapToStudentDto(Result result) {
        return ResultStudentDto.builder()
                .id(result.getId())
                .testId(result.getTest().getId())
                .testTitle(result.getTest().getTitle())
                .score(result.getScore())
                .totalMarks(result.getTotalMarks())
                .percentage(result.getPercentage())
                .correctAnswers(result.getCorrectAnswers())
                .answeredQuestions(result.getAnsweredQuestions())
                .submittedAt(result.getSubmittedAt())
                .passed(result.getPassed())
                .build();
    }

    private ResultAdminDto mapToAdminDto(Result result) {
        return ResultAdminDto.builder()
                .id(result.getId())
                .testId(result.getTest().getId())
                .testTitle(result.getTest().getTitle())
                .studentId(result.getUser().getId())
                .studentName(result.getUser().getName())
                .studentEmail(result.getUser().getEmail())
                .score(result.getScore())
                .totalMarks(result.getTotalMarks())
                .percentage(result.getPercentage())
                .correctAnswers(result.getCorrectAnswers())
                .answeredQuestions(result.getAnsweredQuestions())
                .submittedAt(result.getSubmittedAt())
                .passed(result.getPassed())
                .build();
    }
}
