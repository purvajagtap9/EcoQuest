package com.ecoquest.repository;

import com.ecoquest.entity.ImpactFormulaConfig;
import com.ecoquest.entity.enums.QuestCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImpactFormulaConfigRepository extends MongoRepository<ImpactFormulaConfig, String> {
    Optional<ImpactFormulaConfig> findByCategory(QuestCategory category);
}
