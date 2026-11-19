package com.ecoquest.dto.quest;

import com.ecoquest.entity.enums.QuestCategory;
import com.ecoquest.entity.enums.QuestDifficulty;
import com.ecoquest.entity.enums.QuestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestResponse {
    private String id;
    private String title;
    private String description;
    private QuestCategory category;
    private QuestDifficulty difficulty;
    private Integer ecoCoinsReward;
    private Integer xpReward;
    private boolean proofRequired;
    private String requiredProofDescription;
    private boolean gpsRequired;
    private Double targetLatitude;
    private Double targetLongitude;
    private String targetLocationMapUrl; // free OpenStreetMap link, null if no target location
    private Integer allowedRadiusMeters;
    private boolean autoRejectOutsideRadius;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxSubmissionsPerStudent;
    private QuestStatus status;
    private String schoolId; // null = available to all schools
    private String createdByUserId;
}
