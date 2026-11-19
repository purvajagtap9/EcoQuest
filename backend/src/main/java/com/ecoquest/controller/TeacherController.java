package com.ecoquest.controller;

import com.ecoquest.dto.impact.ImpactSummaryResponse;
import com.ecoquest.dto.leaderboard.LeaderboardEntryResponse;
import com.ecoquest.dto.teacher.StudentSummaryResponse;
import com.ecoquest.dto.teacher.TeacherDashboardResponse;
import com.ecoquest.entity.enums.LeaderboardPeriod;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.ImpactService;
import com.ecoquest.service.LeaderboardService;
import com.ecoquest.service.TeacherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Everything here is scoped to the authenticated teacher's own assigned school
 * (business rule #4) — no cross-school access exists through this controller.
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher", description = "Teacher dashboard, student roster, class leaderboard, and class impact")
public class TeacherController {

    private final TeacherService teacherService;
    private final LeaderboardService leaderboardService;
    private final ImpactService impactService;

    @GetMapping("/dashboard")
    public TeacherDashboardResponse getDashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        return teacherService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/students")
    public Page<StudentSummaryResponse> getStudents(@AuthenticationPrincipal CustomUserDetails principal,
                                                      Pageable pageable) {
        return teacherService.getAssignedStudents(principal.getUser().getId(), pageable);
    }

    @GetMapping("/leaderboard")
    public Page<LeaderboardEntryResponse> getClassLeaderboard(
            @RequestParam(defaultValue = "ALL_TIME") LeaderboardPeriod period,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        String schoolId = teacherService.getAssignedSchoolId(principal.getUser().getId());
        return leaderboardService.getIndividualLeaderboardForSchool(schoolId, period, pageable);
    }

    @GetMapping("/impact")
    public ImpactSummaryResponse getClassImpact(@AuthenticationPrincipal CustomUserDetails principal) {
        String schoolId = teacherService.getAssignedSchoolId(principal.getUser().getId());
        return impactService.getSummaryForSchool(schoolId);
    }
}
