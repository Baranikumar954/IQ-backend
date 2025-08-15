package com.example.IntelliQuestBackend.services;

import com.example.IntelliQuestBackend.modules.Feedbacks;
import com.example.IntelliQuestBackend.repository.FeedbacksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String toMail;

    @Autowired
    private FeedbacksRepository feedbacksRepository;

    public ResponseEntity<?> sendContactForm(Map<String, String> body) {
        String fromName = body.get("from_name");
        String replyTo = body.get("reply_to");
        String content = body.get("message");
        try{
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setSubject("Contact from " + fromName);
            msg.setTo(toMail);
            msg.setText(content);
            msg.setReplyTo(replyTo);
            mailSender.send(msg);
            return ResponseEntity.ok().body(
                    java.util.Map.of("message", "Your message has been sent successfully!")
            );
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    java.util.Map.of("message", "Failed to send email. Please try again later.")
            );
        }
    }

    public List<Feedbacks> getAllFeedbacks() {
        return feedbacksRepository.findAllByNewestFirst();
    }

    public double getAllAvgRatings() {
        List<Feedbacks> feedbacks = feedbacksRepository.findAll();
        if(feedbacks.isEmpty()){
            return 0.0;
        }
        int sum = 0;
        for (Feedbacks f:feedbacks){
            sum=sum +f.getRating();
        }
        return (double)sum/feedbacks.size();
    }

    public ResponseEntity<?> saveFeedback(Map<String, Object> body) {
        String email = (String) body.get("gmail_id");
        String name = (String) body.get("username");
        String feedback = (String) body.get("feedback");
        Integer rating = (Integer)body.get("rating");
        if (feedback == null || feedback.isEmpty() || rating == null || rating == 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please fill out all fields and select a rating.")
            );
        }
        Feedbacks newFeedback = new Feedbacks();
        newFeedback.setName(name);
        newFeedback.setEmail(email);
        newFeedback.setRating(rating);
        newFeedback.setFeedback(feedback);
        newFeedback.setLike(0);
        newFeedback.setDislike(0);
        newFeedback.setPostedAt(LocalDateTime.now());
        Feedbacks saved = feedbacksRepository.save(newFeedback);
        return ResponseEntity.ok(Map.of(
                "message", "Feedback submitted successfully!",
                "data", saved
        ));
    }

    public Feedbacks incrementLike(String id,String userEmail) {
        Feedbacks feedback = feedbacksRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        // Prevent multiple likes from same user
        if (feedback.getLikedBy().contains(userEmail)) {
            throw new RuntimeException("You have already liked this feedback");
        }

        // If user previously disliked, remove it
        if (feedback.getDislikedBy().remove(userEmail)) {
            feedback.setDislike(feedback.getDislike() - 1);
        }

        feedback.getLikedBy().add(userEmail);
        feedback.setLike(feedback.getLike() + 1);

        return feedbacksRepository.save(feedback);
    }

    public Feedbacks incrementDislike(String id,String userEmail) {
        Feedbacks feedback = feedbacksRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        // Prevent multiple dislikes from same user
        if (feedback.getDislikedBy().contains(userEmail)) {
            throw new RuntimeException("You have already disliked this feedback");
        }

        // If user previously liked, remove it
        if (feedback.getLikedBy().remove(userEmail)) {
            feedback.setLike(feedback.getLike() - 1);
        }

        feedback.getDislikedBy().add(userEmail);
        feedback.setDislike(feedback.getDislike() + 1);

        return feedbacksRepository.save(feedback);
    }
}
