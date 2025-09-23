package com.finance.dashboard.repository;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    // User-specific categories
    List<Category> findByUser(User user);
    
    Optional<Category> findByUserAndName(User user, String name);
    
    boolean existsByUserAndName(User user, String name);
    
    // System categories (user is null)
    @Query("SELECT c FROM Category c WHERE c.user IS NULL")
    List<Category> findSystemCategories();
    
    // Get all categories available to a user (system + user's custom categories)
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user ORDER BY c.name")
    List<Category> findAvailableCategoriesForUser(@Param("user") User user);
}
