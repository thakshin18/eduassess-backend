package com.eduassess.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuestionAdminDto {
    private String id;
    private String questionText;
    private Integer marks;
    private Integer questionOrder;
    private List<OptionAdminDto> options;
}
