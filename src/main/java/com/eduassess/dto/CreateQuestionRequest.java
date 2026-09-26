package com.eduassess.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CreateQuestionRequest {
    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Marks required")
    @Min(value = 1, message = "Marks must be positive")
    private Integer marks;

    @NotNull(message = "Question order required")
    private Integer questionOrder;

    @NotNull(message = "Options required")
    @Size(min = 2, message = "At least 2 options are required")
    @Valid
    private List<OptionAdminDto> options;
}
