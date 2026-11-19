package com.ecoquest.repository;

import com.ecoquest.entity.StudentBadge;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudentBadgeRepository extends MongoRepository<StudentBadge, String> {

    List<StudentBadge> findByStudentId(String studentId);

    long countByStudentId(String studentId);

    boolean existsByStudentIdAndBadgeId(String studentId, String badgeId);
}
