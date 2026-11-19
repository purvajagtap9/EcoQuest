package com.ecoquest.entity;

import com.ecoquest.entity.enums.ImpactType;
import com.ecoquest.entity.enums.QuestCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Admin-configurable "quest category X -> +N units of impact metric Y per approved completion"
 * (spec #22). ImpactService falls back to hardcoded sensible defaults for any category with no
 * config row here, so the system works out of the box without requiring setup.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "impact_formula_configs")
public class ImpactFormulaConfig {

    @Id
    private String id;

    @Indexed(unique = true)
    private QuestCategory category;

    private ImpactType impactType;
    private Double valuePerCompletion;
    private String unit;
}
