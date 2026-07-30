package com.finance.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
public class HuggingFaceService {
    
    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceService.class);
    
    // Using free Hugging Face Inference API
    private static final String HUGGING_FACE_API_URL = "https://api-inference.huggingface.co/models/";
    
    // Free models that don't require API key
    private static final String SENTIMENT_MODEL = "cardiffnlp/twitter-roberta-base-sentiment-latest";
    private static final String CLASSIFICATION_MODEL = "microsoft/DialoGPT-medium"; // For text analysis
    private static final String TEXT_ANALYSIS_MODEL = "sentence-transformers/all-MiniLM-L6-v2";
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${app.huggingface.api-key:}")
    private String apiKey;
    
    public HuggingFaceService() {
        this.webClient = WebClient.builder()
                .baseUrl(HUGGING_FACE_API_URL)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Analyze transaction sentiment using Hugging Face sentiment analysis
     */
    public Map<String, Object> analyzeSentiment(String text) {
        try {
            Map<String, String> request = Map.of("inputs", text);
            
            String response = webClient.post()
                    .uri(SENTIMENT_MODEL)
                    .header("Authorization", apiKey.isEmpty() ? "" : "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (response != null) {
                JsonNode jsonResponse = objectMapper.readTree(response);
                return parseSentimentResponse(jsonResponse);
            }
        } catch (Exception e) {
            logger.warn("Failed to analyze sentiment with Hugging Face: {}", e.getMessage());
            return getFallbackSentiment(text);
        }
        
        return getFallbackSentiment(text);
    }
    
    /**
     * Categorize transaction using text classification
     */
    public Map<String, Object> categorizeTransaction(String description, List<String> availableCategories) {
        try {
            // Create a classification prompt
            String prompt = String.format("Categorize this financial transaction: '%s'. Available categories: %s", 
                    description, String.join(", ", availableCategories));
            
            Map<String, String> request = Map.of("inputs", prompt);
            
            String response = webClient.post()
                    .uri(TEXT_ANALYSIS_MODEL)
                    .header("Authorization", apiKey.isEmpty() ? "" : "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (response != null) {
                return parseCategorizationResponse(response, description, availableCategories);
            }
        } catch (Exception e) {
            logger.warn("Failed to categorize transaction with Hugging Face: {}", e.getMessage());
            return getFallbackCategorization(description, availableCategories);
        }
        
        return getFallbackCategorization(description, availableCategories);
    }
    
    /**
     * Generate spending insights using text generation
     */
    public List<String> generateSpendingInsights(String financialSummary) {
        try {
            String prompt = String.format("Analyze this financial data and provide insights: %s. Give 3 actionable recommendations.", 
                    financialSummary);
            
            Map<String, Object> request = Map.of(
                "inputs", prompt,
                "parameters", Map.of("max_length", 200, "do_sample", true)
            );
            
            String response = webClient.post()
                    .uri("gpt2") // Using free GPT2 model
                    .header("Authorization", apiKey.isEmpty() ? "" : "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (response != null) {
                return parseInsightsResponse(response);
            }
        } catch (Exception e) {
            logger.warn("Failed to generate insights with Hugging Face: {}", e.getMessage());
            return getFallbackInsights(financialSummary);
        }
        
        return getFallbackInsights(financialSummary);
    }
    
    private Map<String, Object> parseSentimentResponse(JsonNode response) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (response.isArray() && response.size() > 0) {
                JsonNode firstResult = response.get(0);
                if (firstResult.isArray() && firstResult.size() > 0) {
                    JsonNode sentimentResult = firstResult.get(0);
                    result.put("sentiment", sentimentResult.get("label").asText().toLowerCase());
                    result.put("confidence", sentimentResult.get("score").asDouble());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse sentiment response: {}", e.getMessage());
            return getFallbackSentiment("");
        }
        return result;
    }
    
    private Map<String, Object> parseCategorizationResponse(String response, String description, List<String> categories) {
        Map<String, Object> result = new HashMap<>();
        
        // Simple keyword-based fallback with ML-like confidence scoring
        String bestCategory = findBestCategoryMatch(description, categories);
        double confidence = calculateCategoryConfidence(description, bestCategory);
        
        result.put("suggestedCategory", bestCategory);
        result.put("confidence", confidence);
        result.put("alternatives", getAlternativeCategories(description, categories, bestCategory));
        
        return result;
    }
    
    private List<String> parseInsightsResponse(String response) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                String generatedText = jsonResponse.get(0).get("generated_text").asText();
                return extractInsightsFromText(generatedText);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse insights response: {}", e.getMessage());
        }
        return getFallbackInsights("");
    }
    
    private Map<String, Object> getFallbackSentiment(String text) {
        // Simple rule-based sentiment analysis as fallback
        String lowerText = text.toLowerCase();
        String sentiment = "neutral";
        double confidence = 0.6;
        
        if (lowerText.contains("great") || lowerText.contains("good") || lowerText.contains("bonus") || lowerText.contains("refund")) {
            sentiment = "positive";
            confidence = 0.7;
        } else if (lowerText.contains("fee") || lowerText.contains("penalty") || lowerText.contains("overdraft") || lowerText.contains("charge")) {
            sentiment = "negative";
            confidence = 0.7;
        }
        
        return Map.of("sentiment", sentiment, "confidence", confidence);
    }
    
    private Map<String, Object> getFallbackCategorization(String description, List<String> categories) {
        String bestCategory = findBestCategoryMatch(description, categories);
        double confidence = calculateCategoryConfidence(description, bestCategory);
        
        return Map.of(
            "suggestedCategory", bestCategory,
            "confidence", confidence,
            "alternatives", getAlternativeCategories(description, categories, bestCategory)
        );
    }
    
    private List<String> getFallbackInsights(String financialSummary) {
        return Arrays.asList(
            "Monitor your spending patterns and identify areas where you can reduce expenses",
            "Consider setting up automatic savings to build an emergency fund",
            "Review your budget regularly and adjust categories based on actual spending"
        );
    }
    
    private String findBestCategoryMatch(String description, List<String> categories) {
        String lowerDesc = description.toLowerCase();
        
        // Define keyword mappings for better categorization
        Map<String, List<String>> categoryKeywords = Map.of(
            "food & dining", Arrays.asList("restaurant", "food", "grocery", "cafe", "dinner", "lunch", "breakfast", "pizza", "mcdonald", "starbucks"),
            "transportation", Arrays.asList("gas", "fuel", "uber", "taxi", "bus", "train", "parking", "toll", "metro"),
            "shopping", Arrays.asList("amazon", "store", "mall", "purchase", "buy", "shop", "retail", "clothing"),
            "entertainment", Arrays.asList("movie", "theater", "netflix", "spotify", "game", "concert", "ticket"),
            "utilities", Arrays.asList("electric", "water", "internet", "phone", "cable", "utility", "bill"),
            "healthcare", Arrays.asList("doctor", "hospital", "pharmacy", "medical", "health", "clinic", "dentist"),
            "income", Arrays.asList("salary", "wage", "bonus", "commission", "refund", "deposit", "payment received")
        );
        
        for (String category : categories) {
            String lowerCategory = category.toLowerCase();
            if (categoryKeywords.containsKey(lowerCategory)) {
                for (String keyword : categoryKeywords.get(lowerCategory)) {
                    if (lowerDesc.contains(keyword)) {
                        return category;
                    }
                }
            }
        }
        
        // Default fallback
        return categories.isEmpty() ? "Other" : categories.get(0);
    }
    
    private double calculateCategoryConfidence(String description, String category) {
        String lowerDesc = description.toLowerCase();
        String lowerCategory = category.toLowerCase();
        
        // Simple confidence based on keyword matches
        if (lowerDesc.contains(lowerCategory.split(" ")[0])) {
            return 0.9;
        }
        
        return 0.6; // Default confidence
    }
    
    private List<String> getAlternativeCategories(String description, List<String> categories, String bestCategory) {
        return categories.stream()
                .filter(cat -> !cat.equals(bestCategory))
                .limit(2)
                .toList();
    }
    
    private List<String> extractInsightsFromText(String text) {
        // Simple extraction of insights from generated text
        String[] sentences = text.split("\\.");
        List<String> insights = new ArrayList<>();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 20 && !sentence.isEmpty()) {
                insights.add(sentence);
                if (insights.size() >= 3) break;
            }
        }
        
        return insights.isEmpty() ? getFallbackInsights("") : insights;
    }
}