package com.finance.dashboard.dto;

import java.math.BigDecimal;

public class BudgetComparisonDto {
    
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private Double percentageDifference; // Positive = over budget, Negative = under budget
    private String statusColor; // "green" for under budget, "red" for over budget
    private String categoryColor; // Original category color
    
    // Constructors
    public BudgetComparisonDto() {}
    
    public BudgetComparisonDto(String categoryName, BigDecimal budgetAmount, BigDecimal actualAmount) {
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.actualAmount = actualAmount;
        this.calculatePercentageDifference();
        this.setStatusColor();
    }
    
    public BudgetComparisonDto(String categoryName, BigDecimal budgetAmount, BigDecimal actualAmount, String categoryColor) {
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.actualAmount = actualAmount;
        this.categoryColor = categoryColor;
        this.calculatePercentageDifference();
        this.setStatusColor();
    }
    
    // Calculate percentage difference
    private void calculatePercentageDifference() {
        if (budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal difference = actualAmount.abs().subtract(budgetAmount);
            this.percentageDifference = difference.divide(budgetAmount, 4, java.math.RoundingMode.HALF_UP)
                                     .multiply(BigDecimal.valueOf(100)).doubleValue();
        } else {
            this.percentageDifference = 0.0;
        }
    }
    
    // Set status color based on budget performance
    private void setStatusColor() {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.statusColor = "#1890ff"; // Blue for no budget set
        } else if (actualAmount.abs().compareTo(budgetAmount) <= 0) {
            this.statusColor = "#52c41a"; // Green for under or on budget
        } else {
            this.statusColor = "#ff4d4f"; // Red for over budget
        }
    }
    
    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }
    
    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
        this.calculatePercentageDifference();
        this.setStatusColor();
    }
    
    public BigDecimal getActualAmount() {
        return actualAmount;
    }
    
    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
        this.calculatePercentageDifference();
        this.setStatusColor();
    }
    
    public Double getPercentageDifference() {
        return percentageDifference;
    }
    
    public void setPercentageDifference(Double percentageDifference) {
        this.percentageDifference = percentageDifference;
    }
    
    public String getStatusColor() {
        return statusColor;
    }
    
    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
    
    public String getCategoryColor() {
        return categoryColor;
    }
    
    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }
    
    // Helper methods
    public boolean isOverBudget() {
        return budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0 
               && actualAmount.abs().compareTo(budgetAmount) > 0;
    }
    
    public boolean isUnderBudget() {
        return budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0 
               && actualAmount.abs().compareTo(budgetAmount) <= 0;
    }
    
    public boolean hasBudget() {
        return budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}