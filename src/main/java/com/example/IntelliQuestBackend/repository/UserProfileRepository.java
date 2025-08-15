package com.example.IntelliQuestBackend.repository;

import com.example.IntelliQuestBackend.modules.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile,String> {
    Optional<UserProfile> findByEmail(String email);
    boolean existsByEmail(String email);
}
