package com.ecoquest.dto.admin;

import com.ecoquest.entity.enums.ImpactType;
import com.ecoquest.entity.enums.QuestCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ImpactFormulaRequest {

    @NotNull(message = "category is required")
    private QuestCategory category;

    @NotNull(message = "impactType is required")
    private ImpactType impactType;

    @NotNull(message = "valuePerCompletion is required")
    @Positive(message = "valuePerCompletion must be positive")
    private Double valuePerCompletion;

    @NotBlank(message = "unit is required")
    private String unit;
}
