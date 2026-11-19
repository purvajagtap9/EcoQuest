package com.ecoquest.service;

import com.ecoquest.dto.admin.AdminDashboardResponse;
import com.ecoquest.dto.admin.SchoolRequest;
import com.ecoquest.dto.admin.SchoolResponse;
import com.ecoquest.dto.admin.UserAdminResponse;
import com.ecoquest.entity.ImpactMetric;
import com.ecoquest.entity.School;
import com.ecoquest.entity.User;
import com.ecoquest.entity.embedded.GeoLocation;
import com.ecoquest.entity.enums.ImpactType;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.BadgeRepository;
import com.ecoquest.repository.ImpactMetricRepository;
import com.ecoquest.repository.QuestRepository;
import com.ecoquest.repository.QuestSubmissionRepository;
import com.ecoquest.repository.SchoolRepository;
import com.ecoquest.repository.StudentBadgeRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final QuestRepository questRepository;
    private final QuestSubmissionRepository questSubmissionRepository;
    private final BadgeRepository badgeRepository;
    private final StudentBadgeRepository studentBadgeRepository;
    private final ImpactMetricRepository impactMetricRepository;

    // ---------- Dashboard ----------

    public AdminDashboardResponse getDashboard() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalTeachers = userRepository.countByRole(Role.TEACHER);
        long totalSchools = schoolRepository.count();
        long totalQuests = questRepository.count();

        long completedQuests = questSubmissionRepository.countByStatus(SubmissionStatus.APPROVED);
        long pendingVerifications = questSubmissionRepository.countByStatus(SubmissionStatus.PENDING)
                + questSubmissionRepository.countByStatus(SubmissionStatus.AI_REVIEWED);
        long rejected = questSubmissionRepository.countByStatus(SubmissionStatus.REJECTED);
        long totalSubmissions = completedQuests + pendingVerifications + rejected;

        long totalEcoCoins = userRepository.findByRole(Role.STUDENT).stream()
                .mapToLong(u -> u.getStudentProfile() != null && u.getStudentProfile().getEcoCoins() != null
                        ? u.getStudentProfile().getEcoCoins() : 0)
                .sum();

        long totalBadgesUnlocked = studentBadgeRepository.count();

        double totalTrees = sumImpact(ImpactType.TREES_PLANTED);
        double totalWaste = sumImpact(ImpactType.WASTE_SEGREGATED_KG);

        long activeUsers = userRepository.countByEnabled(true);

        double questCompletionRate = totalSubmissions == 0 ? 0.0 : (double) completedQuests / totalSubmissions;
        double approvalRate = (completedQuests + rejected) == 0 ? 0.0 : (double) completedQuests / (completedQuests + rejected);

        return AdminDashboardResponse.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalSchools(totalSchools)
                .totalQuests(totalQuests)
                .completedQuests(completedQuests)
                .pendingVerifications(pendingVerifications)
                .totalEcoCoins(totalEcoCoins)
                .totalBadgesUnlocked(totalBadgesUnlocked)
                .totalTreesPlanted(totalTrees)
                .totalWasteSegregatedKg(totalWaste)
                .activeUsers(activeUsers)
                .questCompletionRate(round3(questCompletionRate))
                .approvalRate(round3(approvalRate))
                .build();
    }

    private double sumImpact(ImpactType type) {
        return impactMetricRepository.findByType(type).stream()
                .mapToDouble(m -> m.getValue() == null ? 0 : m.getValue())
                .sum();
    }

    private double round3(double v) {
        return Math.round(v * 1000) / 1000.0;
    }

    // ---------- Schools ----------

    public Page<SchoolResponse> listSchools(Pageable pageable) {
        return schoolRepository.findAll(pageable).map(this::toSchoolResponse);
    }

    public SchoolResponse getSchool(String id) {
        return toSchoolResponse(findSchoolOrThrow(id));
    }

    public SchoolResponse createSchool(SchoolRequest request) {
        School school = School.builder()
                .name(request.getName())
                .city(request.getCity())
                .state(request.getState())
                .campusLocation(toGeoLocation(request))
                .createdAt(Instant.now())
                .build();
        return toSchoolResponse(schoolRepository.save(school));
    }

    public SchoolResponse updateSchool(String id, SchoolRequest request) {
        School school = findSchoolOrThrow(id);
        school.setName(request.getName());
        school.setCity(request.getCity());
        school.setState(request.getState());
        school.setCampusLocation(toGeoLocation(request));
        return toSchoolResponse(schoolRepository.save(school));
    }

    public void deleteSchool(String id) {
        if (!schoolRepository.existsById(id)) {
            throw ApiException.notFound("School not found");
        }
        schoolRepository.deleteById(id);
    }

    private School findSchoolOrThrow(String id) {
        return schoolRepository.findById(id).orElseThrow(() -> ApiException.notFound("School not found"));
    }

    private GeoLocation toGeoLocation(SchoolRequest request) {
        if (request.getCampusLatitude() == null || request.getCampusLongitude() == null) {
            return null;
        }
        return new GeoLocation(request.getCampusLatitude(), request.getCampusLongitude());
    }

    private SchoolResponse toSchoolResponse(School school) {
        return SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .city(school.getCity())
                .state(school.getState())
                .campusLatitude(school.getCampusLocation() != null ? school.getCampusLocation().getLatitude() : null)
                .campusLongitude(school.getCampusLocation() != null ? school.getCampusLocation().getLongitude() : null)
                .createdAt(school.getCreatedAt())
                .build();
    }

    // ---------- Students / Teachers ----------

    public Page<UserAdminResponse> listStudents(Pageable pageable) {
        return userRepository.findByRole(Role.STUDENT, pageable).map(this::toUserAdminResponse);
    }

    public Page<UserAdminResponse> listTeachers(Pageable pageable) {
        return userRepository.findByRole(Role.TEACHER, pageable).map(this::toUserAdminResponse);
    }

    public UserAdminResponse getUser(String id) {
        return toUserAdminResponse(findUserOrThrow(id));
    }

    public UserAdminResponse setUserEnabled(String id, boolean enabled) {
        User user = findUserOrThrow(id);
        user.setEnabled(enabled);
        user.setUpdatedAt(Instant.now());
        return toUserAdminResponse(userRepository.save(user));
    }

    public UserAdminResponse setLeaderboardPrivacy(String id, String displayName, boolean showRealNameOnLeaderboard) {
        User user = findUserOrThrow(id);
        if (user.getRole() != Role.STUDENT || user.getStudentProfile() == null) {
            throw ApiException.badRequest("User is not a student");
        }
        user.getStudentProfile().setDisplayName(displayName != null && !displayName.isBlank() ? displayName : user.getName());
        user.getStudentProfile().setShowRealNameOnLeaderboard(showRealNameOnLeaderboard);
        user.setUpdatedAt(Instant.now());
        return toUserAdminResponse(userRepository.save(user));
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw ApiException.notFound("User not found");
        }
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(String id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    private UserAdminResponse toUserAdminResponse(User user) {
        UserAdminResponse.UserAdminResponseBuilder builder = UserAdminResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt());

        if (user.getStudentProfile() != null) {
            builder.schoolId(user.getStudentProfile().getSchoolId())
                    .grade(user.getStudentProfile().getGrade())
                    .ecoCoins(user.getStudentProfile().getEcoCoins())
                    .xp(user.getStudentProfile().getXp());
        }
        if (user.getTeacherProfile() != null) {
            builder.assignedSchoolId(user.getTeacherProfile().getAssignedSchoolId());
        }
        return builder.build();
    }
}
