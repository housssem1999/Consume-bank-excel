package com.finance.dashboard.dto;

import java.math.BigDecimal;

public class NetWorthTrendDto {
    
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal monthlyNetIncome;
    private BigDecimal cumulativeNetWorth;
    private BigDecimal netWorthChange;
    private String trendDirection; // "up", "down", "flat"
    
    // Constructors
    public NetWorthTrendDto() {}
    
    public NetWorthTrendDto(Integer year, Integer month, BigDecimal monthlyNetIncome, 
                           BigDecimal cumulativeNetWorth, BigDecimal netWorthChange) {
        this.year = year;
        this.month = month;
        this.monthlyNetIncome = monthlyNetIncome;
        this.cumulativeNetWorth = cumulativeNetWorth;
        this.netWorthChange = netWorthChange;
        this.monthName = getMonthName(month);
        this.trendDirection = calculateTrendDirection(netWorthChange);
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
    
    public BigDecimal getMonthlyNetIncome() {
        return monthlyNetIncome;
    }
    
    public void setMonthlyNetIncome(BigDecimal monthlyNetIncome) {
        this.monthlyNetIncome = monthlyNetIncome;
    }
    
    public BigDecimal getCumulativeNetWorth() {
        return cumulativeNetWorth;
    }
    
    public void setCumulativeNetWorth(BigDecimal cumulativeNetWorth) {
        this.cumulativeNetWorth = cumulativeNetWorth;
    }
    
    public BigDecimal getNetWorthChange() {
        return netWorthChange;
    }
    
    public void setNetWorthChange(BigDecimal netWorthChange) {
        this.netWorthChange = netWorthChange;
        this.trendDirection = calculateTrendDirection(netWorthChange);
    }
    
    public String getTrendDirection() {
        return trendDirection;
    }
    
    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }
    
    private String getMonthName(Integer month) {
        if (month == null) return "";
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }
    
    private String calculateTrendDirection(BigDecimal change) {
        if (change == null) return "flat";
        int comparison = change.compareTo(BigDecimal.ZERO);
        if (comparison > 0) return "up";
        if (comparison < 0) return "down";
        return "flat";
    }
}