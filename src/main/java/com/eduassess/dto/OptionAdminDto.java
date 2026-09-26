package com.eduassess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionAdminDto {
    private String id;
    
    @NotBlank(message = "Option text is required")
    private String optionText;
    
    @NotNull(message = "Option order is required")
    private Integer optionOrder;
    
    @NotNull(message = "Correctness indicator is required")
    private Boolean isCorrect;
}
