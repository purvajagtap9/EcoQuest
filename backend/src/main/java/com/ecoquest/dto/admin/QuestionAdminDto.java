package com.ecoquest.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionAdminDto {

    private String id; // optional on create - service auto-assigns q1, q2... if blank

    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull
    @Size(min = 4, max = 4, message = "Exactly 4 options are required")
    private List<String> options;

    @NotNull(message = "correctOptionIndex is required")
    private Integer correctOptionIndex;

    private String explanation;
}
