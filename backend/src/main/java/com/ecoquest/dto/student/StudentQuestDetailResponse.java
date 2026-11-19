package com.ecoquest.dto.student;

import com.ecoquest.dto.quest.QuestResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentQuestDetailResponse {
    private QuestResponse quest;
    private int mySubmissionCount;
    private boolean canSubmit;
    private String blockReason; // e.g. "Maximum submissions reached", "Quest not yet active", null if canSubmit
}
