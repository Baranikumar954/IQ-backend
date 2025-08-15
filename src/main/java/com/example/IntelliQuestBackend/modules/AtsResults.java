package com.example.IntelliQuestBackend.modules;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Document(collection = "atsResults")
public class AtsResults {
    @Id
    private String id;
    private String email;
    private int atsScore;
    private ArrayList<String> suggestions;
    private ArrayList<String> strengths;
    private ArrayList<String> weaknesses;
    private LocalDateTime createResultAt;
}
