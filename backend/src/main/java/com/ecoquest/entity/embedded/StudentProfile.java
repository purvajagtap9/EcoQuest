package com.ecoquest.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded inside User when role == STUDENT. Balances (ecoCoins, xp) are denormalized caches
 * for fast dashboard reads — the source of truth is EcoCoinTransaction (summed) and XP events;
 * see EcoCoinService, which is the only code path allowed to change these fields
 * (students cannot modify them directly — business rule #7).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {
    private String schoolId;
    private String grade;                 // e.g. "10th", "B.Tech 2nd Year"
    private String preferredLanguage = "en"; // en | hi | mr
    private Integer ecoCoins = 0;
    private Integer xp = 0;
    private Streak streak = new Streak();
    private String displayName;           // for leaderboards; admin can set a privacy-friendly alias
    private Boolean showRealNameOnLeaderboard = true;
}
