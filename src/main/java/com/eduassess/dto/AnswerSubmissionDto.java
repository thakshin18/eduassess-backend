package com.eduassess.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmissionDto {
    @NotBlank(message = "Question ID is required")
    private String questionId;
    
    @NotBlank(message = "Option ID is required")
    private String optionId;
}
