package com.example.IntelliQuestBackend.controller;

import com.example.IntelliQuestBackend.services.AtsCheckService;
import com.example.IntelliQuestBackend.services.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:3000","https://iq-frontend-swart.vercel.app",allowCredentials = "true")
public class AtsCheckController {
    @Autowired
    private AtsCheckService atsCheckService;

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/api/extract-text")
    public ResponseEntity<?> extractTextFromPdf(@RequestParam("file")MultipartFile file, Authentication authentication){
        String email = (String)authentication.getPrincipal();
        System.out.println("Uploaded by : "+email);
        return atsCheckService.getExtractedText(file);
    }

    @PostMapping("/api/generate-suggestions")
    public ResponseEntity<?> generateSuggestions(@RequestBody Map<String,String> body,Authentication authentication){
        String extractedText = body.get("text");
        if(extractedText==null || extractedText.isEmpty()){
            return ResponseEntity.badRequest().body("Text is required");
        }
        String email = (String)authentication.getPrincipal();

        try{
            Map<String,Object> suggestions = geminiService.getSuggestionsFromGemini(extractedText,email);
            return ResponseEntity.ok(suggestions);
        }catch(Exception e){
            return ResponseEntity.status(500).body("Failed to get suggestions from Gemini API.");
        }
    }
}
