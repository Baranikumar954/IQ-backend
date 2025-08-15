package com.example.IntelliQuestBackend.modules;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "interviewQuestions")
public class InterviewQuestions {
    @Id
    private String id;
    private String email;
    private String companyName;
    private String roleName;
    private String questionType;
    private String experience;
    private String resumeText;
    private String response;
    private LocalDateTime createdAt;
}
