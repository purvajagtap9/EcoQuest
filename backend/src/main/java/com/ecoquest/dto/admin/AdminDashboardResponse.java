package com.ecoquest.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalStudents;
    private long totalTeachers;
    private long totalSchools;
    private long totalQuests;
    private long completedQuests;      // approved submissions
    private long pendingVerifications; // PENDING + AI_REVIEWED submissions
    private long totalEcoCoins;        // sum across all students' current balance
    private long totalBadgesUnlocked;  // rows in student_badges (Phase 13)
    private double totalTreesPlanted;  // sum of ImpactMetric TREES_PLANTED (Phase 15/16)
    private double totalWasteSegregatedKg;
    private long activeUsers;          // enabled=true across all roles
    private double questCompletionRate; // completedQuests / totalSubmissions, 0 if no submissions
    private double approvalRate;        // approved / (approved + rejected), 0 if none reviewed yet
}
