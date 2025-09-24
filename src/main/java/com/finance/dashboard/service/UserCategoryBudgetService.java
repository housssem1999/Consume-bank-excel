package com.finance.dashboard.service;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.UserCategoryBudget;
import com.finance.dashboard.repository.UserCategoryBudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserCategoryBudgetService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCategoryBudgetService.class);
    
    private final UserCategoryBudgetRepository userCategoryBudgetRepository;
    
    public UserCategoryBudgetService(UserCategoryBudgetRepository userCategoryBudgetRepository) {
        this.userCategoryBudgetRepository = userCategoryBudgetRepository;
    }
    
    /**
     * Get the budget for a specific user and category.
     * For system categories, looks in user_category_budgets table.
     * For user categories, uses the category's monthlyBudget field.
     */
    public BigDecimal getBudgetForUserAndCategory(User user, Category category) {
        if (category.isSystemCategory()) {
            // For system categories, check user-specific budget mapping
            Optional<UserCategoryBudget> userBudget = userCategoryBudgetRepository.findByUserAndCategory(user, category);
            return userBudget.map(UserCategoryBudget::getMonthlyBudget).orElse(BigDecimal.ZERO);
        } else {
            // For user categories, use the category's own budget
            return category.getMonthlyBudget() != null ? category.getMonthlyBudget() : BigDecimal.ZERO;
        }
    }
    
    /**
     * Set or update the budget for a user and category.
     */
    public UserCategoryBudget setBudgetForUserAndCategory(User user, Category category, BigDecimal budget) {
        if (category.isSystemCategory()) {
            // For system categories, use user-category budget mapping
            Optional<UserCategoryBudget> existingBudget = userCategoryBudgetRepository.findByUserAndCategory(user, category);
            
            if (existingBudget.isPresent()) {
                UserCategoryBudget userBudget = existingBudget.get();
                userBudget.setMonthlyBudget(budget);
                logger.info("Updated budget for user {} and system category {}: {}", 
                    user.getId(), category.getName(), budget);
                return userCategoryBudgetRepository.save(userBudget);
            } else {
                UserCategoryBudget newUserBudget = new UserCategoryBudget(user, category, budget);
                logger.info("Created new budget for user {} and system category {}: {}", 
                    user.getId(), category.getName(), budget);
                return userCategoryBudgetRepository.save(newUserBudget);
            }
        } else {
            // For user categories, update the category's budget directly
            category.setMonthlyBudget(budget);
            logger.info("Updated budget for user category {}: {}", category.getName(), budget);
            // Note: We don't save the category here as it should be saved by the calling service
            return null;
        }
    }
    
    /**
     * Get all budgets for a user.
     */
    public List<UserCategoryBudget> getAllBudgetsForUser(User user) {
        return userCategoryBudgetRepository.findByUser(user);
    }
    
    /**
     * Remove budget for a user and category.
     */
    public void removeBudgetForUserAndCategory(User user, Category category) {
        if (category.isSystemCategory()) {
            Optional<UserCategoryBudget> existingBudget = userCategoryBudgetRepository.findByUserAndCategory(user, category);
            if (existingBudget.isPresent()) {
                userCategoryBudgetRepository.delete(existingBudget.get());
                logger.info("Removed budget for user {} and system category {}", 
                    user.getId(), category.getName());
            }
        } else {
            // For user categories, set budget to zero
            category.setMonthlyBudget(BigDecimal.ZERO);
            logger.info("Reset budget for user category {} to zero", category.getName());
        }
    }
    
    /**
     * Check if a user has a budget set for a category.
     */
    public boolean hasBudgetForUserAndCategory(User user, Category category) {
        if (category.isSystemCategory()) {
            return userCategoryBudgetRepository.existsByUserAndCategory(user, category);
        } else {
            return category.getMonthlyBudget() != null && category.getMonthlyBudget().compareTo(BigDecimal.ZERO) > 0;
        }
    }
}