package com.example.IntelliQuestBackend.controller;

import com.example.IntelliQuestBackend.services.GeminiService;
import com.example.IntelliQuestBackend.services.QuestionGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "https://iq-frontend-swart.vercel.app",allowCredentials = "true")
public class QuestionGenerationController {
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private QuestionGenerationService questionGenerationService;

    @PostMapping("/api/questions/generate")
    public ResponseEntity<?> generateQuestions(@RequestParam("resume")MultipartFile resume,
                                               @RequestParam("companyName") String company,
                                               @RequestParam("roleName") String role,
                                               @RequestParam("questionType") String questionType,
                                               @RequestParam("experience") String experience, Authentication authentication){
        String email = (String) authentication.getPrincipal();
        System.out.println("In Ctrl :"+company+role+questionType+experience);
        String extractedText = questionGenerationService.getExtractedText(resume);
        if(extractedText.equals("Invalid") || extractedText.equals("Failed")){
            return ResponseEntity.badRequest().body("Invalid file can not extracted");
        }
        List<String> questions = questionGenerationService.generateQuestions(extractedText,company,role,questionType,experience,email);
        return ResponseEntity.ok(java.util.Map.of("resumeText",extractedText,"questions",questions));
    }

    @PostMapping("/api/questions/improve")
    public ResponseEntity<?> generateImprovedAnswer(@RequestBody Map<String,String> body,Authentication authentication){
        String email = (String)authentication.getPrincipal();
        if(email.isEmpty()){
            return ResponseEntity.status(401).body(
                    java.util.Map.of("message", "Failed no data in the request")
            );
        }
        String question = body.get("question");
        String userAnswer = body.get("userAnswer");
        if(question.isEmpty() || userAnswer.isEmpty()){
            return ResponseEntity.status(204).body(
                    java.util.Map.of("message", "Failed no data in the request")
            );
        }
        try{
            String improvedAnswer = geminiService.getImprovedAnswer(question,userAnswer);
            return ResponseEntity.ok().body(
                    java.util.Map.of("answer", improvedAnswer)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    java.util.Map.of("message", "Failed to fetch improved answer")
            );
        }
    }
}
