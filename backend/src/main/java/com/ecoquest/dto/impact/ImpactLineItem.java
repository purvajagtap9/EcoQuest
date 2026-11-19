package com.ecoquest.dto.impact;

import com.ecoquest.entity.enums.ImpactType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactLineItem {
    private ImpactType type;
    private double totalValue;
    private String unit;
}
