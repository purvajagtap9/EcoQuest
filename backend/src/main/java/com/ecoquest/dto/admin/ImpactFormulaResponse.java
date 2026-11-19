package com.ecoquest.dto.admin;

import com.ecoquest.entity.enums.ImpactType;
import com.ecoquest.entity.enums.QuestCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactFormulaResponse {
    private String id;
    private QuestCategory category;
    private ImpactType impactType;
    private Double valuePerCompletion;
    private String unit;
    private boolean isDefault; // true when there's no admin override - showing the built-in default
}
