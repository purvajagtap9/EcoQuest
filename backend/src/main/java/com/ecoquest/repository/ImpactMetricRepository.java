package com.ecoquest.repository;

import com.ecoquest.entity.ImpactMetric;
import com.ecoquest.entity.enums.ImpactType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ImpactMetricRepository extends MongoRepository<ImpactMetric, String> {
    List<ImpactMetric> findByType(ImpactType type);
    List<ImpactMetric> findByStudentId(String studentId);
    List<ImpactMetric> findBySchoolId(String schoolId);
}
