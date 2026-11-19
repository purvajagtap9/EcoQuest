package com.ecoquest.entity;

import com.ecoquest.entity.enums.BadgeCriteriaType;
import com.ecoquest.entity.enums.QuestCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "badges")
public class Badge {

    @Id
    private String id;

    private String code;        // e.g. "FIRST_STEP", "WASTE_WARRIOR"
    private String name;
    private String description;
    private String icon;        // emoji or icon key, e.g. "🌱"

    private BadgeCriteriaType criteriaType;
    private Integer criteriaThreshold;         // e.g. 10 (quests), 7 (streak days), 5000 (coins)
    private QuestCategory criteriaCategory;    // only used when criteriaType == QUESTS_COMPLETED_IN_CATEGORY

    private Integer ecoCoinsReward = 0;
    private Integer xpReward = 0;

    @CreatedDate
    private Instant createdAt;
}
