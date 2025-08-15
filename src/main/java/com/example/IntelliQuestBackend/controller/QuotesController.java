package com.example.IntelliQuestBackend.controller;

import com.example.IntelliQuestBackend.modules.Quotes;
import com.example.IntelliQuestBackend.services.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin(origins = "https://iq-frontend-swart.vercel.app",allowCredentials = "true")
public class QuotesController {
    @Autowired
    private QuoteService quoteService;

    @GetMapping("/api/quotes/random")
    public ResponseEntity<?> getRandomQuote(){
        Quotes quote = quoteService.getRandomQuote();
        if(quote!=null){
            return ResponseEntity.ok(quote);
        }else{
            return ResponseEntity.status(400).body(Map.of("Error","Fetch random quote failed."));
        }
    }

    @GetMapping("/api/quotes/all")
    public ResponseEntity<?> getAllQuotes(){
        List<Quotes> localQuotes = quoteService.getAllQuotes();
        if(!localQuotes.isEmpty()){
            return ResponseEntity.ok(localQuotes);
        }else{
            return ResponseEntity.status(400).body(Map.of("Error","Fetch random quote failed."));
        }
    }
}
