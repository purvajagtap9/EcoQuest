package com.ecoquest.service;

import com.ecoquest.dto.teacher.StudentSummaryResponse;
import com.ecoquest.dto.teacher.TeacherDashboardResponse;
import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;
    private final QuestSubmissionRepository questSubmissionRepository;

    private User loadTeacher(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));
        if (user.getRole() != Role.TEACHER || user.getTeacherProfile() == null) {
            throw ApiException.badRequest("User is not a teacher");
        }
        return user;
    }

    /** School's currently-enrolled student ids — the scope boundary for business rule #4. */
    private List<String> assignedStudentIds(User teacher) {
        String schoolId = teacher.getTeacherProfile().getAssignedSchoolId();
        if (schoolId == null) {
            return List.of();
        }
        return userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, schoolId)
                .stream().map(User::getId).toList();
    }

    public TeacherDashboardResponse getDashboard(String userId) {
        User teacher = loadTeacher(userId);
        String schoolId = teacher.getTeacherProfile().getAssignedSchoolId();
        List<String> studentIds = assignedStudentIds(teacher);

        if (studentIds.isEmpty()) {
            return TeacherDashboardResponse.builder()
                    .pendingReviews(0L).approvedToday(0L).rejectedToday(0L).totalStudents(0L)
                    .build();
        }

        ZoneId zone = ZoneId.systemDefault();
        Instant startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant now = Instant.now();

        long pending = questSubmissionRepository.countByStatusAndStudentIdIn(SubmissionStatus.PENDING, studentIds)
                + questSubmissionRepository.countByStatusAndStudentIdIn(SubmissionStatus.AI_REVIEWED, studentIds);
        long approvedToday = questSubmissionRepository.countByStatusAndStudentIdInAndReviewedAtBetween(
                SubmissionStatus.APPROVED, studentIds, startOfToday, now);
        long rejectedToday = questSubmissionRepository.countByStatusAndStudentIdInAndReviewedAtBetween(
                SubmissionStatus.REJECTED, studentIds, startOfToday, now);
        long totalStudents = userRepository.countByRoleAndStudentProfile_SchoolId(Role.STUDENT, schoolId);

        return TeacherDashboardResponse.builder()
                .pendingReviews(pending)
                .approvedToday(approvedToday)
                .rejectedToday(rejectedToday)
                .totalStudents(totalStudents)
                .build();
    }

    public Page<StudentSummaryResponse> getAssignedStudents(String userId, Pageable pageable) {
        User teacher = loadTeacher(userId);
        String schoolId = teacher.getTeacherProfile().getAssignedSchoolId();

        if (schoolId == null) {
            return Page.empty(pageable);
        }

        return userRepository.findByRoleAndStudentProfile_SchoolId(Role.STUDENT, schoolId, pageable)
                .map(student -> StudentSummaryResponse.builder()
                        .userId(student.getId())
                        .name(student.getName())
                        .email(student.getEmail())
                        .grade(student.getStudentProfile().getGrade())
                        .ecoCoins(student.getStudentProfile().getEcoCoins())
                        .xp(student.getStudentProfile().getXp())
                        .currentStreak(student.getStudentProfile().getStreak() != null
                                ? student.getStudentProfile().getStreak().getCurrentStreak() : 0)
                        .build());
    }

    public String getAssignedSchoolId(String teacherUserId) {
        return loadTeacher(teacherUserId).getTeacherProfile().getAssignedSchoolId();
    }
}
