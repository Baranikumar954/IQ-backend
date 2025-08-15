package com.example.IntelliQuestBackend.modules;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document
public class UserProfile {

    @Id
    private String id;

    private String fullName = "";
    private String sureName = "";
    private String mobileNumber = "";
    private String email = "";
    private String gender = "Not specified";
    private String education = "Not specified";
    private String collegeName = "Unknown";
    private String aim = "";
    private int strike = 1;
    private LocalDate dob = null;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDate lastLoginDate = LocalDate.now(); // used for checking daily strike

    public String getUserName() {
        return fullName + " " + sureName;
    }
}
