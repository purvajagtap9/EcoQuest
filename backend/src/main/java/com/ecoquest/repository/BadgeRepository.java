package com.ecoquest.repository;

import com.ecoquest.entity.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BadgeRepository extends MongoRepository<Badge, String> {
    boolean existsByCode(String code);
}
