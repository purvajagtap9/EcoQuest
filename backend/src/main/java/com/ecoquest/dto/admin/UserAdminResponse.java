package com.ecoquest.dto.admin;

import com.ecoquest.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminResponse {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private boolean enabled;
    private Instant createdAt;

    // Student-only
    private String schoolId;
    private String grade;
    private Integer ecoCoins;
    private Integer xp;

    // Teacher-only
    private String assignedSchoolId;
}
