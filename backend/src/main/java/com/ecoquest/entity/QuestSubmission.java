package com.ecoquest.entity;

import com.ecoquest.entity.embedded.GeoLocation;
import com.ecoquest.entity.embedded.VerificationResult;
import com.ecoquest.entity.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * One evidence submission for one quest attempt by one student.
 * `rewardGranted` is the guard used to satisfy business rules #1-3: a submission can only ever
 * transition PENDING/AI_REVIEWED -> APPROVED once, and only that transition (never AI, never a
 * second approval) sets rewardGranted=true and creates the matching EcoCoinTransaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "quest_submissions")
@CompoundIndex(name = "student_quest_idx", def = "{'studentId': 1, 'questId': 1}")
public class QuestSubmission {

    @Id
    private String id;

    @Indexed
    private String questId;

    @Indexed
    private String studentId;

    private String imageUrl;
    private String description;
    private GeoLocation submittedLocation;   // null if quest doesn't require GPS
    private boolean withinAllowedRadius;     // computed at submit time; a warning, not an auto-reject unless quest.autoRejectOutsideRadius
    private Double distanceFromTargetMeters; // null if not applicable (no target location or no submitted location)

    private VerificationResult aiVerification; // embedded — advisory only, see VerificationResult

    @Indexed
    private SubmissionStatus status = SubmissionStatus.PENDING;

    private String reviewedByUserId;
    private String rejectionReason;
    private Instant reviewedAt;

    private boolean rewardGranted = false;   // guards against double-rewarding (business rule #1/#3)
    private boolean adminOverride = false;   // true if an admin (not the assigned teacher) decided this

    @CreatedDate
    private Instant createdAt;
}
