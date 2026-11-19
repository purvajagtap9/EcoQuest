package com.ecoquest.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardResponse {
    private String name;
    private Integer ecoCoins;
    private Integer level;
    private String levelTitle;
    private Integer xp;
    private Integer xpForNextLevel; // 0 means already at max level
    private Integer currentStreak;
    private Integer longestStreak;
    private Long completedQuests;      // count of APPROVED submissions
    private Long pendingVerification;  // count of PENDING + AI_REVIEWED submissions
    private Integer badgeCount;
    private Integer rank;              // live rank by eco-coins among all students; see StudentService
}
