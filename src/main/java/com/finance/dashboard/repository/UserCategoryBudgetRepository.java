package com.finance.dashboard.repository;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.UserCategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCategoryBudgetRepository extends JpaRepository<UserCategoryBudget, Long> {
    
    Optional<UserCategoryBudget> findByUserAndCategory(User user, Category category);
    
    List<UserCategoryBudget> findByUser(User user);
    
    List<UserCategoryBudget> findByCategory(Category category);
    
    @Query("SELECT ucb FROM UserCategoryBudget ucb WHERE ucb.user = :user AND ucb.category.id IN :categoryIds")
    List<UserCategoryBudget> findByUserAndCategoryIds(@Param("user") User user, @Param("categoryIds") List<Long> categoryIds);
    
    boolean existsByUserAndCategory(User user, Category category);
    
    void deleteByUserAndCategory(User user, Category category);
}