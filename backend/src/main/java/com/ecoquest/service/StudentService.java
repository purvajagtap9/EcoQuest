package com.ecoquest.service;

import com.ecoquest.dto.student.StudentDashboardResponse;
import com.ecoquest.dto.student.StudentProfileResponse;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.StudentProfile;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.StudentBadgeRepository;
import com.ecoquest.repository.UserRepository;
import com.ecoquest.util.LevelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;
    private final QuestSubmissionRepository questSubmissionRepository;
    private final StudentBadgeRepository studentBadgeRepository;

    private User loadStudent(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        if (user.getRole() != Role.STUDENT || user.getStudentProfile() == null) {
            throw ApiException.badRequest("User is not a student");
        }
        return user;
    }

    public StudentProfileResponse getProfile(String userId) {
        User user = loadStudent(userId);
        StudentProfile sp = user.getStudentProfile();

        return StudentProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .schoolId(sp.getSchoolId())
                .grade(sp.getGrade())
                .preferredLanguage(sp.getPreferredLanguage())
                .displayName(sp.getDisplayName())
                .showRealNameOnLeaderboard(Boolean.TRUE.equals(sp.getShowRealNameOnLeaderboard()))
                .build();
    }

    public StudentDashboardResponse getDashboard(String userId) {
        User user = loadStudent(userId);
        StudentProfile sp = user.getStudentProfile();

        LevelUtil.LevelInfo levelInfo = LevelUtil.forXp(sp.getXp() == null ? 0 : sp.getXp());

        long completedQuests = questSubmissionRepository.countByStudentIdAndStatus(userId, SubmissionStatus.APPROVED);
        long pendingPending = questSubmissionRepository.countByStudentIdAndStatus(userId, SubmissionStatus.PENDING);
        long pendingAiReviewed = questSubmissionRepository.countByStudentIdAndStatus(userId, SubmissionStatus.AI_REVIEWED);

        long badgeCount = studentBadgeRepository.countByStudentId(userId);

        int ecoCoins = sp.getEcoCoins() == null ? 0 : sp.getEcoCoins();
        int rank = (int) userRepository.countByRoleAndStudentProfile_EcoCoinsGreaterThan(Role.STUDENT, ecoCoins) + 1;

        return StudentDashboardResponse.builder()
                .name(user.getName())
                .ecoCoins(ecoCoins)
                .level(levelInfo.level())
                .levelTitle(levelInfo.title())
                .xp(sp.getXp() == null ? 0 : sp.getXp())
                .xpForNextLevel(levelInfo.xpForNextLevel())
                .currentStreak(sp.getStreak() != null ? sp.getStreak().getCurrentStreak() : 0)
                .longestStreak(sp.getStreak() != null ? sp.getStreak().getLongestStreak() : 0)
                .completedQuests(completedQuests)
                .pendingVerification(pendingPending + pendingAiReviewed)
                .badgeCount((int) badgeCount)
                .rank(rank)
                .build();
    }
}
