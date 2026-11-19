package com.ecoquest.service;

import com.ecoquest.dto.admin.LessonAdminRequest;
import com.ecoquest.dto.admin.LessonAdminResponse;
import com.ecoquest.dto.admin.TopicAdminRequest;
import com.ecoquest.dto.admin.TopicAdminResponse;
import com.ecoquest.dto.learning.LessonResponse;
import com.ecoquest.dto.learning.TopicDetailResponse;
import com.ecoquest.dto.learning.TopicResponse;
import com.ecoquest.entity.EvsTopic;
import com.ecoquest.entity.Lesson;
import com.ecoquest.exception.ApiException;
import com.ecoquest.mapper.LearningMapper;
import com.ecoquest.repository.EvsTopicRepository;
import com.ecoquest.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvsLearningService {

    private final EvsTopicRepository topicRepository;
    private final LessonRepository lessonRepository;

    // ---------- Public ----------

    public List<TopicResponse> listPublishedTopics(String lang) {
        return topicRepository.findByPublishedTrueOrderByOrderIndexAsc().stream()
                .map(t -> LearningMapper.toTopicResponse(t, lang))
                .toList();
    }

    public TopicDetailResponse getTopicDetail(String topicId, String lang) {
        EvsTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("Topic not found"));
        if (!topic.isPublished()) {
            throw ApiException.notFound("Topic not found");
        }

        List<LessonResponse> lessons = lessonRepository.findByTopicIdOrderByOrderIndexAsc(topicId).stream()
                .map(l -> LearningMapper.toLessonResponse(l, lang))
                .toList();

        List<String> objectives = topic.getLearningObjectives() == null
                ? List.of()
                : topic.getLearningObjectives().stream().map(o -> o.get(lang)).toList();

        return TopicDetailResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle() != null ? topic.getTitle().get(lang) : null)
                .description(topic.getDescription() != null ? topic.getDescription().get(lang) : null)
                .learningObjectives(objectives)
                .coverImageUrl(topic.getCoverImageUrl())
                .lessons(lessons)
                .build();
    }

    // ---------- Admin: Topics ----------

    public List<TopicAdminResponse> listAllTopicsAdmin() {
        return topicRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(LearningMapper::toTopicAdminResponse)
                .toList();
    }

    public TopicAdminResponse createTopic(TopicAdminRequest request) {
        EvsTopic topic = EvsTopic.builder()
                .title(LearningMapper.toEntity(request.getTitle()))
                .description(LearningMapper.toEntity(request.getDescription()))
                .learningObjectives(request.getLearningObjectives() == null ? List.of()
                        : request.getLearningObjectives().stream().map(LearningMapper::toEntity).toList())
                .coverImageUrl(request.getCoverImageUrl())
                .orderIndex(request.getOrderIndex())
                .published(request.isPublished())
                .build();
        return LearningMapper.toTopicAdminResponse(topicRepository.save(topic));
    }

    public TopicAdminResponse updateTopic(String id, TopicAdminRequest request) {
        EvsTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Topic not found"));

        topic.setTitle(LearningMapper.toEntity(request.getTitle()));
        topic.setDescription(LearningMapper.toEntity(request.getDescription()));
        topic.setLearningObjectives(request.getLearningObjectives() == null ? List.of()
                : request.getLearningObjectives().stream().map(LearningMapper::toEntity).toList());
        topic.setCoverImageUrl(request.getCoverImageUrl());
        topic.setOrderIndex(request.getOrderIndex());
        topic.setPublished(request.isPublished());

        return LearningMapper.toTopicAdminResponse(topicRepository.save(topic));
    }

    public void deleteTopic(String id) {
        if (!topicRepository.existsById(id)) {
            throw ApiException.notFound("Topic not found");
        }
        lessonRepository.deleteByTopicId(id); // cascade - lessons can't exist without their topic
        topicRepository.deleteById(id);
    }

    // ---------- Admin: Lessons ----------

    public List<LessonAdminResponse> listLessonsAdmin(String topicId) {
        return lessonRepository.findByTopicIdOrderByOrderIndexAsc(topicId).stream()
                .map(LearningMapper::toLessonAdminResponse)
                .toList();
    }

    public LessonAdminResponse createLesson(LessonAdminRequest request) {
        if (!topicRepository.existsById(request.getTopicId())) {
            throw ApiException.badRequest("No such topic: " + request.getTopicId());
        }
        Lesson lesson = Lesson.builder()
                .topicId(request.getTopicId())
                .title(LearningMapper.toEntity(request.getTitle()))
                .body(LearningMapper.toEntity(request.getBody()))
                .localContextNote(LearningMapper.toEntity(request.getLocalContextNote()))
                .imageUrl(request.getImageUrl())
                .orderIndex(request.getOrderIndex())
                .build();
        return LearningMapper.toLessonAdminResponse(lessonRepository.save(lesson));
    }

    public LessonAdminResponse updateLesson(String id, LessonAdminRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Lesson not found"));

        lesson.setTopicId(request.getTopicId());
        lesson.setTitle(LearningMapper.toEntity(request.getTitle()));
        lesson.setBody(LearningMapper.toEntity(request.getBody()));
        lesson.setLocalContextNote(LearningMapper.toEntity(request.getLocalContextNote()));
        lesson.setImageUrl(request.getImageUrl());
        lesson.setOrderIndex(request.getOrderIndex());

        return LearningMapper.toLessonAdminResponse(lessonRepository.save(lesson));
    }

    public void deleteLesson(String id) {
        if (!lessonRepository.existsById(id)) {
            throw ApiException.notFound("Lesson not found");
        }
        lessonRepository.deleteById(id);
    }
}
