package com.ecoquest.service;

import com.ecoquest.ai.AIVerificationService;
import com.ecoquest.dto.quest.QuestSubmissionResponse;
import com.ecoquest.entity.Quest;
import com.ecoquest.entity.QuestSubmission;
import com.ecoquest.entity.embedded.GeoLocation;
import com.ecoquest.entity.embedded.VerificationResult;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.storage.StorageService;
import com.ecoquest.util.GeoUtils;
import com.ecoquest.util.MapUrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestSubmissionService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB - mirrors MAX_UPLOAD_SIZE default

    private final QuestSubmissionRepository submissionRepository;
    private final QuestService questService;
    private final StorageService storageService;
    private final AIVerificationService aiVerificationService;

    public QuestSubmissionResponse submitProof(String studentId, String questId,
                                                org.springframework.web.multipart.MultipartFile image,
                                                String description, Double latitude, Double longitude) {

        Quest quest = questService.getQuestEntityOrThrow(questId);

        long mySubmissionCount = submissionRepository.findByStudentIdAndQuestId(studentId, questId).size();
        String blockReason = questService.checkEligibility(quest, mySubmissionCount);
        if (blockReason != null) {
            throw ApiException.badRequest(blockReason);
        }

        validateProof(quest, image, latitude, longitude);

        String imageUrl = null;
        VerificationResult aiVerification = null;

        if (image != null && !image.isEmpty()) {
            byte[] imageBytes;
            try {
                imageBytes = image.getBytes();
                imageUrl = storageService.store(image, "quest-submissions");
            } catch (IOException e) {
                throw new RuntimeException("Failed to store uploaded file", e);
            }

            // Advisory only (spec #13/#16) - never itself sets APPROVED/REJECTED or awards
            // anything; just gives the teacher a head start. See AIVerificationService.
            try {
                aiVerification = aiVerificationService.verifyEvidence(imageBytes, image.getContentType(), quest, description);
            } catch (Exception e) {
                aiVerification = null; // AI review failed entirely - submission still proceeds, just stays PENDING
            }
        }

        GeoLocation submittedLocation = (latitude != null && longitude != null)
                ? new GeoLocation(latitude, longitude) : null;

        boolean withinRadius = true;
        Double distanceMeters = null;
        if (quest.isGpsRequired() && submittedLocation != null && quest.getTargetLocation() != null) {
            int radius = quest.getAllowedRadiusMeters() != null ? quest.getAllowedRadiusMeters() : 500;
            distanceMeters = GeoUtils.distanceMeters(quest.getTargetLocation().getLatitude(), quest.getTargetLocation().getLongitude(),
                    latitude, longitude);
            withinRadius = distanceMeters <= radius;
        }

        // Spec #14: outside-radius is a WARNING by default, never an auto-reject - UNLESS the
        // quest is explicitly configured to auto-reject (Quest.autoRejectOutsideRadius, Phase 17).
        boolean autoRejected = quest.isGpsRequired() && !withinRadius && quest.isAutoRejectOutsideRadius();

        QuestSubmission submission = QuestSubmission.builder()
                .questId(questId)
                .studentId(studentId)
                .imageUrl(imageUrl)
                .description(description)
                .submittedLocation(submittedLocation)
                .withinAllowedRadius(withinRadius)
                .distanceFromTargetMeters(distanceMeters)
                .aiVerification(aiVerification)
                .status(autoRejected ? SubmissionStatus.REJECTED
                        : (aiVerification != null ? SubmissionStatus.AI_REVIEWED : SubmissionStatus.PENDING))
                .rejectionReason(autoRejected
                        ? "Automatically rejected: submitted location is outside the allowed radius for this quest."
                        : null)
                .reviewedAt(autoRejected ? Instant.now() : null)
                .rewardGranted(false)
                .adminOverride(false)
                .createdAt(Instant.now())
                .build();

        QuestSubmission saved = submissionRepository.save(submission);
        return toResponse(saved, quest.getTitle());
    }

    public Page<QuestSubmissionResponse> listMySubmissions(String studentId, Pageable pageable) {
        // NOTE: quest titles aren't denormalized onto the submission, so a full page-with-title
        // view here means one Quest lookup per row; acceptable at prototype scale. Phase 11's
        // teacher review screen does the same for the same reason (fields are read together).
        return submissionRepository.findByStudentId(studentId, pageable)
                .map(s -> toResponse(s, null));
    }

    public QuestSubmissionResponse getMySubmission(String studentId, String submissionId) {
        QuestSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> ApiException.notFound("Submission not found"));
        if (!studentId.equals(submission.getStudentId())) {
            throw ApiException.forbidden("This is not your submission");
        }
        return toResponse(submission, null);
    }

    private void validateProof(Quest quest, org.springframework.web.multipart.MultipartFile image,
                                Double latitude, Double longitude) {
        if (quest.isProofRequired() && (image == null || image.isEmpty())) {
            throw ApiException.badRequest("This quest requires a photo as proof");
        }

        if (image != null && !image.isEmpty()) {
            if (image.getSize() > MAX_FILE_SIZE_BYTES) {
                throw ApiException.badRequest("Uploaded file is too large (max 5MB)");
            }
            String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw ApiException.badRequest("Only JPEG, PNG, or WEBP images are allowed");
            }
        }

        if (quest.isGpsRequired() && (latitude == null || longitude == null)) {
            throw ApiException.badRequest("This quest requires your location");
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw ApiException.badRequest("Invalid latitude");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw ApiException.badRequest("Invalid longitude");
        }
    }

    private QuestSubmissionResponse toResponse(QuestSubmission s, String questTitle) {
        Double lat = s.getSubmittedLocation() != null ? s.getSubmittedLocation().getLatitude() : null;
        Double lng = s.getSubmittedLocation() != null ? s.getSubmittedLocation().getLongitude() : null;

        return QuestSubmissionResponse.builder()
                .id(s.getId())
                .questId(s.getQuestId())
                .questTitle(questTitle)
                .imageUrl(s.getImageUrl())
                .description(s.getDescription())
                .submittedLatitude(lat)
                .submittedLongitude(lng)
                .mapUrl(MapUrlUtil.openStreetMapUrl(lat, lng))
                .withinAllowedRadius(s.getSubmittedLocation() != null ? s.isWithinAllowedRadius() : null)
                .distanceFromTargetMeters(s.getDistanceFromTargetMeters())
                .status(s.getStatus())
                .rejectionReason(s.getRejectionReason())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
