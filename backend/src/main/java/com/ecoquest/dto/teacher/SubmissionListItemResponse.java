package com.ecoquest.dto.teacher;

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
public class SubmissionListItemResponse {
    private String id;
    private String questId;
    private String questTitle;
    private String studentId;
    private String studentName;
    private String imageUrl;
    private SubmissionStatus status;
    private Boolean withinAllowedRadius;
    private Instant createdAt;
}
