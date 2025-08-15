package com.example.IntelliQuestBackend.repository;

import com.example.IntelliQuestBackend.modules.AtsResults;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AtsResultRepository extends MongoRepository<AtsResults,String> {
    Optional<AtsResults> findByEmail(String email);
}
