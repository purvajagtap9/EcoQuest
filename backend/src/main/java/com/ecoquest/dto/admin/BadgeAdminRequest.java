package com.ecoquest.dto.admin;

import com.ecoquest.entity.enums.BadgeCriteriaType;
import com.ecoquest.entity.enums.QuestCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BadgeAdminRequest {

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "description is required")
    private String description;

    private String icon;

    @NotNull(message = "criteriaType is required")
    private BadgeCriteriaType criteriaType;

    @NotNull(message = "criteriaThreshold is required")
    private Integer criteriaThreshold;

    private QuestCategory criteriaCategory; // required only when criteriaType == QUESTS_COMPLETED_IN_CATEGORY

    private Integer ecoCoinsReward = 0;
    private Integer xpReward = 0;
}
