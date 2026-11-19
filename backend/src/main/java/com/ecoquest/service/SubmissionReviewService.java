package com.ecoquest.service;

import com.ecoquest.dto.teacher.SubmissionDetailResponse;
import com.ecoquest.dto.teacher.SubmissionListItemResponse;
import com.ecoquest.entity.Quest;
import com.ecoquest.entity.QuestSubmission;
import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.QuestRepository;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.UserRepository;
import com.ecoquest.util.MapUrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionReviewService {

    private final QuestSubmissionRepository submissionRepository;
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final EcoCoinService ecoCoinService;
    private final GamificationService gamificationService;
    private final ImpactService impactService;
    private final LeaderboardService leaderboardService;
    private final RealtimeLeaderboardService realtimeLeaderboardService;

    // ---------- Teacher (school-scoped) ----------

    public Page<SubmissionListItemResponse> listForTeacher(String teacherUserId, SubmissionStatus statusFilter, Pageable pageable) {
        List<String> studentIds = assignedStudentIds(teacherUserId);
        if (studentIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<QuestSubmission> page = statusFilter != null
                ? submissionRepository.findByStatusAndStudentIdIn(statusFilter, studentIds, pageable)
                : submissionRepository.findByStudentIdIn(studentIds, pageable);
        return page.map(this::toListItem);
    }

    public SubmissionDetailResponse getDetailForTeacher(String teacherUserId, String submissionId) {
        QuestSubmission submission = loadOrThrow(submissionId);
        assertTeacherScope(teacherUserId, submission);
        return toDetail(submission);
    }

    public SubmissionDetailResponse approveAsTeacher(String teacherUserId, String submissionId) {
        QuestSubmission submission = loadOrThrow(submissionId);
        assertTeacherScope(teacherUserId, submission);
        return approve(submission, teacherUserId, false);
    }

    public SubmissionDetailResponse rejectAsTeacher(String teacherUserId, String submissionId, String reason) {
        QuestSubmission submission = loadOrThrow(submissionId);
        assertTeacherScope(teacherUserId, submission);
        return reject(submission, teacherUserId, reason, false);
    }

    // ---------- Admin (unrestricted - business rule #5, override) ----------

    public Page<SubmissionListItemResponse> listForAdmin(SubmissionStatus statusFilter, Pageable pageable) {
        Page<QuestSubmission> page = statusFilter != null
                ? submissionRepository.findByStatus(statusFilter, pageable)
                : submissionRepository.findAll(pageable);
        return page.map(this::toListItem);
    }

    public SubmissionDetailResponse getDetailForAdmin(String submissionId) {
        return toDetail(loadOrThrow(submissionId));
    }

    public SubmissionDetailResponse approveAsAdmin(String adminUserId, String submissionId) {
        return approve(loadOrThrow(submissionId), adminUserId, true);
    }

    public SubmissionDetailResponse rejectAsAdmin(String adminUserId, String submissionId, String reason) {
        return reject(loadOrThrow(submissionId), adminUserId, reason, true);
    }

    // ---------- Shared approve/reject core ----------

    private SubmissionDetailResponse approve(QuestSubmission submission, String reviewerUserId, boolean isAdmin) {
        if (submission.getStatus() == SubmissionStatus.APPROVED) {
            throw ApiException.conflict("This submission has already been approved");
        }
        if (submission.getStatus() == SubmissionStatus.REJECTED) {
            throw ApiException.conflict("This submission was already rejected and cannot be approved");
        }

        Quest quest = questRepository.findById(submission.getQuestId())
                .orElseThrow(() -> ApiException.notFound("Quest not found"));

        int oldRank = leaderboardService.getStudentRank(submission.getStudentId());

        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setReviewedByUserId(reviewerUserId);
        submission.setReviewedAt(Instant.now());
        submission.setAdminOverride(isAdmin);

        // rewardGranted guard: business rules #1/#3 - a submission is only ever rewarded once,
        // exactly at the moment it transitions into APPROVED here.
        if (!submission.isRewardGranted()) {
            ecoCoinService.award(submission.getStudentId(), quest.getEcoCoinsReward(), quest.getXpReward(),
                    CoinTransactionType.QUEST, "Quest: " + quest.getTitle(), submission.getId());
            submission.setRewardGranted(true);
            gamificationService.recordActivity(submission.getStudentId());
            gamificationService.checkAndAwardBadges(submission.getStudentId());
            impactService.recordImpactForApproval(submission, quest);
            realtimeLeaderboardService.notifyIfRankChanged(submission.getStudentId(), oldRank);
        }

        // Leaderboard snapshot refresh + WebSocket push -> Phase 18/21
        // Notification("Your ... quest was approved! +N Eco-Coins") -> Phase 24

        QuestSubmission saved = submissionRepository.save(submission);
        return toDetail(saved, quest);
    }

    private SubmissionDetailResponse reject(QuestSubmission submission, String reviewerUserId, String reason, boolean isAdmin) {
        if (submission.getStatus() == SubmissionStatus.APPROVED) {
            throw ApiException.conflict("This submission was already approved and cannot be rejected");
        }
        if (submission.getStatus() == SubmissionStatus.REJECTED) {
            throw ApiException.conflict("This submission has already been rejected");
        }

        submission.setStatus(SubmissionStatus.REJECTED);
        submission.setReviewedByUserId(reviewerUserId);
        submission.setReviewedAt(Instant.now());
        submission.setRejectionReason(reason);
        submission.setAdminOverride(isAdmin);
        // rewardGranted stays false - business rule #2, rejected submissions never award coins

        // Notification("Your submission needs review" / rejection reason) -> Phase 24

        QuestSubmission saved = submissionRepository.save(submission);
        return toDetail(saved);
    }

    // ---------- Scoping helpers ----------

    private List<String> assignedStudentIds(String teacherUserId) {
        User teacher = userRepository.findById(teacherUserId)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));
        if (teacher.getRole() != Role.TEACHER || teacher.getTeacherProfile() == null) {
            throw ApiException.badRequest("User is not a teacher");
        }
        String schoolId = teacher.getTeacherProfile().getAssignedSchoolId();
        if (schoolId == null) {
            return List.of();
        }
        return userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, schoolId)
                .stream().map(User::getId).toList();
    }

    /** Business rule #4: a teacher may only review submissions from students in their own school. */
    private void assertTeacherScope(String teacherUserId, QuestSubmission submission) {
        List<String> studentIds = assignedStudentIds(teacherUserId);
        if (!studentIds.contains(submission.getStudentId())) {
            throw ApiException.forbidden("This submission is not from a student in your assigned school");
        }
    }

    private QuestSubmission loadOrThrow(String submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> ApiException.notFound("Submission not found"));
    }

    // ---------- Mapping ----------

    private SubmissionListItemResponse toListItem(QuestSubmission s) {
        String studentName = userRepository.findById(s.getStudentId()).map(User::getName).orElse("Unknown");
        String questTitle = questRepository.findById(s.getQuestId()).map(Quest::getTitle).orElse("Unknown quest");
        return SubmissionListItemResponse.builder()
                .id(s.getId())
                .questId(s.getQuestId())
                .questTitle(questTitle)
                .studentId(s.getStudentId())
                .studentName(studentName)
                .imageUrl(s.getImageUrl())
                .status(s.getStatus())
                .withinAllowedRadius(s.getSubmittedLocation() != null ? s.isWithinAllowedRadius() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }

    private SubmissionDetailResponse toDetail(QuestSubmission s) {
        Quest quest = questRepository.findById(s.getQuestId()).orElse(null);
        return toDetail(s, quest);
    }

    private SubmissionDetailResponse toDetail(QuestSubmission s, Quest quest) {
        String studentName = userRepository.findById(s.getStudentId()).map(User::getName).orElse("Unknown");

        Double subLat = s.getSubmittedLocation() != null ? s.getSubmittedLocation().getLatitude() : null;
        Double subLng = s.getSubmittedLocation() != null ? s.getSubmittedLocation().getLongitude() : null;
        Double targetLat = quest != null && quest.getTargetLocation() != null ? quest.getTargetLocation().getLatitude() : null;
        Double targetLng = quest != null && quest.getTargetLocation() != null ? quest.getTargetLocation().getLongitude() : null;

        return SubmissionDetailResponse.builder()
                .id(s.getId())
                .questId(s.getQuestId())
                .questTitle(quest != null ? quest.getTitle() : null)
                .questDescription(quest != null ? quest.getDescription() : null)
                .ecoCoinsReward(quest != null ? quest.getEcoCoinsReward() : null)
                .xpReward(quest != null ? quest.getXpReward() : null)
                .studentId(s.getStudentId())
                .studentName(studentName)
                .imageUrl(s.getImageUrl())
                .description(s.getDescription())
                .submittedLatitude(subLat)
                .submittedLongitude(subLng)
                .submissionMapUrl(MapUrlUtil.openStreetMapUrl(subLat, subLng))
                .targetMapUrl(MapUrlUtil.openStreetMapUrl(targetLat, targetLng))
                .withinAllowedRadius(s.getSubmittedLocation() != null ? s.isWithinAllowedRadius() : null)
                .distanceFromTargetMeters(s.getDistanceFromTargetMeters())
                .aiDetected(s.getAiVerification() != null ? s.getAiVerification().getDetected() : null)
                .aiConfidence(s.getAiVerification() != null ? s.getAiVerification().getConfidence() : null)
                .aiReason(s.getAiVerification() != null ? s.getAiVerification().getReason() : null)
                .aiRecommendation(s.getAiVerification() != null ? s.getAiVerification().getRecommendation() : null)
                .status(s.getStatus())
                .reviewedByUserId(s.getReviewedByUserId())
                .rejectionReason(s.getRejectionReason())
                .reviewedAt(s.getReviewedAt())
                .adminOverride(s.isAdminOverride())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
