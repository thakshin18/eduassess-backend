package com.eduassess.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateTestRequest {
    private String title;
    private String subject;
    private String description;
    
    @Min(value = 1, message = "Duration must be positive")
    private Integer duration;

    @Min(value = 1, message = "Total marks must be positive")
    private Integer totalMarks;

    @Min(value = 0, message = "Passing marks cannot be negative")
    private Integer passingMarks;

    private String difficulty;
}
