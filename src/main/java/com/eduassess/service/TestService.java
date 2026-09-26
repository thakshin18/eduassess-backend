package com.eduassess.service;

import com.eduassess.dto.CreateTestRequest;
import com.eduassess.dto.TestDto;
import com.eduassess.dto.UpdateTestRequest;
import com.eduassess.entity.Test;
import com.eduassess.entity.TestStatus;
import com.eduassess.exception.ResourceNotFoundException;
import com.eduassess.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

    public List<TestDto> getAllTests(boolean isAdmin) {
        if (isAdmin) {
            return testRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
        }
        // Students only see published tests
        return testRepository.findByStatus(TestStatus.PUBLISHED).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public TestDto getTestById(String id, boolean isAdmin) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

        if (!isAdmin && test.getStatus() != TestStatus.PUBLISHED) {
            throw new org.springframework.security.access.AccessDeniedException("Test is not published");
        }

        return mapToDto(test);
    }

    public TestDto createTest(CreateTestRequest request) {
        Test test = Test.builder()
                .title(request.getTitle())
                .subject(request.getSubject())
                .description(request.getDescription())
                .duration(request.getDuration())
                .totalMarks(request.getTotalMarks())
                .passingMarks(request.getPassingMarks())
                .difficulty(request.getDifficulty())
                .createdAt(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .status(TestStatus.DRAFT) // default
                .build();

        return mapToDto(testRepository.save(test));
    }

    public TestDto updateTest(String id, UpdateTestRequest request) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

        if (request.getTitle() != null) test.setTitle(request.getTitle());
        if (request.getSubject() != null) test.setSubject(request.getSubject());
        if (request.getDescription() != null) test.setDescription(request.getDescription());
        if (request.getDuration() != null) test.setDuration(request.getDuration());
        if (request.getTotalMarks() != null) test.setTotalMarks(request.getTotalMarks());
        if (request.getPassingMarks() != null) test.setPassingMarks(request.getPassingMarks());
        if (request.getDifficulty() != null) test.setDifficulty(request.getDifficulty());

        return mapToDto(testRepository.save(test));
    }

    public TestDto publishTest(String id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
        test.setStatus(TestStatus.PUBLISHED);
        return mapToDto(testRepository.save(test));
    }

    public TestDto unpublishTest(String id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
        test.setStatus(TestStatus.DRAFT);
        return mapToDto(testRepository.save(test));
    }

    public void deleteTest(String id) {
        if (!testRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test not found with id: " + id);
        }
        testRepository.deleteById(id);
    }

    private TestDto mapToDto(Test test) {
        return TestDto.builder()
                .id(test.getId())
                .title(test.getTitle())
                .subject(test.getSubject())
                .description(test.getDescription())
                .duration(test.getDuration())
                .totalMarks(test.getTotalMarks())
                .passingMarks(test.getPassingMarks())
                .difficulty(test.getDifficulty())
                .createdAt(test.getCreatedAt())
                .status(test.getStatus().name())
                .build();
    }
}
