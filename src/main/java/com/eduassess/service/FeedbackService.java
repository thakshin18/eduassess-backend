package com.eduassess.service;

import com.eduassess.dto.FeedbackDto;
import com.eduassess.entity.Feedback;
import com.eduassess.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional(readOnly = true)
    public List<FeedbackDto> getAllFeedback() {
        return feedbackRepository.findAll().stream()
                .map(this::mapToDto)
                .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt())) // Descending order
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackDto submitFeedback(FeedbackDto dto) {
        Feedback feedback = Feedback.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .message(dto.getMessage())
                .createdAt(Instant.now().toString())
                .build();
        
        feedback = feedbackRepository.save(feedback);
        return mapToDto(feedback);
    }

    @Transactional
    public void deleteFeedback(String id) {
        if (!feedbackRepository.existsById(id)) {
            throw new RuntimeException("Feedback not found");
        }
        feedbackRepository.deleteById(id);
    }

    private FeedbackDto mapToDto(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .name(feedback.getName())
                .email(feedback.getEmail())
                .message(feedback.getMessage())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
