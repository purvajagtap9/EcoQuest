package com.ecoquest.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/** Not persisted - just the shape of a {_id, total} group aggregation result. */
@Data
@NoArgsConstructor
@AllArgsConstructor
class StudentPeriodTotal {
    @Id
    private String id; // studentId (the aggregation's _id after grouping)
    private Integer total;
}
