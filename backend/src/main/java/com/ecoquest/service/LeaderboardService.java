package com.ecoquest.service;

import com.ecoquest.dto.leaderboard.LeaderboardEntryResponse;
import com.ecoquest.dto.leaderboard.LocationLeaderboardEntryResponse;
import com.ecoquest.dto.leaderboard.SchoolLeaderboardEntryResponse;
import com.ecoquest.entity.School;
import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.entity.enums.LeaderboardPeriod;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.repository.SchoolRepository;
import com.ecoquest.repository.StudentBadgeRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes leaderboards LIVE from ledger data (business rule #8) - no cache yet (that's the
 * LeaderboardSnapshot / WebSocket-diffing optimization in Phase 18/21).
 *
 * Period semantics:
 * - ALL_TIME uses each student's current cached balance (includes any ADMIN_ADJUSTMENT).
 * - WEEKLY/MONTHLY sum EcoCoinTransaction.amount within a rolling window, EXCLUDING
 *   ADMIN_ADJUSTMENT (a correction isn't something "earned" in that window).
 * - xp and badgeCount shown on every row are always cumulative totals - there is no per-event
 *   XP ledger to compute a period-scoped XP figure from.
 */
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final StudentBadgeRepository studentBadgeRepository;
    private final MongoTemplate mongoTemplate;

    // ---------- Individual ----------

    public Page<LeaderboardEntryResponse> getIndividualLeaderboard(LeaderboardPeriod period, Pageable pageable) {
        List<LeaderboardEntryResponse> fullRanking = buildIndividualRanking(period, null);
        return paginate(fullRanking, pageable);
    }

    /** Same ranking, filtered to one school - used by the teacher's class leaderboard. */
    public Page<LeaderboardEntryResponse> getIndividualLeaderboardForSchool(String schoolId, LeaderboardPeriod period, Pageable pageable) {
        List<LeaderboardEntryResponse> fullRanking = buildIndividualRanking(period, schoolId);
        return paginate(fullRanking, pageable);
    }

    private List<LeaderboardEntryResponse> buildIndividualRanking(LeaderboardPeriod period, String schoolIdFilter) {
        Map<String, Integer> coinsByStudent = coinsByStudentForPeriod(period);
        List<User> students = userRepository.findByRole(Role.STUDENT);

        List<User> filtered = schoolIdFilter == null
                ? students
                : students.stream()
                    .filter(u -> u.getStudentProfile() != null && schoolIdFilter.equals(u.getStudentProfile().getSchoolId()))
                    .toList();

        List<LeaderboardEntryResponse> ranked = filtered.stream()
                .map(u -> {
                    int coins = period == LeaderboardPeriod.ALL_TIME
                            ? (u.getStudentProfile().getEcoCoins() == null ? 0 : u.getStudentProfile().getEcoCoins())
                            : coinsByStudent.getOrDefault(u.getId(), 0);
                    return LeaderboardEntryResponse.builder()
                            .studentId(u.getId())
                            .displayName(publicDisplayName(u))
                            .ecoCoins(coins)
                            .xp(u.getStudentProfile().getXp() == null ? 0 : u.getStudentProfile().getXp())
                            .badgeCount((int) studentBadgeRepository.countByStudentId(u.getId()))
                            .schoolId(u.getStudentProfile().getSchoolId())
                            .build();
                })
                .sorted(Comparator.comparingInt(LeaderboardEntryResponse::getEcoCoins).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }
        return ranked;
    }

    private String publicDisplayName(User u) {
        var sp = u.getStudentProfile();
        if (Boolean.TRUE.equals(sp.getShowRealNameOnLeaderboard())) {
            return u.getName();
        }
        if (sp.getDisplayName() != null && !sp.getDisplayName().isBlank() && !sp.getDisplayName().equals(u.getName())) {
            return sp.getDisplayName();
        }
        String last4 = u.getId().length() > 4 ? u.getId().substring(u.getId().length() - 4) : u.getId();
        return "Student " + last4;
    }

    /** Fast single-student rank (ALL_TIME, by current balance) - used for the WebSocket push, not the full list endpoints above. */
    public int getStudentRank(String studentId) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null || student.getStudentProfile() == null) {
            return -1;
        }
        int coins = student.getStudentProfile().getEcoCoins() == null ? 0 : student.getStudentProfile().getEcoCoins();
        return (int) userRepository.countByRoleAndStudentProfile_EcoCoinsGreaterThan(Role.STUDENT, coins) + 1;
    }

    // ---------- School ----------

    public Page<SchoolLeaderboardEntryResponse> getSchoolLeaderboard(LeaderboardPeriod period, Pageable pageable) {
        Map<String, int[]> schoolAgg = aggregateBySchool(period); // schoolId -> [totalCoins, studentCount]

        List<School> schools = schoolRepository.findAllById(schoolAgg.keySet());
        Map<String, School> schoolById = schools.stream().collect(Collectors.toMap(School::getId, s -> s));

        List<SchoolLeaderboardEntryResponse> ranked = schoolAgg.entrySet().stream()
                .map(e -> {
                    School school = schoolById.get(e.getKey());
                    return SchoolLeaderboardEntryResponse.builder()
                            .schoolId(e.getKey())
                            .schoolName(school != null ? school.getName() : "Unknown school")
                            .city(school != null ? school.getCity() : null)
                            .state(school != null ? school.getState() : null)
                            .totalEcoCoins(e.getValue()[0])
                            .studentCount(e.getValue()[1])
                            .build();
                })
                .sorted(Comparator.comparingInt(SchoolLeaderboardEntryResponse::getTotalEcoCoins).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }
        return paginate(ranked, pageable);
    }

    // ---------- Location (city or state) ----------

    public Page<LocationLeaderboardEntryResponse> getLocationLeaderboard(String groupBy, LeaderboardPeriod period, Pageable pageable) {
        boolean byState = "state".equalsIgnoreCase(groupBy);
        Map<String, int[]> schoolAgg = aggregateBySchool(period);
        List<School> schools = schoolRepository.findAllById(schoolAgg.keySet());

        Map<String, int[]> locationAgg = new HashMap<>(); // locationName -> [totalCoins, schoolCount]
        for (School school : schools) {
            String location = byState ? school.getState() : school.getCity();
            if (location == null) continue;
            int[] schoolTotals = schoolAgg.get(school.getId());
            if (schoolTotals == null) continue;
            locationAgg.computeIfAbsent(location, k -> new int[2]);
            locationAgg.get(location)[0] += schoolTotals[0];
            locationAgg.get(location)[1] += 1;
        }

        List<LocationLeaderboardEntryResponse> ranked = locationAgg.entrySet().stream()
                .map(e -> LocationLeaderboardEntryResponse.builder()
                        .locationName(e.getKey())
                        .totalEcoCoins(e.getValue()[0])
                        .schoolCount(e.getValue()[1])
                        .build())
                .sorted(Comparator.comparingInt(LocationLeaderboardEntryResponse::getTotalEcoCoins).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }
        return paginate(ranked, pageable);
    }

    // ---------- Shared computation ----------

    private Map<String, int[]> aggregateBySchool(LeaderboardPeriod period) {
        Map<String, Integer> coinsByStudent = coinsByStudentForPeriod(period);
        List<User> students = userRepository.findByRole(Role.STUDENT);

        Map<String, int[]> schoolAgg = new HashMap<>();
        for (User u : students) {
            if (u.getStudentProfile() == null || u.getStudentProfile().getSchoolId() == null) {
                continue;
            }
            String schoolId = u.getStudentProfile().getSchoolId();
            int coins = period == LeaderboardPeriod.ALL_TIME
                    ? (u.getStudentProfile().getEcoCoins() == null ? 0 : u.getStudentProfile().getEcoCoins())
                    : coinsByStudent.getOrDefault(u.getId(), 0);
            schoolAgg.computeIfAbsent(schoolId, k -> new int[2]);
            schoolAgg.get(schoolId)[0] += coins;
            schoolAgg.get(schoolId)[1] += 1;
        }
        return schoolAgg;
    }

    private Map<String, Integer> coinsByStudentForPeriod(LeaderboardPeriod period) {
        if (period == LeaderboardPeriod.ALL_TIME) {
            return Map.of(); // callers use the cached balance directly for ALL_TIME
        }

        Instant periodStart = Instant.now().minus(period == LeaderboardPeriod.WEEKLY ? 7 : 30, ChronoUnit.DAYS);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(periodStart)
                        .and("type").ne(CoinTransactionType.ADMIN_ADJUSTMENT)),
                Aggregation.group("studentId").sum("amount").as("total")
        );

        List<StudentPeriodTotal> results = mongoTemplate.aggregate(
                aggregation, "eco_coin_transactions", StudentPeriodTotal.class).getMappedResults();

        return results.stream().collect(Collectors.toMap(StudentPeriodTotal::getId,
                t -> t.getTotal() == null ? 0 : t.getTotal()));
    }

    private <T> Page<T> paginate(List<T> fullList, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= fullList.size()) {
            return new PageImpl<>(List.of(), pageable, fullList.size());
        }
        int end = Math.min(start + pageable.getPageSize(), fullList.size());
        return new PageImpl<>(fullList.subList(start, end), pageable, fullList.size());
    }
}
