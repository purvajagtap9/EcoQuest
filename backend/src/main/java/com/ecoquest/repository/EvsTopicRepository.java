package com.ecoquest.repository;

import com.ecoquest.entity.EvsTopic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EvsTopicRepository extends MongoRepository<EvsTopic, String> {
    List<EvsTopic> findByPublishedTrueOrderByOrderIndexAsc();
    List<EvsTopic> findAllByOrderByOrderIndexAsc();
}
