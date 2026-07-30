package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class SpendingInsightDto {
    private String insightType;
    private String title;
    private String description;
    private String severity; // LOW, MEDIUM, HIGH
    private BigDecimal impact;
    private List<String> recommendations;
    private String category;
    private Double confidence;
    
    public SpendingInsightDto() {}
    
    public SpendingInsightDto(String insightType, String title, String description, String severity) {
        this.insightType = insightType;
        this.title = title;
        this.description = description;
        this.severity = severity;
    }
    
    public String getInsightType() {
        return insightType;
    }
    
    public void setInsightType(String insightType) {
        this.insightType = insightType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public BigDecimal getImpact() {
        return impact;
    }
    
    public void setImpact(BigDecimal impact) {
        this.impact = impact;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}