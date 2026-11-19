package com.ecoquest.service;

import com.ecoquest.dto.quest.QuestRequest;
import com.ecoquest.dto.quest.QuestResponse;
import com.ecoquest.dto.student.StudentQuestDetailResponse;
import com.ecoquest.entity.Quest;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.GeoLocation;
import com.ecoquest.entity.enums.QuestStatus;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.exception.ApiException;
import com.ecoquest.mapper.QuestMapper;
import com.ecoquest.repository.QuestRepository;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final QuestSubmissionRepository questSubmissionRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${app.quest.default-geo-radius-meters}")
    private int defaultRadiusMeters;

    // ---------- Student ----------

    /**
     * A quest is visible to a student when: status=ACTIVE, and either it has no schoolId
     * (available to all schools) or its schoolId matches the student's own school, and today
     * falls within [startDate, endDate] wherever those are set (null = no bound on that side).
     */
    public List<QuestResponse> listVisibleQuestsForStudent(String studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        String schoolId = student.getStudentProfile() != null ? student.getStudentProfile().getSchoolId() : null;

        LocalDate today = LocalDate.now();

        Criteria criteria = Criteria.where("status").is(QuestStatus.ACTIVE)
                .orOperator(
                        Criteria.where("schoolId").isNull(),
                        Criteria.where("schoolId").is(schoolId)
                )
                .andOperator(
                        new Criteria().orOperator(Criteria.where("startDate").isNull(), Criteria.where("startDate").lte(today)),
                        new Criteria().orOperator(Criteria.where("endDate").isNull(), Criteria.where("endDate").gte(today))
                );

        return mongoTemplate.find(Query.query(criteria), Quest.class).stream()
                .map(QuestMapper::toResponse)
                .toList();
    }

    public StudentQuestDetailResponse getQuestDetailForStudent(String studentId, String questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> ApiException.notFound("Quest not found"));

        long mySubmissionCount = questSubmissionRepository.findByStudentIdAndQuestId(studentId, questId).size();
        String blockReason = checkEligibility(quest, mySubmissionCount);

        return StudentQuestDetailResponse.builder()
                .quest(QuestMapper.toResponse(quest))
                .mySubmissionCount((int) mySubmissionCount)
                .canSubmit(blockReason == null)
                .blockReason(blockReason)
                .build();
    }

    /**
     * Shared eligibility logic - used both to report a non-fatal blockReason in the detail view
     * (getQuestDetailForStudent) and to actually enforce submission rules server-side
     * (QuestSubmissionService.submitProof calls this and throws if non-null).
     * Returns null when the student CAN submit, otherwise a human-readable reason they can't.
     */
    public String checkEligibility(Quest quest, long mySubmissionCount) {
        if (quest.getStatus() != QuestStatus.ACTIVE) {
            return "Quest is not currently active";
        }
        if (quest.getStartDate() != null && LocalDate.now().isBefore(quest.getStartDate())) {
            return "Quest has not started yet";
        }
        if (quest.getEndDate() != null && LocalDate.now().isAfter(quest.getEndDate())) {
            return "Quest has ended";
        }
        if (mySubmissionCount >= quest.getMaxSubmissionsPerStudent()) {
            return "Maximum submissions reached for this quest";
        }
        return null;
    }

    public Quest getQuestEntityOrThrow(String questId) {
        return findQuestOrThrow(questId);
    }

    // ---------- Admin ----------

    public Page<QuestResponse> listAllQuestsAdmin(Pageable pageable) {
        return questRepository.findAll(pageable).map(QuestMapper::toResponse);
    }

    public QuestResponse createQuestAsAdmin(QuestRequest request, String adminUserId) {
        Quest quest = buildQuestFromRequest(request, adminUserId, request.getSchoolId());
        return QuestMapper.toResponse(questRepository.save(quest));
    }

    public QuestResponse updateQuestAsAdmin(String id, QuestRequest request) {
        Quest quest = findQuestOrThrow(id);
        applyRequestToQuest(quest, request, request.getSchoolId());
        return QuestMapper.toResponse(questRepository.save(quest));
    }

    public void deleteQuestAsAdmin(String id) {
        if (!questRepository.existsById(id)) {
            throw ApiException.notFound("Quest not found");
        }
        questRepository.deleteById(id);
    }

    // ---------- Teacher (scoped to their own school; can only edit/delete what they created) ----------

    public Page<QuestResponse> listQuestsForTeacher(String teacherUserId, Pageable pageable) {
        return questRepository.findByCreatedByUserId(teacherUserId, pageable).map(QuestMapper::toResponse);
    }

    public QuestResponse createQuestAsTeacher(QuestRequest request, String teacherUserId) {
        User teacher = userRepository.findById(teacherUserId)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));
        if (teacher.getRole() != Role.TEACHER || teacher.getTeacherProfile() == null) {
            throw ApiException.badRequest("User is not a teacher");
        }
        String forcedSchoolId = teacher.getTeacherProfile().getAssignedSchoolId();
        Quest quest = buildQuestFromRequest(request, teacherUserId, forcedSchoolId);
        return QuestMapper.toResponse(questRepository.save(quest));
    }

    public QuestResponse updateQuestAsTeacher(String id, QuestRequest request, String teacherUserId) {
        Quest quest = findQuestOrThrow(id);
        assertTeacherOwnsQuest(quest, teacherUserId);
        applyRequestToQuest(quest, request, quest.getSchoolId()); // teachers can't move a quest to another school
        return QuestMapper.toResponse(questRepository.save(quest));
    }

    public void deleteQuestAsTeacher(String id, String teacherUserId) {
        Quest quest = findQuestOrThrow(id);
        assertTeacherOwnsQuest(quest, teacherUserId);
        questRepository.deleteById(id);
    }

    private void assertTeacherOwnsQuest(Quest quest, String teacherUserId) {
        if (!teacherUserId.equals(quest.getCreatedByUserId())) {
            throw ApiException.forbidden("You can only manage quests you created");
        }
    }

    // ---------- Shared ----------

    private Quest findQuestOrThrow(String id) {
        return questRepository.findById(id).orElseThrow(() -> ApiException.notFound("Quest not found"));
    }

    private Quest buildQuestFromRequest(QuestRequest request, String creatorUserId, String schoolId) {
        Quest quest = new Quest();
        quest.setCreatedByUserId(creatorUserId);
        applyRequestToQuest(quest, request, schoolId);
        return quest;
    }

    private void applyRequestToQuest(Quest quest, QuestRequest request, String schoolId) {
        quest.setTitle(request.getTitle());
        quest.setDescription(request.getDescription());
        quest.setCategory(request.getCategory());
        quest.setDifficulty(request.getDifficulty());
        quest.setEcoCoinsReward(request.getEcoCoinsReward());
        quest.setXpReward(request.getXpReward());
        quest.setProofRequired(request.isProofRequired());
        quest.setRequiredProofDescription(request.getRequiredProofDescription());
        quest.setGpsRequired(request.isGpsRequired());

        if (request.isGpsRequired() && request.getTargetLatitude() != null && request.getTargetLongitude() != null) {
            quest.setTargetLocation(new GeoLocation(request.getTargetLatitude(), request.getTargetLongitude()));
            quest.setAllowedRadiusMeters(request.getAllowedRadiusMeters() != null
                    ? request.getAllowedRadiusMeters() : defaultRadiusMeters);
            quest.setAutoRejectOutsideRadius(request.isAutoRejectOutsideRadius());
        } else {
            quest.setTargetLocation(null);
            quest.setAllowedRadiusMeters(null);
            quest.setAutoRejectOutsideRadius(false);
        }

        quest.setStartDate(request.getStartDate());
        quest.setEndDate(request.getEndDate());
        quest.setMaxSubmissionsPerStudent(request.getMaxSubmissionsPerStudent());
        quest.setStatus(request.getStatus());
        quest.setSchoolId(schoolId);
    }
}
