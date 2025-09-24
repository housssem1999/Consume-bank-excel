package com.finance.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String name;
    
    private String description;
    
    private String color; // For UI visualization
    
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyBudget; // Monthly budget for this category
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> transactions = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for system default categories
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCategoryBudget> userBudgets = new ArrayList<>();
    
    // Constructors
    public Category() {}
    
    public Category(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.monthlyBudget = BigDecimal.ZERO; // Default budget to zero
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
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }
    
    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isSystemCategory() {
        return user == null;
    }
    
    public List<UserCategoryBudget> getUserBudgets() {
        return userBudgets;
    }
    
    public void setUserBudgets(List<UserCategoryBudget> userBudgets) {
        this.userBudgets = userBudgets;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", monthlyBudget=" + monthlyBudget +
                '}';
    }
}
