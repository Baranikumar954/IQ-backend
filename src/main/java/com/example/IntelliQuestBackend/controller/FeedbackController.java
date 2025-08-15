package com.example.IntelliQuestBackend.controller;

import com.example.IntelliQuestBackend.modules.Feedbacks;
import com.example.IntelliQuestBackend.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(value = "http://localhost:3000",allowCredentials = "true")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/api/feedbacks")
    public ResponseEntity<?> getAllFeedbacks(Authentication authentication){
        String email = (String)authentication.getPrincipal();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get feedbacks"));
        }
        List<Feedbacks> feedbacks=feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/api/feedbacks/average")
    public ResponseEntity<?> getAllAvgRatings(Authentication authentication){
        String email = (String)authentication.getPrincipal();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get feedbacks"));
        }
        double averageRatings = feedbackService.getAllAvgRatings();
        return ResponseEntity.ok().body(java.util.Map.of("average",averageRatings));
    }

    @PostMapping("/api/feedbacks")
    public ResponseEntity<?> updateFeedback(@RequestBody Map<String, Object> body){
        return feedbackService.saveFeedback(body);
    }

    @PostMapping("/user/contact")
    public ResponseEntity<?> sendContactForm(@RequestBody Map<String,String> body){
        return feedbackService.sendContactForm(body);
    }

    @PutMapping("/api/feedbacks/{id}/like")
    public ResponseEntity<?> incrementLike(@PathVariable String id,Authentication authentication){
        String email = (String) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(feedbackService.incrementLike(id, email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/feedbacks/{id}/dislike")
    public ResponseEntity<?> decrementLike(@PathVariable String id,Authentication authentication){
        String email = (String) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(feedbackService.incrementDislike(id, email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
