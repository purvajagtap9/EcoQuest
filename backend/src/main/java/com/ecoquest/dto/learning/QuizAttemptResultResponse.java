package com.ecoquest.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResultResponse {
    private Integer score;
    private Integer totalQuestions;
    private Integer coinsEarned;   // 0 if this wasn't the student's first attempt on this quiz
    private Integer xpEarned;      // 0 if this wasn't the student's first attempt on this quiz
    private boolean rewarded;      // false = retake, shown so the frontend can explain why coins are 0
    private List<QuestionResultDto> results;
}
