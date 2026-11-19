package com.ecoquest.dto.impact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactSummaryResponse {
    private List<ImpactLineItem> items;
    private String disclaimer;
}
