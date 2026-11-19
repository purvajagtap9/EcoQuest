package com.ecoquest.dto.teacher;

import com.ecoquest.entity.enums.AiRecommendation;
import com.ecoquest.entity.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDetailResponse {
    private String id;
    private String questId;
    private String questTitle;
    private String questDescription;
    private Integer ecoCoinsReward;
    private Integer xpReward;

    private String studentId;
    private String studentName;

    private String imageUrl;
    private String description;
    private Double submittedLatitude;
    private Double submittedLongitude;
    private String submissionMapUrl; // free OpenStreetMap link for where the student submitted from
    private String targetMapUrl;     // free OpenStreetMap link for the quest's target location
    private Boolean withinAllowedRadius;
    private Double distanceFromTargetMeters;

    // AI verification fields - always null until Phase 16 wires the AI service in
    private Boolean aiDetected;
    private Double aiConfidence;
    private String aiReason;
    private AiRecommendation aiRecommendation;

    private SubmissionStatus status;
    private String reviewedByUserId;
    private String rejectionReason;
    private Instant reviewedAt;
    private boolean adminOverride;
    private Instant createdAt;
}
