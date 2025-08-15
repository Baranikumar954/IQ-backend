package com.example.IntelliQuestBackend.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AtsCheckService {

    public ResponseEntity<?> getExtractedText(MultipartFile file) {
        if(file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")){
            return ResponseEntity.badRequest().body("Invalid file.Please upload a PDF");
        }
        try(PDDocument document = PDDocument.load(file.getInputStream())){
//            PDFTextStripper stripper = new PDFTextStripper();
//            String text = stripper.getText(document);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(2); // ✅ only first 2 pages to reduce size
            String text = stripper.getText(document);
            return ResponseEntity.ok().body(java.util.Map.of("text",text));
        }catch (IOException e){
            return ResponseEntity.status(500).body("Failed to extract text from PDF");
        }
    }
}
