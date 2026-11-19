package com.ecoquest.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankChangeMessage {
    private int oldRank;
    private int newRank;
    private String message;
}
