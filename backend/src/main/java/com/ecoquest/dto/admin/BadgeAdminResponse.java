package com.ecoquest.dto.admin;

import com.ecoquest.entity.enums.BadgeCriteriaType;
import com.ecoquest.entity.enums.QuestCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeAdminResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private BadgeCriteriaType criteriaType;
    private Integer criteriaThreshold;
    private QuestCategory criteriaCategory;
    private Integer ecoCoinsReward;
    private Integer xpReward;
    private Instant createdAt;
}
