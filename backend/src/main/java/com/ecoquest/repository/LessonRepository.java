package com.ecoquest.repository;

import com.ecoquest.entity.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LessonRepository extends MongoRepository<Lesson, String> {
    List<Lesson> findByTopicIdOrderByOrderIndexAsc(String topicId);
    long deleteByTopicId(String topicId);
}
