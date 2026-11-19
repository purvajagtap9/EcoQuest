package com.ecoquest.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedded inside Quiz.questions rather than its own collection — questions are always
 * created, edited, and fetched as part of "the quiz," never independently. Each question
 * keeps a stable short `id` (not the Mongo document id) so QuizAttempt.answers can reference
 * it without needing a real join.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String id;              // stable id within the quiz, e.g. "q1"
    private String text;
    private List<String> options;   // exactly 4 options
    private Integer correctOptionIndex; // 0-3 - NEVER sent to the frontend before attempt is graded
    private String explanation;
}
