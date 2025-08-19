package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FinancialSummaryDto {
    
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netIncome;
    private Long totalTransactions;
    private List<CategorySummaryDto> expensesByCategory;
    private List<CategorySummaryDto> incomeByCategory;
    private List<MonthlyTrendDto> monthlyTrends;
    private ForecastDto forecast;
    
    // Constructors
    public FinancialSummaryDto() {}
    
    public FinancialSummaryDto(BigDecimal totalIncome, BigDecimal totalExpenses, 
                              BigDecimal netIncome, Long totalTransactions) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netIncome = netIncome;
        this.totalTransactions = totalTransactions;
    }
    
    // Getters and Setters
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }
    
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }
    
    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }
    
    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
    
    public BigDecimal getNetIncome() {
        return netIncome;
    }
    
    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }
    
    public Long getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
    
    public List<CategorySummaryDto> getExpensesByCategory() {
        return expensesByCategory;
    }
    
    public void setExpensesByCategory(List<CategorySummaryDto> expensesByCategory) {
        this.expensesByCategory = expensesByCategory;
    }
    
    public List<CategorySummaryDto> getIncomeByCategory() {
        return incomeByCategory;
    }
    
    public void setIncomeByCategory(List<CategorySummaryDto> incomeByCategory) {
        this.incomeByCategory = incomeByCategory;
    }
    
    public List<MonthlyTrendDto> getMonthlyTrends() {
        return monthlyTrends;
    }
    
    public void setMonthlyTrends(List<MonthlyTrendDto> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
    
    public ForecastDto getForecast() {
        return forecast;
    }
    
    public void setForecast(ForecastDto forecast) {
        this.forecast = forecast;
    }
}
