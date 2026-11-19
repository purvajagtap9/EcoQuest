package com.ecoquest.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String schoolId;
    private String grade;
    private String preferredLanguage;
    private String displayName;
    private boolean showRealNameOnLeaderboard;
}
