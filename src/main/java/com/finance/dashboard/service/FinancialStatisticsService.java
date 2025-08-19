package com.finance.dashboard.service;

import com.finance.dashboard.dto.BudgetComparisonDto;
import com.finance.dashboard.dto.CategorySummaryDto;
import com.finance.dashboard.dto.FinancialSummaryDto;
import com.finance.dashboard.dto.MonthlyTrendDto;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialStatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialStatisticsService.class);
    
    @Autowired
    private final TransactionRepository transactionRepository;
    
    @Autowired
    private final CategoryRepository categoryRepository;

    public FinancialStatisticsService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public FinancialSummaryDto getFinancialSummary(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating financial summary from {} to {}", startDate, endDate);
        
        // Calculate totals
        BigDecimal totalIncome = getTotalByType(TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpenses = getTotalByType(TransactionType.EXPENSE, startDate, endDate);
        // Ensure expenses are negative before calculating net income
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            totalExpenses = totalExpenses.negate();
        }
        BigDecimal netIncome = totalIncome.add(totalExpenses); // expenses are now guaranteed to be negative
        Long totalTransactions = transactionRepository.countTransactionsBetweenDates(startDate, endDate);
        
        FinancialSummaryDto summary = new FinancialSummaryDto(totalIncome, totalExpenses, netIncome, totalTransactions);
        
        // Add category breakdowns
        summary.setExpensesByCategory(getCategorySummary(TransactionType.EXPENSE, startDate, endDate));
        summary.setIncomeByCategory(getCategorySummary(TransactionType.INCOME, startDate, endDate));
        
        // Add monthly trends
        summary.setMonthlyTrends(getMonthlyTrends());
        
        return summary;
    }
    
    public FinancialSummaryDto getCurrentMonthSummary() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        return getFinancialSummary(startDate, endDate);
    }
    
    public FinancialSummaryDto getCurrentYearSummary() {
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        return getFinancialSummary(startDate, endDate);
    }
    
    private BigDecimal getTotalByType(TransactionType type, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.sumAmountByTypeAndDateBetween(type, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    private List<CategorySummaryDto> getCategorySummary(TransactionType type, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.findCategoryTotalsByTypeAndDateBetween(type, startDate, endDate);
        List<CategorySummaryDto> categorySummaries = new ArrayList<>();
        
        // Calculate total for percentage calculation
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Object[] result : results) {
            BigDecimal amount = (BigDecimal) result[1];
            grandTotal = grandTotal.add(amount.abs());
        }
        
        // Create category summaries with percentages
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            
            Double percentage = 0.0;
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.abs().divide(grandTotal, 4, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100)).doubleValue();
            }
            
            CategorySummaryDto summary = new CategorySummaryDto(categoryName, amount, percentage, null);
            categorySummaries.add(summary);
        }
        
        return categorySummaries;
    }
    
    private List<MonthlyTrendDto> getMonthlyTrends() {
        List<Object[]> incomeResults = transactionRepository.findMonthlyTotalsByType(TransactionType.INCOME);
        List<Object[]> expenseResults = transactionRepository.findMonthlyTotalsByType(TransactionType.EXPENSE);
        
        // Create maps for easy lookup
        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();
        
        for (Object[] result : incomeResults) {
            Integer year = (Integer) result[0];
            Integer month = (Integer) result[1];
            BigDecimal amount = (BigDecimal) result[2];
            String key = year + "-" + month;
            incomeMap.put(key, amount);
        }
        
        for (Object[] result : expenseResults) {
            Integer year = (Integer) result[0];
            Integer month = (Integer) result[1];
            BigDecimal amount = (BigDecimal) result[2];
            String key = year + "-" + month;
            expenseMap.put(key, amount);
        }
        
        // Combine results
        List<MonthlyTrendDto> trends = new ArrayList<>();
        
        // Get all unique year-month combinations
        for (String key : incomeMap.keySet()) {
            String[] parts = key.split("-");
            Integer year = Integer.parseInt(parts[0]);
            Integer month = Integer.parseInt(parts[1]);
            
            BigDecimal income = incomeMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal expenses = expenseMap.getOrDefault(key, BigDecimal.ZERO);
            
            trends.add(new MonthlyTrendDto(year, month, income, expenses));
        }
        
        // Add months that only have expenses
        for (String key : expenseMap.keySet()) {
            if (!incomeMap.containsKey(key)) {
                String[] parts = key.split("-");
                Integer year = Integer.parseInt(parts[0]);
                Integer month = Integer.parseInt(parts[1]);
                
                BigDecimal income = BigDecimal.ZERO;
                BigDecimal expenses = expenseMap.get(key);
                
                trends.add(new MonthlyTrendDto(year, month, income, expenses));
            }
        }
        
        // Sort by year and month
        trends.sort((a, b) -> {
            int yearCompare = a.getYear().compareTo(b.getYear());
            if (yearCompare != 0) return yearCompare;
            return a.getMonth().compareTo(b.getMonth());
        });
        
        return trends;
    }
    
    public List<CategorySummaryDto> getTopExpenseCategories(int limit) {
        LocalDate startDate = LocalDate.now().minusMonths(12);
        LocalDate endDate = LocalDate.now();
        
        List<CategorySummaryDto> categories = getCategorySummary(TransactionType.EXPENSE, startDate, endDate);
        return categories.stream()
                .sorted((a, b) -> b.getTotalAmount().abs().compareTo(a.getTotalAmount().abs()))
                .limit(limit)
                .toList();
    }
    
    public BigDecimal getAverageMonthlyExpenses(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        BigDecimal totalExpenses = getTotalByType(TransactionType.EXPENSE, startDate, endDate);
        return totalExpenses.abs().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }
    
    public List<BudgetComparisonDto> getBudgetComparison() {
        // Get current month data
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        return getBudgetComparisonForPeriod(startDate, endDate);
    }
    
    public List<BudgetComparisonDto> getBudgetComparisonForPeriod(LocalDate startDate, LocalDate endDate) {
        List<BudgetComparisonDto> budgetComparisons = new ArrayList<>();
        
        // Get all categories
        List<Category> categories = categoryRepository.findAll();
        
        // Get actual spending for the period by category
        List<CategorySummaryDto> actualSpending = getCategorySummary(TransactionType.EXPENSE, startDate, endDate);
        Map<String, BigDecimal> actualSpendingMap = new HashMap<>();
        for (CategorySummaryDto spending : actualSpending) {
            actualSpendingMap.put(spending.getCategoryName(), spending.getTotalAmount().abs());
        }
        
        // Create budget comparison for each category with a budget
        for (Category category : categories) {
            if (category.getMonthlyBudget() != null && category.getMonthlyBudget().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal actualAmount = actualSpendingMap.getOrDefault(category.getName(), BigDecimal.ZERO);
                BudgetComparisonDto comparison = new BudgetComparisonDto(
                    category.getName(),
                    category.getMonthlyBudget(),
                    actualAmount,
                    category.getColor()
                );
                budgetComparisons.add(comparison);
            }
        }
        
        return budgetComparisons;
    }
}
