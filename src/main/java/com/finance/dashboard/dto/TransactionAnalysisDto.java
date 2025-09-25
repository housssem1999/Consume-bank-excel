package com.finance.dashboard.dto;

import java.util.List;
import java.util.Map;

public class TransactionAnalysisDto {
    private String transactionDescription;
    private String suggestedCategory;
    private Double confidence;
    private List<String> alternativeCategories;
    private Map<String, Double> categoryProbabilities;
    private String sentiment;
    private Double sentimentScore;
    private List<String> extractedKeywords;
    
    public TransactionAnalysisDto() {}
    
    public TransactionAnalysisDto(String transactionDescription, String suggestedCategory, Double confidence) {
        this.transactionDescription = transactionDescription;
        this.suggestedCategory = suggestedCategory;
        this.confidence = confidence;
    }
    
    public String getTransactionDescription() {
        return transactionDescription;
    }
    
    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }
    
    public String getSuggestedCategory() {
        return suggestedCategory;
    }
    
    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public List<String> getAlternativeCategories() {
        return alternativeCategories;
    }
    
    public void setAlternativeCategories(List<String> alternativeCategories) {
        this.alternativeCategories = alternativeCategories;
    }
    
    public Map<String, Double> getCategoryProbabilities() {
        return categoryProbabilities;
    }
    
    public void setCategoryProbabilities(Map<String, Double> categoryProbabilities) {
        this.categoryProbabilities = categoryProbabilities;
    }
    
    public String getSentiment() {
        return sentiment;
    }
    
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
    
    public Double getSentimentScore() {
        return sentimentScore;
    }
    
    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }
    
    public List<String> getExtractedKeywords() {
        return extractedKeywords;
    }
    
    public void setExtractedKeywords(List<String> extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }
}