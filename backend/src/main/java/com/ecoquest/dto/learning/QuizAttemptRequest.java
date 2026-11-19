package com.ecoquest.dto.learning;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class QuizAttemptRequest {

    /** questionId -> selected option index (0-3) */
    @NotEmpty(message = "answers cannot be empty")
    private Map<String, Integer> answers;
}
