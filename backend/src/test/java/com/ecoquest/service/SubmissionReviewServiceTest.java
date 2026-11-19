package com.ecoquest.service;

import com.ecoquest.dto.teacher.SubmissionDetailResponse;
import com.ecoquest.entity.Quest;
import com.ecoquest.entity.QuestSubmission;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.StudentProfile;
import com.ecoquest.entity.embedded.TeacherProfile;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.QuestRepository;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionReviewServiceTest {

    @Mock private QuestSubmissionRepository submissionRepository;
    @Mock private QuestRepository questRepository;
    @Mock private UserRepository userRepository;
    @Mock private EcoCoinService ecoCoinService;
    @Mock private GamificationService gamificationService;
    @Mock private ImpactService impactService;
    @Mock private LeaderboardService leaderboardService;
    @Mock private RealtimeLeaderboardService realtimeLeaderboardService;

    @InjectMocks
    private SubmissionReviewService reviewService;

    private User teacher;
    private User student;
    private Quest quest;
    private QuestSubmission submission;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .id("teacher-1").role(Role.TEACHER)
                .teacherProfile(new TeacherProfile("school-1", List.of()))
                .build();

        student = User.builder()
                .id("student-1").role(Role.STUDENT)
                .studentProfile(StudentProfile.builder().schoolId("school-1").ecoCoins(0).xp(0).build())
                .build();

        quest = Quest.builder()
                .id("quest-1").title("Tree Guardian").ecoCoinsReward(200).xpReward(150)
                .build();

        submission = QuestSubmission.builder()
                .id("submission-1").questId("quest-1").studentId("student-1")
                .status(SubmissionStatus.AI_REVIEWED).rewardGranted(false)
                .build();
    }

    @Test
    void approveAsTeacher_firstApproval_grantsRewardExactlyOnce() {
        when(submissionRepository.findById("submission-1")).thenReturn(Optional.of(submission));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, "school-1")).thenReturn(List.of(student));
        when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
        when(submissionRepository.save(any(QuestSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionDetailResponse response = reviewService.approveAsTeacher("teacher-1", "submission-1");

        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
        verify(ecoCoinService).award("student-1", 200, 150, CoinTransactionType.QUEST, "Quest: Tree Guardian", "submission-1");
        verify(gamificationService).recordActivity("student-1");
        verify(gamificationService).checkAndAwardBadges("student-1");
        verify(impactService).recordImpactForApproval(eq(submission), eq(quest));
    }

    @Test
    void approveAsTeacher_alreadyApproved_throwsConflictAndDoesNotDoubleAward() {
        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setRewardGranted(true);

        when(submissionRepository.findById("submission-1")).thenReturn(Optional.of(submission));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, "school-1")).thenReturn(List.of(student));

        assertThatThrownBy(() -> reviewService.approveAsTeacher("teacher-1", "submission-1"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already been approved");

        verify(ecoCoinService, never()).award(any(), anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    void approveAsTeacher_rejectedSubmission_cannotBeApproved() {
        submission.setStatus(SubmissionStatus.REJECTED);

        when(submissionRepository.findById("submission-1")).thenReturn(Optional.of(submission));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, "school-1")).thenReturn(List.of(student));

        assertThatThrownBy(() -> reviewService.approveAsTeacher("teacher-1", "submission-1"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already rejected");
    }

    @Test
    void approveAsTeacher_studentNotInTeachersSchool_throwsForbidden() {
        submission.setStudentId("student-2");

        when(submissionRepository.findById("submission-1")).thenReturn(Optional.of(submission));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        // teacher's own school (school-1) has no matching student for student-2
        when(userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, "school-1")).thenReturn(List.of(student));

        assertThatThrownBy(() -> reviewService.approveAsTeacher("teacher-1", "submission-1"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not from a student in your assigned school");

        verify(ecoCoinService, never()).award(any(), anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    void rejectAsTeacher_requiresReasonAndDoesNotAwardReward() {
        when(submissionRepository.findById("submission-1")).thenReturn(Optional.of(submission));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, "school-1")).thenReturn(List.of(student));
        when(submissionRepository.save(any(QuestSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionDetailResponse response = reviewService.rejectAsTeacher("teacher-1", "submission-1",
                "Uploaded image does not provide sufficient evidence.");

        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
        assertThat(response.getRejectionReason()).isEqualTo("Uploaded image does not provide sufficient evidence.");
        verify(ecoCoinService, never()).award(any(), anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    void approveAsAdmin_bypassesSchoolScoping() {
        when(submissionRepository.findById("submission-1")).thenReturn(Optional.of(submission));
        when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
        when(submissionRepository.save(any(QuestSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionDetailResponse response = reviewService.approveAsAdmin("admin-1", "submission-1");

        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
        assertThat(response.isAdminOverride()).isTrue();
        // admin path never needs to resolve teacher scope - userRepository.findById for a teacher is never called
        verify(userRepository, never()).findById("admin-1");
    }
}
