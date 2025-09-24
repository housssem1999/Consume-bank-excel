package com.finance.dashboard.dto;

import java.math.BigDecimal;

public class CategoryWithBudgetDto {
    private Long id;
    private String name;
    private String description;
    private String color;
    private BigDecimal monthlyBudget;
    private boolean isSystemCategory;
    private boolean hasBudget;
    
    public CategoryWithBudgetDto() {}
    
    public CategoryWithBudgetDto(Long id, String name, String description, String color, 
                                BigDecimal monthlyBudget, boolean isSystemCategory, boolean hasBudget) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.monthlyBudget = monthlyBudget;
        this.isSystemCategory = isSystemCategory;
        this.hasBudget = hasBudget;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }
    
    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
    
    public boolean isSystemCategory() {
        return isSystemCategory;
    }
    
    public void setSystemCategory(boolean systemCategory) {
        isSystemCategory = systemCategory;
    }
    
    public boolean isHasBudget() {
        return hasBudget;
    }
    
    public void setHasBudget(boolean hasBudget) {
        this.hasBudget = hasBudget;
    }
    
    @Override
    public String toString() {
        return "CategoryWithBudgetDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", monthlyBudget=" + monthlyBudget +
                ", isSystemCategory=" + isSystemCategory +
                ", hasBudget=" + hasBudget +
                '}';
    }
}