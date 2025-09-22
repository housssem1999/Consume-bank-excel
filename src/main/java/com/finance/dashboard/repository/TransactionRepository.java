package com.finance.dashboard.repository;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
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
    
    // PostgreSQL and H2 compatible query using EXTRACT(DOW FROM date)
    // PostgreSQL: DOW returns 0=Sunday, 1=Monday, ..., 6=Saturday
    // H2: DOW returns 0=Sunday, 1=Monday, ..., 6=Saturday (same as PostgreSQL)
    @Query(value = "SELECT c.name, EXTRACT(DOW FROM t.date), SUM(t.amount) FROM transactions t " +
           "JOIN categories c ON c.id = t.category_id " +
           "WHERE t.type = ?1 AND t.date BETWEEN ?2 AND ?3 " +
           "GROUP BY c.name, EXTRACT(DOW FROM t.date) " +
           "ORDER BY c.name, EXTRACT(DOW FROM t.date)", nativeQuery = true)
    List<Object[]> findExpenseHeatmapData(@Param("type") String type,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
