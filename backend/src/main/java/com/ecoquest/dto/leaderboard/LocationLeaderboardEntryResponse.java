package com.ecoquest.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationLeaderboardEntryResponse {
    private int rank;
    private String locationName; // a city or a state name, depending on groupBy
    private int totalEcoCoins;
    private int schoolCount;
}
