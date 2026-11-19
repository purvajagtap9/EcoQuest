package com.ecoquest.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAdminResponse {
    private String id;
    private String topicId;
    private String title;
    private List<QuestionAdminDto> questions;
    private Integer coinsPerCorrectAnswer;
    private Integer xpPerCorrectAnswer;
    private boolean published;
    private Instant createdAt;
}
