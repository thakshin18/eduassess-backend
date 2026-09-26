package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackDto {
    private String id;
    private String name;
    private String email;
    private String message;
    private String createdAt;
}
