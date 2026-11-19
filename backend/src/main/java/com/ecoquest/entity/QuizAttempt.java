package com.ecoquest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * One record per completed attempt. Reward-abuse prevention (spec #9, business rule #1) works
 * by: only the FIRST attempt per (studentId, quizId) awards coins/XP — see QuizService.
 * Later attempts are still stored (for retake/practice) but are flagged rewarded=false.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "quiz_attempts")
@CompoundIndex(name = "student_quiz_idx", def = "{'studentId': 1, 'quizId': 1}")
public class QuizAttempt {

    @Id
    private String id;

    @Indexed
    private String studentId;

    @Indexed
    private String quizId;

    private Map<String, Integer> selectedAnswers; // questionId -> selected option index
    private List<String> incorrectQuestionIds;

    private Integer score;          // number correct
    private Integer totalQuestions;
    private Integer coinsEarned;
    private Integer xpEarned;
    private boolean rewarded;       // true only for the first attempt on this quiz

    @CreatedDate
    private Instant createdAt;
}
