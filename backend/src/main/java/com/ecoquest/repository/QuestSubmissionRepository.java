package com.ecoquest.repository;

import com.ecoquest.entity.QuestSubmission;
import com.ecoquest.entity.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface QuestSubmissionRepository extends MongoRepository<QuestSubmission, String> {

    Page<QuestSubmission> findByStudentId(String studentId, Pageable pageable);

    Page<QuestSubmission> findByStudentIdIn(List<String> studentIds, Pageable pageable);

    List<QuestSubmission> findByStudentIdAndQuestId(String studentId, String questId);

    long countByStudentIdAndStatus(String studentId, SubmissionStatus status);

    List<QuestSubmission> findByStudentIdAndStatus(String studentId, SubmissionStatus status);

    long countByStatus(SubmissionStatus status);

    Page<QuestSubmission> findByStatus(SubmissionStatus status, Pageable pageable);

    // Used by TeacherService (Phase 5) to scope pending reviews to a teacher's school later;
    // kept here now since it belongs with the rest of the submission queries.
    Page<QuestSubmission> findByStatusAndStudentIdIn(SubmissionStatus status, List<String> studentIds, Pageable pageable);

    long countByStatusAndStudentIdIn(SubmissionStatus status, List<String> studentIds);

    long countByStatusAndStudentIdInAndReviewedAtBetween(
            SubmissionStatus status, List<String> studentIds, Instant from, Instant to);
}
