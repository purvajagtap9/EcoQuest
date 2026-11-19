package com.ecoquest.service;

import com.ecoquest.entity.Badge;
import com.ecoquest.entity.StudentBadge;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.StudentProfile;
import com.ecoquest.entity.embedded.Streak;
import com.ecoquest.entity.enums.BadgeCriteriaType;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BadgeRepository badgeRepository;
    @Mock private StudentBadgeRepository studentBadgeRepository;
    @Mock private EcoCoinTransactionRepository transactionRepository;
    @Mock private QuestSubmissionRepository questSubmissionRepository;
    @Mock private QuestRepository questRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private EcoCoinService ecoCoinService;

    @InjectMocks
    private GamificationService gamificationService;

    private User student;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id("student-1")
                .studentProfile(StudentProfile.builder().ecoCoins(0).xp(0).streak(new Streak(0, 0, null)).build())
                .build();
    }

    @Test
    void recordActivity_firstEverActivity_setsStreakToOne() {
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));

        gamificationService.recordActivity("student-1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getStudentProfile().getStreak().getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void recordActivity_consecutiveDay_incrementsStreak() {
        student.getStudentProfile().setStreak(new Streak(4, 4, LocalDate.now().minusDays(1)));
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));

        gamificationService.recordActivity("student-1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        Streak streak = captor.getValue().getStudentProfile().getStreak();
        assertThat(streak.getCurrentStreak()).isEqualTo(5);
        assertThat(streak.getLongestStreak()).isEqualTo(5);
    }

    @Test
    void recordActivity_gapOfMoreThanOneDay_resetsStreakToOne() {
        student.getStudentProfile().setStreak(new Streak(10, 10, LocalDate.now().minusDays(3)));
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));

        gamificationService.recordActivity("student-1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        Streak streak = captor.getValue().getStudentProfile().getStreak();
        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.getLongestStreak()).isEqualTo(10); // longest is preserved, not reset
    }

    @Test
    void recordActivity_sameDayTwice_isNoOp() {
        student.getStudentProfile().setStreak(new Streak(3, 3, LocalDate.now()));
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));

        gamificationService.recordActivity("student-1");

        verify(userRepository, never()).save(any());
    }

    @Test
    void recordActivity_reachingSevenDayMilestone_awardsBonusCoins() {
        student.getStudentProfile().setStreak(new Streak(6, 6, LocalDate.now().minusDays(1)));
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));

        gamificationService.recordActivity("student-1");

        verify(ecoCoinService).award(eq("student-1"), eq(100), eq(0), eq(CoinTransactionType.STREAK), any(), eq("streak:7"));
    }

    @Test
    void checkAndAwardBadges_meetsQuestCountThreshold_awardsBadgeOnce() {
        Badge badge = Badge.builder()
                .id("badge-1").name("First Step").criteriaType(BadgeCriteriaType.QUESTS_COMPLETED_TOTAL)
                .criteriaThreshold(1).ecoCoinsReward(25).xpReward(20)
                .build();

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(studentBadgeRepository.findByStudentId("student-1")).thenReturn(List.of()); // not yet earned
        when(questSubmissionRepository.countByStudentIdAndStatus("student-1", SubmissionStatus.APPROVED)).thenReturn(1L);

        gamificationService.checkAndAwardBadges("student-1");

        ArgumentCaptor<StudentBadge> captor = ArgumentCaptor.forClass(StudentBadge.class);
        verify(studentBadgeRepository).save(captor.capture());
        assertThat(captor.getValue().getBadgeId()).isEqualTo("badge-1");
        verify(ecoCoinService).award("student-1", 25, 20, CoinTransactionType.BADGE, "Badge unlocked: First Step", "badge-1");
    }

    @Test
    void checkAndAwardBadges_alreadyEarned_isNotAwardedAgain() {
        Badge badge = Badge.builder()
                .id("badge-1").name("First Step").criteriaType(BadgeCriteriaType.QUESTS_COMPLETED_TOTAL)
                .criteriaThreshold(1).build();

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(studentBadgeRepository.findByStudentId("student-1"))
                .thenReturn(List.of(StudentBadge.builder().studentId("student-1").badgeId("badge-1").build()));

        gamificationService.checkAndAwardBadges("student-1");

        verify(studentBadgeRepository, never()).save(any());
        verify(ecoCoinService, never()).award(any(), anyInt(), anyInt(), any(), any(), any());
    }
}
