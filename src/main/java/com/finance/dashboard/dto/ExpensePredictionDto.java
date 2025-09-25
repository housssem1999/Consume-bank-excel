package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpensePredictionDto {
    private LocalDate predictionDate;
    private BigDecimal predictedAmount;
    private String categoryName;
    private Double confidence;
    private List<String> insights;
    private String predictionModel;
    
    public ExpensePredictionDto() {}
    
    public ExpensePredictionDto(LocalDate predictionDate, BigDecimal predictedAmount, String categoryName, Double confidence) {
        this.predictionDate = predictionDate;
        this.predictedAmount = predictedAmount;
        this.categoryName = categoryName;
        this.confidence = confidence;
    }
    
    public LocalDate getPredictionDate() {
        return predictionDate;
    }
    
    public void setPredictionDate(LocalDate predictionDate) {
        this.predictionDate = predictionDate;
    }
    
    public BigDecimal getPredictedAmount() {
        return predictedAmount;
    }
    
    public void setPredictedAmount(BigDecimal predictedAmount) {
        this.predictedAmount = predictedAmount;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public List<String> getInsights() {
        return insights;
    }
    
    public void setInsights(List<String> insights) {
        this.insights = insights;
    }
    
    public String getPredictionModel() {
        return predictionModel;
    }
    
    public void setPredictionModel(String predictionModel) {
        this.predictionModel = predictionModel;
    }
}