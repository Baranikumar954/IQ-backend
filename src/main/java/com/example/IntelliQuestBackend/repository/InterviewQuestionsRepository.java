package com.example.IntelliQuestBackend.repository;

import com.example.IntelliQuestBackend.modules.InterviewQuestions;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InterviewQuestionsRepository extends MongoRepository<InterviewQuestions,String> {
    Optional<InterviewQuestions> findByEmail(String email);
}
