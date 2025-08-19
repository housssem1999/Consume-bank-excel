package com.finance.dashboard.dto;

import java.math.BigDecimal;

public class ForecastDto {
    
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal projectedIncome;
    private BigDecimal projectedExpenses;
    private BigDecimal projectedNetAmount;
    private BigDecimal confidenceLowerBound;
    private BigDecimal confidenceUpperBound;
    private String forecastMethod; // "LINEAR_REGRESSION" or "MOVING_AVERAGE"
    
    // Constructors
    public ForecastDto() {}
    
    public ForecastDto(Integer year, Integer month, BigDecimal projectedIncome, 
                       BigDecimal projectedExpenses, BigDecimal confidenceLowerBound, 
                       BigDecimal confidenceUpperBound, String forecastMethod) {
        this.year = year;
        this.month = month;
        this.projectedIncome = projectedIncome;
        this.projectedExpenses = projectedExpenses;
        this.projectedNetAmount = projectedIncome.subtract(projectedExpenses.abs());
        this.confidenceLowerBound = confidenceLowerBound;
        this.confidenceUpperBound = confidenceUpperBound;
        this.forecastMethod = forecastMethod;
        this.monthName = getMonthName(month);
    }
    
    // Getters and Setters
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
        this.monthName = getMonthName(month);
    }
    
    public String getMonthName() {
        return monthName;
    }
    
    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }
    
    public BigDecimal getProjectedIncome() {
        return projectedIncome;
    }
    
    public void setProjectedIncome(BigDecimal projectedIncome) {
        this.projectedIncome = projectedIncome;
    }
    
    public BigDecimal getProjectedExpenses() {
        return projectedExpenses;
    }
    
    public void setProjectedExpenses(BigDecimal projectedExpenses) {
        this.projectedExpenses = projectedExpenses;
    }
    
    public BigDecimal getProjectedNetAmount() {
        return projectedNetAmount;
    }
    
    public void setProjectedNetAmount(BigDecimal projectedNetAmount) {
        this.projectedNetAmount = projectedNetAmount;
    }
    
    public BigDecimal getConfidenceLowerBound() {
        return confidenceLowerBound;
    }
    
    public void setConfidenceLowerBound(BigDecimal confidenceLowerBound) {
        this.confidenceLowerBound = confidenceLowerBound;
    }
    
    public BigDecimal getConfidenceUpperBound() {
        return confidenceUpperBound;
    }
    
    public void setConfidenceUpperBound(BigDecimal confidenceUpperBound) {
        this.confidenceUpperBound = confidenceUpperBound;
    }
    
    public String getForecastMethod() {
        return forecastMethod;
    }
    
    public void setForecastMethod(String forecastMethod) {
        this.forecastMethod = forecastMethod;
    }
    
    private String getMonthName(Integer month) {
        if (month == null) return "";
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }
}