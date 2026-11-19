package com.ecoquest.mapper;

import com.ecoquest.dto.quest.QuestResponse;
import com.ecoquest.entity.Quest;
import com.ecoquest.util.MapUrlUtil;

public class QuestMapper {

    private QuestMapper() {
    }

    public static QuestResponse toResponse(Quest quest) {
        Double lat = quest.getTargetLocation() != null ? quest.getTargetLocation().getLatitude() : null;
        Double lng = quest.getTargetLocation() != null ? quest.getTargetLocation().getLongitude() : null;

        return QuestResponse.builder()
                .id(quest.getId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .category(quest.getCategory())
                .difficulty(quest.getDifficulty())
                .ecoCoinsReward(quest.getEcoCoinsReward())
                .xpReward(quest.getXpReward())
                .proofRequired(quest.isProofRequired())
                .requiredProofDescription(quest.getRequiredProofDescription())
                .gpsRequired(quest.isGpsRequired())
                .targetLatitude(lat)
                .targetLongitude(lng)
                .targetLocationMapUrl(MapUrlUtil.openStreetMapUrl(lat, lng))
                .allowedRadiusMeters(quest.getAllowedRadiusMeters())
                .autoRejectOutsideRadius(quest.isAutoRejectOutsideRadius())
                .startDate(quest.getStartDate())
                .endDate(quest.getEndDate())
                .maxSubmissionsPerStudent(quest.getMaxSubmissionsPerStudent())
                .status(quest.getStatus())
                .schoolId(quest.getSchoolId())
                .createdByUserId(quest.getCreatedByUserId())
                .build();
    }
}
