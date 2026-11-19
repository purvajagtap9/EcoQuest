package com.ecoquest.mapper;

import com.ecoquest.dto.admin.LessonAdminResponse;
import com.ecoquest.dto.admin.TopicAdminResponse;
import com.ecoquest.dto.common.LocalizedTextDto;
import com.ecoquest.dto.learning.LessonResponse;
import com.ecoquest.dto.learning.TopicResponse;
import com.ecoquest.entity.EvsTopic;
import com.ecoquest.entity.Lesson;
import com.ecoquest.entity.embedded.LocalizedText;

import java.util.Collections;
import java.util.List;

public class LearningMapper {

    private LearningMapper() {
    }

    public static LocalizedText toEntity(LocalizedTextDto dto) {
        if (dto == null) return null;
        return new LocalizedText(dto.getEn(), dto.getHi(), dto.getMr());
    }

    public static LocalizedTextDto toDto(LocalizedText entity) {
        if (entity == null) return null;
        return new LocalizedTextDto(entity.getEn(), entity.getHi(), entity.getMr());
    }

    // ---------- Topic ----------

    public static TopicResponse toTopicResponse(EvsTopic topic, String lang) {
        List<String> objectives = topic.getLearningObjectives() == null
                ? Collections.emptyList()
                : topic.getLearningObjectives().stream().map(o -> o.get(lang)).toList();

        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle() != null ? topic.getTitle().get(lang) : null)
                .description(topic.getDescription() != null ? topic.getDescription().get(lang) : null)
                .learningObjectives(objectives)
                .coverImageUrl(topic.getCoverImageUrl())
                .orderIndex(topic.getOrderIndex())
                .build();
    }

    public static TopicAdminResponse toTopicAdminResponse(EvsTopic topic) {
        List<LocalizedTextDto> objectives = topic.getLearningObjectives() == null
                ? Collections.emptyList()
                : topic.getLearningObjectives().stream().map(LearningMapper::toDto).toList();

        return TopicAdminResponse.builder()
                .id(topic.getId())
                .title(toDto(topic.getTitle()))
                .description(toDto(topic.getDescription()))
                .learningObjectives(objectives)
                .coverImageUrl(topic.getCoverImageUrl())
                .orderIndex(topic.getOrderIndex())
                .published(topic.isPublished())
                .createdAt(topic.getCreatedAt())
                .build();
    }

    // ---------- Lesson ----------

    public static LessonResponse toLessonResponse(Lesson lesson, String lang) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .topicId(lesson.getTopicId())
                .title(lesson.getTitle() != null ? lesson.getTitle().get(lang) : null)
                .body(lesson.getBody() != null ? lesson.getBody().get(lang) : null)
                .localContextNote(lesson.getLocalContextNote() != null ? lesson.getLocalContextNote().get(lang) : null)
                .imageUrl(lesson.getImageUrl())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }

    public static LessonAdminResponse toLessonAdminResponse(Lesson lesson) {
        return LessonAdminResponse.builder()
                .id(lesson.getId())
                .topicId(lesson.getTopicId())
                .title(toDto(lesson.getTitle()))
                .body(toDto(lesson.getBody()))
                .localContextNote(toDto(lesson.getLocalContextNote()))
                .imageUrl(lesson.getImageUrl())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }
}
