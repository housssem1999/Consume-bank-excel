package com.finance.dashboard.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_category_budgets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"}))
public class UserCategoryBudget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal monthlyBudget;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Constructors
    public UserCategoryBudget() {}
    
    public UserCategoryBudget(User user, Category category, BigDecimal monthlyBudget) {
        this.user = user;
        this.category = category;
        this.monthlyBudget = monthlyBudget;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }
    
    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserCategoryBudget{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", categoryId=" + (category != null ? category.getId() : null) +
                ", monthlyBudget=" + monthlyBudget +
                ", updatedAt=" + updatedAt +
                '}';
    }
}