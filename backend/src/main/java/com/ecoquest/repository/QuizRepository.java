package com.ecoquest.repository;

import com.ecoquest.entity.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByTopicId(String topicId);
    List<Quiz> findByTopicIdAndPublishedTrue(String topicId);
    Optional<Quiz> findByIdAndPublishedTrue(String id);
}
