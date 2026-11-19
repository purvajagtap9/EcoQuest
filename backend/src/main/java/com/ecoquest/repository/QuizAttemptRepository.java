package com.ecoquest.repository;

import com.ecoquest.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {
    boolean existsByStudentIdAndQuizId(String studentId, String quizId);
    Page<QuizAttempt> findByStudentId(String studentId, Pageable pageable);
    long countByStudentIdAndRewardedTrue(String studentId);
}
