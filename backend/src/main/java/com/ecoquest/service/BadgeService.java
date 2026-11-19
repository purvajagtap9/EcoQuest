package com.ecoquest.service;

import com.ecoquest.dto.admin.BadgeAdminRequest;
import com.ecoquest.dto.admin.BadgeAdminResponse;
import com.ecoquest.dto.student.StudentBadgeResponse;
import com.ecoquest.entity.Badge;
import com.ecoquest.entity.StudentBadge;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.BadgeRepository;
import com.ecoquest.repository.StudentBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final StudentBadgeRepository studentBadgeRepository;

    // ---------- Student ----------

    public List<StudentBadgeResponse> listForStudent(String studentId) {
        Map<String, Instant> earnedAtByBadgeId = studentBadgeRepository.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(StudentBadge::getBadgeId, StudentBadge::getEarnedAt));

        return badgeRepository.findAll().stream()
                .map(b -> StudentBadgeResponse.builder()
                        .badgeId(b.getId())
                        .code(b.getCode())
                        .name(b.getName())
                        .description(b.getDescription())
                        .icon(b.getIcon())
                        .earned(earnedAtByBadgeId.containsKey(b.getId()))
                        .earnedAt(earnedAtByBadgeId.get(b.getId()))
                        .build())
                .toList();
    }

    // ---------- Admin ----------

    public List<BadgeAdminResponse> listAllAdmin() {
        return badgeRepository.findAll().stream().map(this::toAdminResponse).toList();
    }

    public BadgeAdminResponse createBadge(BadgeAdminRequest request) {
        if (badgeRepository.existsByCode(request.getCode())) {
            throw ApiException.conflict("A badge with this code already exists");
        }
        Badge badge = Badge.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .criteriaType(request.getCriteriaType())
                .criteriaThreshold(request.getCriteriaThreshold())
                .criteriaCategory(request.getCriteriaCategory())
                .ecoCoinsReward(request.getEcoCoinsReward())
                .xpReward(request.getXpReward())
                .createdAt(Instant.now())
                .build();
        return toAdminResponse(badgeRepository.save(badge));
    }

    public BadgeAdminResponse updateBadge(String id, BadgeAdminRequest request) {
        Badge badge = badgeRepository.findById(id).orElseThrow(() -> ApiException.notFound("Badge not found"));
        badge.setCode(request.getCode());
        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIcon(request.getIcon());
        badge.setCriteriaType(request.getCriteriaType());
        badge.setCriteriaThreshold(request.getCriteriaThreshold());
        badge.setCriteriaCategory(request.getCriteriaCategory());
        badge.setEcoCoinsReward(request.getEcoCoinsReward());
        badge.setXpReward(request.getXpReward());
        return toAdminResponse(badgeRepository.save(badge));
    }

    public void deleteBadge(String id) {
        if (!badgeRepository.existsById(id)) {
            throw ApiException.notFound("Badge not found");
        }
        badgeRepository.deleteById(id);
    }

    private BadgeAdminResponse toAdminResponse(Badge b) {
        return BadgeAdminResponse.builder()
                .id(b.getId())
                .code(b.getCode())
                .name(b.getName())
                .description(b.getDescription())
                .icon(b.getIcon())
                .criteriaType(b.getCriteriaType())
                .criteriaThreshold(b.getCriteriaThreshold())
                .criteriaCategory(b.getCriteriaCategory())
                .ecoCoinsReward(b.getEcoCoinsReward())
                .xpReward(b.getXpReward())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
