package com.ecoquest.service;

import com.ecoquest.entity.Badge;
import com.ecoquest.entity.EcoCoinTransaction;
import com.ecoquest.entity.Quest;
import com.ecoquest.entity.QuestSubmission;
import com.ecoquest.entity.StudentBadge;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.Streak;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.entity.enums.QuestCategory;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.repository.BadgeRepository;
import com.ecoquest.repository.EcoCoinTransactionRepository;
import com.ecoquest.repository.QuestRepository;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.QuizAttemptRepository;
import com.ecoquest.repository.StudentBadgeRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Called after any reward-granting activity (QuizService.submitAttempt on first reward,
 * SubmissionReviewService.approve) - fills in the hooks those phases deliberately left as
 * comments. Two responsibilities: keep the student's streak counter current, and check whether
 * any badge's criteria are now met.
 */
@Service
@RequiredArgsConstructor
public class GamificationService {

    private static final Map<Integer, Integer> STREAK_MILESTONE_BONUS_COINS = Map.of(
            3, 30,
            7, 100,
            30, 500
    );

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final StudentBadgeRepository studentBadgeRepository;
    private final EcoCoinTransactionRepository transactionRepository;
    private final QuestSubmissionRepository questSubmissionRepository;
    private final QuestRepository questRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final EcoCoinService ecoCoinService;

    /**
     * Updates the student's streak based on today being consecutive with their last qualifying
     * activity. Same-day repeats are no-ops. Awards a one-time bonus at 3/7/30-day milestones.
     */
    public void recordActivity(String studentId) {
        User user = userRepository.findById(studentId).orElse(null);
        if (user == null || user.getStudentProfile() == null) {
            return;
        }

        Streak streak = user.getStudentProfile().getStreak();
        if (streak == null) {
            streak = new Streak(0, 0, null);
        }

        LocalDate today = LocalDate.now();
        if (today.equals(streak.getLastActivityDate())) {
            return; // already counted today
        }

        boolean isConsecutive = streak.getLastActivityDate() != null
                && streak.getLastActivityDate().plusDays(1).equals(today);
        int newCurrent = isConsecutive ? (streak.getCurrentStreak() == null ? 1 : streak.getCurrentStreak() + 1) : 1;

        streak.setCurrentStreak(newCurrent);
        streak.setLongestStreak(Math.max(streak.getLongestStreak() == null ? 0 : streak.getLongestStreak(), newCurrent));
        streak.setLastActivityDate(today);

        user.getStudentProfile().setStreak(streak);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        Integer bonus = STREAK_MILESTONE_BONUS_COINS.get(newCurrent);
        if (bonus != null) {
            ecoCoinService.award(studentId, bonus, 0, CoinTransactionType.STREAK,
                    newCurrent + "-day streak bonus", "streak:" + newCurrent);
        }
    }

    /**
     * Evaluates every badge's criteria against the student's current stats and awards any not
     * already earned. Runs a second pass in case a badge's own coin reward pushes the student
     * over another badge's ECO_COINS_EARNED_TOTAL threshold within the same call.
     */
    public void checkAndAwardBadges(String studentId) {
        for (int pass = 0; pass < 2; pass++) {
            boolean awardedAny = evaluateBadgesOnce(studentId);
            if (!awardedAny) {
                break;
            }
        }
    }

    private boolean evaluateBadgesOnce(String studentId) {
        List<Badge> allBadges = badgeRepository.findAll();
        Set<String> alreadyEarnedBadgeIds = studentBadgeRepository.findByStudentId(studentId).stream()
                .map(StudentBadge::getBadgeId).collect(Collectors.toSet());

        boolean awardedAny = false;

        for (Badge badge : allBadges) {
            if (alreadyEarnedBadgeIds.contains(badge.getId())) {
                continue;
            }
            if (meetsCriteria(studentId, badge)) {
                studentBadgeRepository.save(StudentBadge.builder()
                        .studentId(studentId)
                        .badgeId(badge.getId())
                        .earnedAt(Instant.now())
                        .build());

                if ((badge.getEcoCoinsReward() != null && badge.getEcoCoinsReward() != 0)
                        || (badge.getXpReward() != null && badge.getXpReward() != 0)) {
                    ecoCoinService.award(studentId,
                            badge.getEcoCoinsReward() == null ? 0 : badge.getEcoCoinsReward(),
                            badge.getXpReward() == null ? 0 : badge.getXpReward(),
                            CoinTransactionType.BADGE,
                            "Badge unlocked: " + badge.getName(),
                            badge.getId());
                }
                awardedAny = true;
            }
        }
        return awardedAny;
    }

    private boolean meetsCriteria(String studentId, Badge badge) {
        int threshold = badge.getCriteriaThreshold() == null ? Integer.MAX_VALUE : badge.getCriteriaThreshold();

        return switch (badge.getCriteriaType()) {
            case QUESTS_COMPLETED_TOTAL ->
                    questSubmissionRepository.countByStudentIdAndStatus(studentId, SubmissionStatus.APPROVED) >= threshold;

            case QUESTS_COMPLETED_IN_CATEGORY -> countApprovedInCategory(studentId, badge.getCriteriaCategory()) >= threshold;

            case STREAK_DAYS -> {
                User user = userRepository.findById(studentId).orElse(null);
                Integer current = (user != null && user.getStudentProfile() != null && user.getStudentProfile().getStreak() != null)
                        ? user.getStudentProfile().getStreak().getCurrentStreak() : 0;
                yield current != null && current >= threshold;
            }

            case ECO_COINS_EARNED_TOTAL -> lifetimeEarnedCoins(studentId) >= threshold;

            case QUIZZES_COMPLETED_TOTAL -> quizAttemptRepository.countByStudentIdAndRewardedTrue(studentId) >= threshold;
        };
    }

    private long countApprovedInCategory(String studentId, QuestCategory category) {
        if (category == null) {
            return 0;
        }
        List<QuestSubmission> approved = questSubmissionRepository.findByStudentIdAndStatus(studentId, SubmissionStatus.APPROVED);
        if (approved.isEmpty()) {
            return 0;
        }
        List<String> questIds = approved.stream().map(QuestSubmission::getQuestId).distinct().toList();
        Map<String, QuestCategory> categoryByQuestId = questRepository.findAllById(questIds).stream()
                .collect(Collectors.toMap(Quest::getId, Quest::getCategory));

        return approved.stream()
                .filter(s -> category.equals(categoryByQuestId.get(s.getQuestId())))
                .count();
    }

    /** Sum of all positive earned amounts - excludes ADMIN_ADJUSTMENT, which is a correction, not something "earned". */
    private long lifetimeEarnedCoins(String studentId) {
        return transactionRepository.findByStudentId(studentId).stream()
                .filter(t -> t.getType() != CoinTransactionType.ADMIN_ADJUSTMENT)
                .mapToLong(EcoCoinTransaction::getAmount)
                .filter(a -> a > 0)
                .sum();
    }
}
