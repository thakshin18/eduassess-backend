package com.eduassess.service;

import com.eduassess.dto.CreateQuestionRequest;
import com.eduassess.dto.OptionAdminDto;
import com.eduassess.dto.OptionStudentDto;
import com.eduassess.dto.QuestionAdminDto;
import com.eduassess.dto.QuestionStudentDto;
import com.eduassess.entity.Option;
import com.eduassess.entity.Question;
import com.eduassess.entity.Test;
import com.eduassess.entity.TestStatus;
import com.eduassess.exception.ResourceNotFoundException;
import com.eduassess.repository.QuestionRepository;
import com.eduassess.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;

    public QuestionAdminDto createQuestion(String testId, CreateQuestionRequest request) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        validateOptions(request.getOptions());

        Question question = Question.builder()
                .test(test)
                .questionText(request.getQuestionText())
                .marks(request.getMarks())
                .questionOrder(request.getQuestionOrder())
                .build();

        List<Option> options = request.getOptions().stream().map(dto -> Option.builder()
                .question(question)
                .optionText(dto.getOptionText())
                .optionOrder(dto.getOptionOrder())
                .isCorrect(dto.getIsCorrect())
                .build()).collect(Collectors.toList());

        question.setOptions(options);
        return mapToAdminDto(questionRepository.save(question));
    }

    public List<QuestionAdminDto> getAdminQuestions(String testId) {
        if (!testRepository.existsById(testId)) throw new ResourceNotFoundException("Test not found");
        return questionRepository.findByTestIdOrderByQuestionOrderAsc(testId)
                .stream().map(this::mapToAdminDto).collect(Collectors.toList());
    }

    public List<QuestionStudentDto> getStudentQuestions(String testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new AccessDeniedException("Test is not available");
        }

        return questionRepository.findByTestIdOrderByQuestionOrderAsc(testId)
                .stream().map(this::mapToStudentDto).collect(Collectors.toList());
    }

    public QuestionAdminDto updateQuestion(String questionId, CreateQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        validateOptions(request.getOptions());

        question.setQuestionText(request.getQuestionText());
        question.setMarks(request.getMarks());
        question.setQuestionOrder(request.getQuestionOrder());

        // Clear and recreate options to handle modifications easily without orphan issues
        question.getOptions().clear();
        
        List<Option> newOptions = request.getOptions().stream().map(dto -> Option.builder()
                .question(question)
                .optionText(dto.getOptionText())
                .optionOrder(dto.getOptionOrder())
                .isCorrect(dto.getIsCorrect())
                .build()).collect(Collectors.toList());

        question.getOptions().addAll(newOptions);
        return mapToAdminDto(questionRepository.save(question));
    }

    public void deleteQuestion(String questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found");
        }
        questionRepository.deleteById(questionId);
    }

    private void validateOptions(List<OptionAdminDto> options) {
        long correctCount = options.stream().filter(OptionAdminDto::getIsCorrect).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Question must have exactly one correct option");
        }
    }

    private QuestionAdminDto mapToAdminDto(Question question) {
        return QuestionAdminDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .marks(question.getMarks())
                .questionOrder(question.getQuestionOrder())
                .options(question.getOptions().stream().map(opt -> OptionAdminDto.builder()
                        .id(opt.getId())
                        .optionText(opt.getOptionText())
                        .optionOrder(opt.getOptionOrder())
                        .isCorrect(opt.getIsCorrect())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    private QuestionStudentDto mapToStudentDto(Question question) {
        return QuestionStudentDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .marks(question.getMarks())
                .questionOrder(question.getQuestionOrder())
                .options(question.getOptions().stream().map(opt -> OptionStudentDto.builder()
                        .id(opt.getId())
                        .optionText(opt.getOptionText())
                        .optionOrder(opt.getOptionOrder())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
