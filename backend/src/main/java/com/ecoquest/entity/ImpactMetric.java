package com.ecoquest.entity;

import com.ecoquest.entity.enums.ImpactType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * One record per approved submission's estimated environmental contribution. Values come from
 * CONFIGURABLE formulas (spec #22 - e.g. "tree quest -> +1 tree"), applied once at approval
 * time and never claimed as scientifically precise — see ImpactFormulaConfig (Phase 15).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "impact_metrics")
public class ImpactMetric {

    @Id
    private String id;

    private ImpactType type;
    private Double value;
    private String unit; // "trees", "kg", "L", "kWh", "trips", "cleanups"

    @Indexed
    private String studentId;
    private String schoolId;

    @Indexed
    private String submissionId; // traceability back to the approved QuestSubmission

    @CreatedDate
    private Instant createdAt;
}
