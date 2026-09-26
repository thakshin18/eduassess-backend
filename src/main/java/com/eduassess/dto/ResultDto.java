package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultDto {
    private String id;
    private String testId;
    private String userId;
    private Integer score;
    private Boolean passed;
    private String submittedAt;
}
