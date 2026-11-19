package com.ecoquest.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponse {
    private int rank;
    private String studentId;
    private String displayName; // respects showRealNameOnLeaderboard - see LeaderboardService
    private int ecoCoins;       // period-scoped for WEEKLY/MONTHLY, current balance for ALL_TIME
    private int xp;             // always cumulative - see phase note on why XP isn't period-scoped
    private int badgeCount;     // always cumulative
    private String schoolId;
}
