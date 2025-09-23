package com.finance.dashboard.controller;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.CategoryService;
import com.finance.dashboard.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {
    
    @Autowired
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody CategoryRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            Category category = categoryService.createCategory(
                request.getName(), 
                request.getDescription(), 
                request.getColor(),
                currentUser
            );
            
            response.put("success", true);
            response.put("message", "Category created successfully");
            response.put("category", category);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Inner class for request body
    public static class CategoryRequest {
        private String name;
        private String description;
        private String color;
        
        // Getters and setters
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
    }
    
    @PutMapping("/{id}/budget")
    public ResponseEntity<Map<String, Object>> updateCategoryBudget(
            @PathVariable Long id,
            @RequestBody BudgetUpdateRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(id);
            if (categoryOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Category not found");
                return ResponseEntity.notFound().build();
            }
            
            Category category = categoryOpt.get();
            category.setMonthlyBudget(request.getMonthlyBudget());
            Category updatedCategory = categoryService.saveCategory(category);
            
            response.put("success", true);
            response.put("message", "Budget updated successfully");
            response.put("category", updatedCategory);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating budget: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Category updatedCategory = categoryService.updateCategory(
                id, 
                request.getName(), 
                request.getDescription(), 
                request.getColor()
            );
            
            response.put("success", true);
            response.put("message", "Category updated successfully");
            response.put("category", updatedCategory);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            categoryService.deleteCategory(id);
            
            response.put("success", true);
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}/transaction-count")
    public ResponseEntity<Map<String, Object>> getCategoryTransactionCount(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = categoryService.getTransactionCount(id);
            response.put("success", true);
            response.put("transactionCount", count);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting transaction count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Inner class for budget update request
    public static class BudgetUpdateRequest {
        private BigDecimal monthlyBudget;
        
        public BigDecimal getMonthlyBudget() {
            return monthlyBudget;
        }
        
        public void setMonthlyBudget(BigDecimal monthlyBudget) {
            this.monthlyBudget = monthlyBudget;
        }
    }
}
