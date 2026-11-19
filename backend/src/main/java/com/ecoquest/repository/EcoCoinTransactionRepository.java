package com.ecoquest.repository;

import com.ecoquest.entity.EcoCoinTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EcoCoinTransactionRepository extends MongoRepository<EcoCoinTransaction, String> {
    Page<EcoCoinTransaction> findByStudentId(String studentId, Pageable pageable);

    // Unpaged - used to sum lifetime earned coins for badge criteria evaluation.
    List<EcoCoinTransaction> findByStudentId(String studentId);
}
