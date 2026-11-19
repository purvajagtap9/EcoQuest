package com.ecoquest.dto.quest;

import com.ecoquest.entity.enums.QuestCategory;
import com.ecoquest.entity.enums.QuestDifficulty;
import com.ecoquest.entity.enums.QuestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class QuestRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "category is required")
    private QuestCategory category;

    @NotNull(message = "difficulty is required")
    private QuestDifficulty difficulty;

    @NotNull(message = "ecoCoinsReward is required")
    @Positive(message = "ecoCoinsReward must be positive")
    private Integer ecoCoinsReward;

    @NotNull(message = "xpReward is required")
    @Positive(message = "xpReward must be positive")
    private Integer xpReward;

    private boolean proofRequired = true;
    private String requiredProofDescription;

    private boolean gpsRequired = false;
    private Double targetLatitude;
    private Double targetLongitude;
    private Integer allowedRadiusMeters; // falls back to app.quest.default-geo-radius-meters if null and gpsRequired=true
    private boolean autoRejectOutsideRadius = false;

    private LocalDate startDate;
    private LocalDate endDate;

    @Positive(message = "maxSubmissionsPerStudent must be positive")
    private Integer maxSubmissionsPerStudent = 1;

    private QuestStatus status = QuestStatus.ACTIVE;

    // Admin only - ignored/overridden server-side on the teacher endpoint (forced to the
    // teacher's own assigned school). Null = visible to all schools; only admin may set this.
    private String schoolId;
}
