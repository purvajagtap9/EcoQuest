package com.ecoquest.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolLeaderboardEntryResponse {
    private int rank;
    private String schoolId;
    private String schoolName;
    private String city;
    private String state;
    private int totalEcoCoins;
    private int studentCount;
}
