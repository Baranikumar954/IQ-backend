package com.example.IntelliQuestBackend.services;

import com.example.IntelliQuestBackend.modules.User;
import com.example.IntelliQuestBackend.modules.UserProfile;
import com.example.IntelliQuestBackend.repository.UserProfileRepository;
import com.example.IntelliQuestBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JavaMailSender mailSender;

    public String register(String email,String password){
        if(userRepository.findByEmail(email).isPresent()){
            return "Email exists";
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        String encoded = passwordEncoder.encode(password);
        User user= new User(email,encoded,null,false);
        userRepository.save(user);
        sendVerificationEmail(email);
        return "Verification email sent";
    }

    public String verify(String email){
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
            return "Invalid link";
        }
        user.setVerified(true);
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail(email);
        userProfile.setCreatedAt(LocalDateTime.now());
        userProfileRepository.save(userProfile);
        userRepository.save(user);
        return "Verified successfully";
    }

    public boolean login(String email,String password){
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().isVerified()
                && new BCryptPasswordEncoder().matches(password, user.get().getPassword());
    }

    public void sendVerificationEmail(String email){
        String verifyUrl = "https://iq-backend-p2fi.onrender.com/user/verify?email="+email;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Verify your email");
        msg.setText("Click the link to verify "+verifyUrl);
        mailSender.send(msg);
    }

    public void googleLogin(String email,String name){
        userRepository.findByEmail(email).orElseGet(()->{
            User newUser = new User(email, null, name, true);
            userRepository.save(newUser);

            // Save new profile for the user
            if (!userProfileRepository.existsByEmail(email)) {
                UserProfile userProfile = new UserProfile();
                userProfile.setEmail(email);
                userProfile.setCreatedAt(LocalDateTime.now());
                userProfileRepository.save(userProfile);
            }
            return newUser;
        });

    }
}
