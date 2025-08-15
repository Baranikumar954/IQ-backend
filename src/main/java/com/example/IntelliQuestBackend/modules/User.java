package com.example.IntelliQuestBackend.modules;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String name;
    private boolean verified;

    public User(String email, String password, String name, boolean verified) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.verified = verified;
    }
}
