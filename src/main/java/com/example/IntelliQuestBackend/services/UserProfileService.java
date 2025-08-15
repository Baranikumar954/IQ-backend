package com.example.IntelliQuestBackend.services;

import com.example.IntelliQuestBackend.modules.User;
import com.example.IntelliQuestBackend.modules.UserProfile;
import com.example.IntelliQuestBackend.repository.UserProfileRepository;
import com.example.IntelliQuestBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserProfileService {
    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserRepository userRepository;

    public UserProfile getProfileInfo(String email) {
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findByEmail(email);

        if (optionalUserProfile.isEmpty()) {
            return null;
        }

        UserProfile profile = optionalUserProfile.get();

        LocalDate today = LocalDate.now();
        LocalDate lastLogin = profile.getLastLoginDate();

        // If this is the first login or not the same day
        if (lastLogin == null || !lastLogin.isEqual(today)) {
            if (lastLogin != null && lastLogin.plusDays(1).isEqual(today)) {
                // Continued streak
                profile.setStrike(profile.getStrike() + 1);
            } else {
                // Missed a day or first time login
                profile.setStrike(1);
            }

            // Update last login date
            profile.setLastLoginDate(today);
            userProfileRepository.save(profile);
        }

        return profile;
    }

    public UserProfile updateProfileInfo(String id,String email, UserProfile userProfile) {
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findById(id);
        if(optionalUserProfile.isEmpty()){
            return null;
        }
        UserProfile existingUser = optionalUserProfile.get();
        existingUser.setFullName(userProfile.getFullName());
        existingUser.setSureName(userProfile.getSureName());
        existingUser.setMobileNumber(userProfile.getMobileNumber());
        existingUser.setGender(userProfile.getGender());
        existingUser.setEducation(userProfile.getEducation());
        existingUser.setCollegeName(userProfile.getCollegeName());
        existingUser.setAim(userProfile.getAim());
        existingUser.setDob(userProfile.getDob());

        Optional<User> optionalUser =userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            User newUpdatedUser = optionalUser.get();
            newUpdatedUser.setName(userProfile.getUserName());
            userRepository.save(newUpdatedUser);
        }
        return userProfileRepository.save(existingUser);
    }
}
