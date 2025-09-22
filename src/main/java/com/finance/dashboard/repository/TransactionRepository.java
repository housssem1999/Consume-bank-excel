package com.finance.dashboard.repository;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // User-aware methods
    Page<Transaction> findByUser(User user, Pageable pageable);
    
    Long countByUser(User user);
    
    Page<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<Transaction> findByUserAndType(User user, TransactionType type);
    
    List<Transaction> findByUserAndDateBetweenAndType(User user, LocalDate startDate, LocalDate endDate, TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.category.id = :categoryId")
    List<Transaction> findByUserAndCategoryId(@Param("user") User user, @Param("categoryId") Long categoryId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateBetween(@Param("user") User user, @Param("type") TransactionType type, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategoryTotalsByUserAndTypeAndDateBetween(@Param("user") User user, @Param("type") TransactionType type,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT YEAR(t.date), MONTH(t.date), SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type " +
           "GROUP BY YEAR(t.date), MONTH(t.date) " +
           "ORDER BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> findMonthlyTotalsByUserAndType(@Param("user") User user, @Param("type") TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate")
    Long countTransactionsByUserAndBetweenDates(@Param("user") User user, @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t.category.name, DAYOFWEEK(t.date), SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.name, DAYOFWEEK(t.date) " +
           "ORDER BY t.category.name, DAYOFWEEK(t.date)")
    List<Object[]> findExpenseHeatmapDataByUser(@Param("user") User user, @Param("type") TransactionType type,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    // Keep original methods for backward compatibility and system-wide queries
    Page<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByDateBetweenAndType(LocalDate startDate, LocalDate endDate, TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.category.id = :categoryId")
    List<Transaction> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateBetween(@Param("type") TransactionType type, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategoryTotalsByTypeAndDateBetween(@Param("type") TransactionType type,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT YEAR(t.date), MONTH(t.date), SUM(t.amount) FROM Transaction t " +
           "WHERE t.type = :type " +
           "GROUP BY YEAR(t.date), MONTH(t.date) " +
           "ORDER BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> findMonthlyTotalsByType(@Param("type") TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.date BETWEEN :startDate AND :endDate")
    Long countTransactionsBetweenDates(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t.category.name, DAYOFWEEK(t.date), SUM(t.amount) FROM Transaction t " +
           "WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.name, DAYOFWEEK(t.date) " +
           "ORDER BY t.category.name, DAYOFWEEK(t.date)")
    List<Object[]> findExpenseHeatmapData(@Param("type") TransactionType type,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
