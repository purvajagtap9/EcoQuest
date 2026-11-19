package com.ecoquest.controller;

import com.ecoquest.entity.enums.LeaderboardPeriod;
import com.ecoquest.service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * scope=individual|school|city|state. city/state are location roll-ups over school totals
 * (see LeaderboardService.getLocationLeaderboard) - "national" is simply the unfiltered
 * individual or school leaderboard, so there's no separate scope value for it.
 */
@RestController
@RequestMapping("/api/admin/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Leaderboard", description = "Individual, school, city, and state leaderboards")
public class AdminLeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public Object getLeaderboard(@RequestParam(defaultValue = "individual") String scope,
                                  @RequestParam(defaultValue = "ALL_TIME") LeaderboardPeriod period,
                                  Pageable pageable) {
        return switch (scope.toLowerCase()) {
            case "school" -> leaderboardService.getSchoolLeaderboard(period, pageable);
            case "city" -> leaderboardService.getLocationLeaderboard("city", period, pageable);
            case "state" -> leaderboardService.getLocationLeaderboard("state", period, pageable);
            default -> leaderboardService.getIndividualLeaderboard(period, pageable);
        };
    }
}
