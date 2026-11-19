package com.ecoquest.controller;

import com.ecoquest.dto.impact.ImpactSummaryResponse;
import com.ecoquest.dto.leaderboard.LeaderboardEntryResponse;
import com.ecoquest.dto.leaderboard.SchoolLeaderboardEntryResponse;
import com.ecoquest.dto.quest.QuestResponse;
import com.ecoquest.dto.quest.QuestSubmissionResponse;
import com.ecoquest.dto.student.CoinTransactionResponse;
import com.ecoquest.dto.student.StudentBadgeResponse;
import com.ecoquest.dto.student.StudentDashboardResponse;
import com.ecoquest.dto.student.StudentProfileResponse;
import com.ecoquest.dto.student.StudentQuestDetailResponse;
import com.ecoquest.entity.EcoCoinTransaction;
import com.ecoquest.entity.enums.LeaderboardPeriod;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.BadgeService;
import com.ecoquest.service.EcoCoinService;
import com.ecoquest.service.ImpactService;
import com.ecoquest.service.LeaderboardService;
import com.ecoquest.service.QuestService;
import com.ecoquest.service.QuestSubmissionService;
import com.ecoquest.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Everything here is scoped to the currently-authenticated student — there is no
 * /api/student/{id}/... route, deliberately: a student can never fetch another student's
 * dashboard/profile through this controller. Admin cross-student access lives under /api/admin.
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "Student", description = "Student dashboard, profile, quests, submissions, coins, badges, leaderboards, and impact")
public class StudentController {

    private final StudentService studentService;
    private final QuestService questService;
    private final QuestSubmissionService submissionService;
    private final EcoCoinService ecoCoinService;
    private final BadgeService badgeService;
    private final LeaderboardService leaderboardService;
    private final ImpactService impactService;

    @GetMapping("/profile")
    public StudentProfileResponse getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return studentService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/dashboard")
    public StudentDashboardResponse getDashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        return studentService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/quests")
    public List<QuestResponse> listQuests(@AuthenticationPrincipal CustomUserDetails principal) {
        return questService.listVisibleQuestsForStudent(principal.getUser().getId());
    }

    @GetMapping("/quests/{id}")
    public StudentQuestDetailResponse getQuest(@PathVariable String id,
                                                @AuthenticationPrincipal CustomUserDetails principal) {
        return questService.getQuestDetailForStudent(principal.getUser().getId(), id);
    }

    @GetMapping("/submissions")
    public Page<QuestSubmissionResponse> listSubmissions(Pageable pageable,
                                                           @AuthenticationPrincipal CustomUserDetails principal) {
        return submissionService.listMySubmissions(principal.getUser().getId(), pageable);
    }

    @GetMapping("/submissions/{id}")
    public QuestSubmissionResponse getSubmission(@PathVariable String id,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return submissionService.getMySubmission(principal.getUser().getId(), id);
    }

    @GetMapping("/coin-transactions")
    public Page<CoinTransactionResponse> listCoinTransactions(Pageable pageable,
                                                                @AuthenticationPrincipal CustomUserDetails principal) {
        return ecoCoinService.getHistory(principal.getUser().getId(), pageable).map(this::toCoinResponse);
    }

    @GetMapping("/badges")
    public List<StudentBadgeResponse> listBadges(@AuthenticationPrincipal CustomUserDetails principal) {
        return badgeService.listForStudent(principal.getUser().getId());
    }

    /** scope=individual (default) ranks all students; scope=school ranks schools. */
    @GetMapping("/leaderboard")
    public Object getLeaderboard(@RequestParam(defaultValue = "individual") String scope,
                                  @RequestParam(defaultValue = "ALL_TIME") LeaderboardPeriod period,
                                  Pageable pageable) {
        if ("school".equalsIgnoreCase(scope)) {
            return leaderboardService.getSchoolLeaderboard(period, pageable);
        }
        return leaderboardService.getIndividualLeaderboard(period, pageable);
    }

    @GetMapping("/impact")
    public ImpactSummaryResponse getImpact(@AuthenticationPrincipal CustomUserDetails principal) {
        return impactService.getSummaryForStudent(principal.getUser().getId());
    }

    private CoinTransactionResponse toCoinResponse(EcoCoinTransaction t) {
        return CoinTransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .referenceId(t.getReferenceId())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
