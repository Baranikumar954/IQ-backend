package com.example.IntelliQuestBackend.repository;

import com.example.IntelliQuestBackend.modules.Feedbacks;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbacksRepository extends MongoRepository<Feedbacks,String> {
    default List<Feedbacks> findAllByNewestFirst() {
        return findAll(Sort.by(Sort.Direction.DESC, "postedAt"));
    }
}
