package com.ecoquest.entity;

import com.ecoquest.entity.embedded.GeoLocation;
import com.ecoquest.entity.enums.QuestCategory;
import com.ecoquest.entity.enums.QuestDifficulty;
import com.ecoquest.entity.enums.QuestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "quests")
public class Quest {

    @Id
    private String id;

    private String title;
    private String description;
    private QuestCategory category;
    private QuestDifficulty difficulty;

    private Integer ecoCoinsReward;
    private Integer xpReward;

    private boolean proofRequired = true;
    private String requiredProofDescription; // e.g. "Photo of segregated waste bins"

    private boolean gpsRequired = false;
    private GeoLocation targetLocation;       // required only if gpsRequired
    private Integer allowedRadiusMeters;      // e.g. 500; falls back to app.quest.default-geo-radius-meters
    private boolean autoRejectOutsideRadius = false; // if true, a submission outside the radius is auto-REJECTED at submit time instead of just flagged for teacher review

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxSubmissionsPerStudent = 1;

    private QuestStatus status = QuestStatus.ACTIVE;

    @Indexed
    private String createdByUserId;   // teacher or admin who created it
    private String schoolId;          // null = available to all schools

    @CreatedDate
    private Instant createdAt;
}
