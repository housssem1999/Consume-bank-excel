package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RecurringTransactionDto {
    
    private String merchant;
    private String description;
    private BigDecimal averageAmount;
    private String frequency; // "MONTHLY", "WEEKLY"
    private Integer intervalDays;
    private Integer transactionCount;
    private LocalDate firstTransaction;
    private LocalDate lastTransaction;
    private LocalDate nextExpectedDate;
    private List<LocalDate> transactionDates;
    private String categoryName;
    
    // Constructors
    public RecurringTransactionDto() {}
    
    public RecurringTransactionDto(String merchant, String description, BigDecimal averageAmount, 
                                 String frequency, Integer intervalDays, Integer transactionCount,
                                 LocalDate firstTransaction, LocalDate lastTransaction, 
                                 LocalDate nextExpectedDate, List<LocalDate> transactionDates,
                                 String categoryName) {
        this.merchant = merchant;
        this.description = description;
        this.averageAmount = averageAmount;
        this.frequency = frequency;
        this.intervalDays = intervalDays;
        this.transactionCount = transactionCount;
        this.firstTransaction = firstTransaction;
        this.lastTransaction = lastTransaction;
        this.nextExpectedDate = nextExpectedDate;
        this.transactionDates = transactionDates;
        this.categoryName = categoryName;
    }
    
    // Getters and Setters
    public String getMerchant() {
        return merchant;
    }
    
    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAverageAmount() {
        return averageAmount;
    }
    
    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public Integer getIntervalDays() {
        return intervalDays;
    }
    
    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    public LocalDate getFirstTransaction() {
        return firstTransaction;
    }
    
    public void setFirstTransaction(LocalDate firstTransaction) {
        this.firstTransaction = firstTransaction;
    }
    
    public LocalDate getLastTransaction() {
        return lastTransaction;
    }
    
    public void setLastTransaction(LocalDate lastTransaction) {
        this.lastTransaction = lastTransaction;
    }
    
    public LocalDate getNextExpectedDate() {
        return nextExpectedDate;
    }
    
    public void setNextExpectedDate(LocalDate nextExpectedDate) {
        this.nextExpectedDate = nextExpectedDate;
    }
    
    public List<LocalDate> getTransactionDates() {
        return transactionDates;
    }
    
    public void setTransactionDates(List<LocalDate> transactionDates) {
        this.transactionDates = transactionDates;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}