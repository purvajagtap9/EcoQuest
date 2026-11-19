package com.ecoquest.dto.quest;

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
public class QuestSubmissionResponse {
    private String id;
    private String questId;
    private String questTitle;
    private String imageUrl;
    private String description;
    private Double submittedLatitude;
    private Double submittedLongitude;
    private String mapUrl; // free OpenStreetMap link, null if no location submitted
    private Boolean withinAllowedRadius; // null when the quest doesn't require GPS
    private Double distanceFromTargetMeters; // null when not applicable
    private SubmissionStatus status;
    private String rejectionReason;
    private Instant createdAt;
}
