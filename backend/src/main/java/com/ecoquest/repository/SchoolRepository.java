package com.ecoquest.repository;

import com.ecoquest.entity.School;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SchoolRepository extends MongoRepository<School, String> {
    // findAll(Pageable), findById, save, deleteById all come from MongoRepository
}
