package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionStudentDto {
    private String id;
    private String optionText;
    private Integer optionOrder;
    // CRITICAL: isCorrect intentionally omitted
}
