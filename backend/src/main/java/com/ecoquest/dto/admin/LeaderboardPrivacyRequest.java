package com.ecoquest.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaderboardPrivacyRequest {

    private String displayName; // if null/blank, falls back to a generated "Student XXXX" label

    @NotNull(message = "showRealNameOnLeaderboard is required")
    private Boolean showRealNameOnLeaderboard;
}
