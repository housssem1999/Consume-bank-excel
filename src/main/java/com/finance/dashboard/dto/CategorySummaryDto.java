package com.finance.dashboard.dto;

import java.math.BigDecimal;

public class CategorySummaryDto {
    
    private String categoryName;
    private BigDecimal totalAmount;
    private Double percentage;
    private String color;
    
    // Constructors
    public CategorySummaryDto() {}
    
    public CategorySummaryDto(String categoryName, BigDecimal totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }
    
    public CategorySummaryDto(String categoryName, BigDecimal totalAmount, Double percentage, String color) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.color = color;
    }
    
    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
}
