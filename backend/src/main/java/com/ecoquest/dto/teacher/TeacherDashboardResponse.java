package com.ecoquest.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDashboardResponse {
    private Long pendingReviews;
    private Long approvedToday;
    private Long rejectedToday;
    private Long totalStudents;
}
