package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuestionDto {
    private String id;
    private String text;
    private List<String> options;
    // Note: 'answer' is intentionally omitted here to prevent leaking correct answers to students.
    // An AdminQuestionDto might include it.
}
