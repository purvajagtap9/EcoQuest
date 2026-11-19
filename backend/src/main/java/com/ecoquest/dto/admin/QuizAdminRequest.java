package com.ecoquest.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizAdminRequest {

    @NotBlank(message = "topicId is required")
    private String topicId;

    @NotBlank(message = "title is required")
    private String title;

    @Valid
    @NotEmpty(message = "At least one question is required")
    private List<QuestionAdminDto> questions;

    private Integer coinsPerCorrectAnswer = 5;
    private Integer xpPerCorrectAnswer = 10;
    private boolean published = true;
}
