package com.example.IntelliQuestBackend.services;

import com.example.IntelliQuestBackend.modules.Quotes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class QuoteService {
    private final List<Quotes> localQuotes;
    private final Random random = new Random();

    public QuoteService() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        localQuotes = Arrays.asList(
                mapper.readValue(
                        new ClassPathResource("quotes.json").getInputStream(),
                        Quotes[].class
                )
        );
    }

    public Quotes getRandomQuote() {
        return localQuotes.get(random.nextInt(localQuotes.size()));
    }

    public List<Quotes> getAllQuotes(){
        return localQuotes;
    }
}
