package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonthlyTrendDto {
    
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal netAmount;
    
    // Constructors
    public MonthlyTrendDto() {}
    
    public MonthlyTrendDto(Integer year, Integer month, BigDecimal income, BigDecimal expenses) {
        this.year = year;
        this.month = month;
        this.income = income;
        this.expenses = expenses;
        this.netAmount = income.subtract(expenses.abs());
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
    
    public BigDecimal getIncome() {
        return income;
    }
    
    public void setIncome(BigDecimal income) {
        this.income = income;
    }
    
    public BigDecimal getExpenses() {
        return expenses;
    }
    
    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }
    
    public BigDecimal getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
    
    public BigDecimal getSavingsRate() {
        if (income == null || income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // Calculate savings rate as (netAmount / income) * 100
        return netAmount.divide(income, 4, RoundingMode.HALF_UP)
                       .multiply(BigDecimal.valueOf(100));
    }
    
    private String getMonthName(Integer month) {
        if (month == null) return "";
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }
}
