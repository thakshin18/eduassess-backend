package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultAdminDto {
    private String id;
    private String testId;
    private String testTitle;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Integer correctAnswers;
    private Integer answeredQuestions;
    private String submittedAt;
    private Boolean passed;
}
