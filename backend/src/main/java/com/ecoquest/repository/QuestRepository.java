package com.ecoquest.repository;

import com.ecoquest.entity.Quest;
import com.ecoquest.entity.enums.QuestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestRepository extends MongoRepository<Quest, String> {
    Page<Quest> findByStatus(QuestStatus status, Pageable pageable);
    Page<Quest> findByCreatedByUserId(String createdByUserId, Pageable pageable);
}
