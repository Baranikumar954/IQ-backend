package com.example.IntelliQuestBackend.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionGenerationService {

    @Autowired
    private GeminiService geminiService;

    public String getExtractedText(MultipartFile file) {
        if(file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")){
            return "Invalid";
        }
        try(PDDocument document = PDDocument.load(file.getInputStream())){
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(2); // ✅ only first 2 pages to reduce size
            String text = stripper.getText(document);
            return text;
        }catch (IOException e){
            return "Failed";
        }
    }

    public List<String> generateQuestions(String extractedText, String company, String role, String questionType, String experience,String email) {
        return geminiService.generateQuestions(extractedText,company,role,questionType,experience,email);
    }
}
