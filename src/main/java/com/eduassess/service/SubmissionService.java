package com.eduassess.service;

import com.eduassess.dto.AnswerSubmissionDto;
import com.eduassess.dto.TestSubmissionRequest;
import com.eduassess.dto.TestSubmissionResponse;
import com.eduassess.entity.*;
import com.eduassess.exception.ResourceNotFoundException;
import com.eduassess.repository.OptionRepository;
import com.eduassess.repository.QuestionRepository;
import com.eduassess.repository.ResultRepository;
import com.eduassess.repository.TestRepository;
import com.eduassess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;
    private final ResultRepository resultRepository;

    @Transactional
    public TestSubmissionResponse submitTest(String testId, String userEmail, TestSubmissionRequest request) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new AccessDeniedException("Test is not published");
        }

        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Evaluate answers
        int score = 0;
        int correctAnswers = 0;
        int answeredQuestions = 0;

        Set<String> processedQuestions = new HashSet<>();
        List<Answer> resultAnswers = new ArrayList<>();

        Result result = Result.builder()
                .test(test)
                .user(student)
                .submittedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        for (AnswerSubmissionDto answerDto : request.getAnswers()) {
            if (!processedQuestions.add(answerDto.getQuestionId())) {
                throw new IllegalArgumentException("Duplicate answers submitted for question: " + answerDto.getQuestionId());
            }

            Question question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + answerDto.getQuestionId()));

            if (!question.getTest().getId().equals(test.getId())) {
                throw new IllegalArgumentException("Question does not belong to this test");
            }

            Option selectedOption = optionRepository.findById(answerDto.getOptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Option not found: " + answerDto.getOptionId()));

            if (!selectedOption.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException("Option does not belong to this question");
            }

            answeredQuestions++;

            // Backend determines correct score truth
            if (selectedOption.getIsCorrect()) {
                correctAnswers++;
                score += question.getMarks();
            }

            resultAnswers.add(Answer.builder()
                    .result(result)
                    .question(question)
                    .selectedOption(selectedOption)
                    .build());
        }

        result.setScore(score);
        result.setTotalMarks(test.getTotalMarks());
        
        double percentage = test.getTotalMarks() > 0 
                ? ((double) score / test.getTotalMarks()) * 100 
                : 0.0;
        result.setPercentage(percentage);
        
        result.setCorrectAnswers(correctAnswers);
        result.setAnsweredQuestions(answeredQuestions);
        result.setPassed(score >= test.getPassingMarks());
        result.setAnswers(resultAnswers);

        resultRepository.save(result);

        return TestSubmissionResponse.builder()
                .id(result.getId())
                .testId(test.getId())
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
