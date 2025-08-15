package com.example.IntelliQuestBackend.modules;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "feedbacks")
public class Feedbacks {
    @Id
    private String id;
    private String name;
    private String email;
    private String feedback;
    private int rating;
    private int like;
    private int dislike;
    private LocalDateTime postedAt;

    private Set<String> likedBy = new HashSet<>();
    private Set<String> dislikedBy = new HashSet<>();
}
