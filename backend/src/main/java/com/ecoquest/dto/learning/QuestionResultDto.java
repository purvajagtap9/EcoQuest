package com.ecoquest.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResultDto {
    private String questionId;
    private String text;
    private Integer yourAnswerIndex; // null if unanswered
    private Integer correctAnswerIndex;
    private boolean correct;
    private String explanation;
}
