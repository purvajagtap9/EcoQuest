package com.ecoquest.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBadgeResponse {
    private String badgeId;
    private String code;
    private String name;
    private String description;
    private String icon;
    private boolean earned;
    private Instant earnedAt; // null if not earned
}
