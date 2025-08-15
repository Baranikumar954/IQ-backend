package com.example.IntelliQuestBackend.services;

import com.example.IntelliQuestBackend.modules.AtsResults;
import com.example.IntelliQuestBackend.modules.InterviewQuestions;
import com.example.IntelliQuestBackend.repository.AtsResultRepository;
import com.example.IntelliQuestBackend.repository.InterviewQuestionsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GeminiService {
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    @Autowired
    private AtsResultRepository atsResultRepository;

    @Autowired
    private InterviewQuestionsRepository interviewQuestionsRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String PROMPT_TEMPLATE = """
        You are an expert Applicant Tracking System (ATS) evaluator and resume coach.

        Given the resume and the job description below:

        1. Analyze the resume for ATS-friendliness.
        2. Score it out of 100 based on:
           - Keyword match (40 points)
           - Structure & formatting (20 points)
           - Clarity & readability (20 points)
           - Overall relevance to the job (20 points)
        3. List the strengths in the resume.
        4. List the weaknesses or red flags (e.g., use of tables, uncommon fonts, graphics).
        5. Give actionable improvement suggestions (e.g., missing keywords, format changes).
        6. Output in structured JSON format:
           {
             "ats_score": 87,
             "strengths": [...],
             "weaknesses": [...],
             "suggestions": [...]
           }

        Resume: %s
        """;

    public Map<String, Object> getSuggestionsFromGemini(String extractedText, String email) throws Exception {
        String prompt = String.format(PROMPT_TEMPLATE, extractedText);

        Map<String, Object> message = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(message, headers);

        String url = GEMINI_URL + geminiApiKey;

        try {
            ResponseEntity<JsonNode> response = retryGeminiPost(requestEntity, url, 3, 2000);

            String contentText = response.getBody()
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText()
                    .trim();

            // Clean markdown
            contentText = contentText.replaceAll("(?s)```json|```", "").trim();

            // Parse JSON
            Map<String, Object> parsedMap = mapper.readValue(contentText, Map.class);

            // Extract fields
            int atsScore = (int) parsedMap.get("ats_score");
            ArrayList<String> strengths = new ArrayList<>((List<String>) parsedMap.get("strengths"));
            ArrayList<String> weaknesses = new ArrayList<>((List<String>) parsedMap.get("weaknesses"));
            ArrayList<String> suggestions = new ArrayList<>((List<String>) parsedMap.get("suggestions"));

            // Save to DB
            AtsResults atsResults = atsResultRepository.findByEmail(email).orElse(new AtsResults());
            atsResults.setEmail(email);
            atsResults.setSuggestions(suggestions);
            atsResults.setAtsScore(atsScore);
            atsResults.setStrengths(strengths);
            atsResults.setWeaknesses(weaknesses);
            atsResults.setCreateResultAt(LocalDateTime.now());

            atsResultRepository.save(atsResults);

            return parsedMap;

        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage());
            throw e;
        }
    }


    public List<String> generateQuestions(String extractedText, String company, String role, String questionType, String experience,String email)  {
//        String prompt = String.format("""
//            You are an HR interview expert.
//
//            Here is the candidate's resume text:
//            %s
//
//            Your task:
//            Generate %s interview questions tailored to the candidate's resume for the role of **%s**.
//
//            Consider that the candidate has **%s** of experience.
//
//            Please generate 10–20 relevant interview questions for each given question type.
//            """,
//                extractedText,
//                questionType.equals("Both") ? "a mix of technical and non-technical" :
//                        questionType.equals("Non-Technical") ? "non-technical (HR-related)" : "technical",
//                role,
//                experience
//        );
        String prompt = String.format("""
            You are an experienced HR and technical interviewer.
            
            Candidate's Resume:
            %s
            
            Instructions:
            - Role: **%s**
            - Candidate Experience: **%s**
            - Question Type: %s
                - If "Both", create a balanced mix of technical and non-technical questions.
                - If "Technical", focus only on technical skills and knowledge relevant to the role.
                - If "Non-Technical", focus only on HR and behavioral questions.
            
            Task:
            Generate 10–20 **clear, unique, and role-specific** interview questions based on the candidate’s resume, experience, and the specified question type.
            
            Important:
            - Ensure all questions are relevant to the candidate’s background and role.
            - Use simple, professional language.
            - Present questions as a numbered list without explanations.
            """, extractedText, role, experience, questionType
        );

        Map<String,Object> requestBody = Map.of("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
        ));
        String url = GEMINI_URL+geminiApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody,headers);

        InterviewQuestions interviewQuestions = interviewQuestionsRepository.findByEmail(email).orElse(new InterviewQuestions());

        interviewQuestions.setEmail(email);
        interviewQuestions.setResumeText(extractedText);
        interviewQuestions.setQuestionType(questionType);
        interviewQuestions.setExperience(experience);
        interviewQuestions.setCompanyName(company);
        interviewQuestions.setRoleName(role);
        interviewQuestions.setCreatedAt(LocalDateTime.now());
        interviewQuestionsRepository.save(interviewQuestions);
        try{
            ResponseEntity<String> response = retryGeminiPostForString(request, url, 3, 2000); // 3 retries, 2 seconds delay

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                return List.of("No questions generated.");
            }

            String questionsText = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            System.out.println("\nIn service :"+company+role+questionType+experience);
            interviewQuestions.setResponse(questionsText);
            interviewQuestionsRepository.save(interviewQuestions);// <== Add this line

            // Split questions by lines or numbering
            return Arrays.stream(questionsText.split("(?m)^\\s*\\d+\\.|\\n"))
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();
        }catch (Exception e){
            return List.of("Error: "+e.getMessage());
        }
    }

    public String getImprovedAnswer( String question, String userAnswer) throws Exception {
        String prompt = String.format(
                "The following is a candidate's response to a common interview question. Please help improve the answer to sound more clear, confident, and professional — the kind of response someone can give in a real HR interview.\n\n" +
                        "Question: %s\n" +
                        "Answer: %s\n\n" +
                        "Return only the improved answer in this JSON format — no extra text, no explanation:\n\n" +
                        "{ \"answer\": \"Your improved and professional answer here.\" }\n\n" +
                        "Keep the language simple and easy to understand. Avoid complex words or jargon.",
                question, userAnswer
        );
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        requestBody.put("contents", List.of(Map.of("parts", List.of(parts))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        ResponseEntity<JsonNode> response = retryGeminiPost(request, url, 3, 3000); // 3 retries, 2 seconds delay
        String fullText = response.getBody()
                .path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Extract JSON portion from response text
        int start = fullText.indexOf("{");
        int end = fullText.lastIndexOf("}");
        if (start == -1 || end == -1) {
            throw new RuntimeException("AI response did not contain valid JSON.");
        }

        String jsonText = fullText.substring(start, end + 1);
        JsonNode parsed = mapper.readTree(jsonText);
        return parsed.path("answer").asText();
    }

    private ResponseEntity<JsonNode> retryGeminiPost(HttpEntity<?> requestEntity, String url, int maxRetries, int delayMs) throws InterruptedException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restTemplate.exchange(url, HttpMethod.POST, requestEntity, JsonNode.class);
            } catch (HttpServerErrorException ex) {
                // Retry on 5xx server errors
                if (i < maxRetries - 1 && (
                        ex.getStatusCode().value() == 500 ||
                                ex.getStatusCode().value() == 502 ||
                                ex.getStatusCode().value() == 503 ||
                                ex.getStatusCode().value() == 504)) {
                    System.out.println("Server error " + ex.getStatusCode() + ". Retrying attempt " + (i + 1));
                    Thread.sleep(delayMs);
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                // Don't retry other types of errors
                throw ex;
            }
        }
        throw new RuntimeException("Gemini request failed after all retries.");
    }


    private ResponseEntity<String> retryGeminiPostForString(HttpEntity<?> requestEntity, String url, int maxRetries, int delayMs) throws InterruptedException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            } catch (HttpStatusCodeException ex) {
                int statusCode = ex.getRawStatusCode();
                if (statusCode == 503 && i < maxRetries - 1) {
                    System.out.println("Attempt " + (i + 1) + " failed with 503. Retrying after " + delayMs + "ms...");
                    Thread.sleep(delayMs);
                    delayMs *= 2; // optional: exponential backoff
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                System.out.println("Unexpected error: " + ex.getMessage());
                throw ex;
            }
        }
        throw new RuntimeException("Gemini request failed after " + maxRetries + " retries.");
    }



}
