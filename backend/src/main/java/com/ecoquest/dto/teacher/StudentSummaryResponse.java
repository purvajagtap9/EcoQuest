package com.ecoquest.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSummaryResponse {
    private String userId;
    private String name;
    private String email;
    private String grade;
    private Integer ecoCoins;
    private Integer xp;
    private Integer currentStreak;
}
