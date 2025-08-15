package com.example.IntelliQuestBackend.controller;

import com.example.IntelliQuestBackend.modules.UserProfile;
import com.example.IntelliQuestBackend.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = "https://iq-frontend-swart.vercel.app",allowCredentials = "true")
public class UserProfileController {
    @Autowired
    UserProfileService userProfileService;

    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfileInfo(Authentication authentication){
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userProfileService.getProfileInfo(email));
    }

    @PutMapping("/user/edit/profile/{id}")
    public ResponseEntity<?> updateProfileInfo(@PathVariable String id,@RequestBody UserProfile userProfile,Authentication authentication ){
        System.out.println("AUTH: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authenticated");
        }
        String email = (String)authentication.getPrincipal();
        UserProfile updatedProfile = userProfileService.updateProfileInfo(id,email,userProfile);
        if(updatedProfile!=null){
           return ResponseEntity.ok(updatedProfile);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong while updating profile.");
        }
    }
}
