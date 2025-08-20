package com.finance.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class HeatmapDataDto {
    
    @JsonProperty("category")
    private String categoryName;
    
    @JsonProperty("dayOfWeek")
    private String dayOfWeek;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    // Constructors
    public HeatmapDataDto() {}
    
    public HeatmapDataDto(String categoryName, String dayOfWeek, BigDecimal amount) {
        this.categoryName = categoryName;
        this.dayOfWeek = dayOfWeek;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return "HeatmapDataDto{" +
                "categoryName='" + categoryName + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", amount=" + amount +
                '}';
    }
}