package com.ecoquest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A cached ranking row, NOT the source of truth (that's EcoCoinTransaction, summed — business
 * rule #8). Recomputed whenever a reward-granting event happens (quest approval, quiz reward,
 * badge unlock) and diffed to detect rank changes for the WebSocket push (spec #21) and the
 * "You moved from #15 -> #11!" notification.
 * `scope` distinguishes individual/school boards and `period` distinguishes weekly/monthly/all-time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "leaderboard_snapshots")
public class LeaderboardSnapshot {

    @Id
    private String id;

    @Indexed
    private String scope;       // "INDIVIDUAL" | "SCHOOL" | "CITY" | "STATE" | "NATIONAL"
    @Indexed
    private String period;      // "WEEKLY" | "MONTHLY" | "ALL_TIME"

    private String subjectId;   // studentId or schoolId, depending on scope
    private String subjectName; // display name at time of snapshot
    private String schoolId;    // for individual scope, which school they belong to

    private Integer ecoCoins;
    private Integer xp;
    private Integer badgeCount;
    private Integer rank;

    private Instant computedAt;
}
