package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TestDto {
    private String id;
    private String title;
    private String subject;
    private String description;
    private Integer duration;
    private Integer totalMarks;
    private Integer passingMarks;
    private String difficulty;
    private String createdAt;
    private String status;
    private List<QuestionDto> questions;
}
