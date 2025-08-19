package com.finance.dashboard.service;

import com.finance.dashboard.dto.CategorySummaryDto;
import com.finance.dashboard.dto.FinancialSummaryDto;
import com.finance.dashboard.dto.ForecastDto;
import com.finance.dashboard.dto.MonthlyTrendDto;
import com.finance.dashboard.model.TransactionType;
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

    public FinancialStatisticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
        
        // Add forecast for next month
        summary.setForecast(calculateForecast());
        
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
    
    /**
     * Calculate forecast for the next month using linear regression on the last 6 months of data
     */
    public ForecastDto calculateForecast() {
        List<MonthlyTrendDto> trends = getMonthlyTrends();
        
        if (trends.size() < 3) {
            // Not enough data for meaningful forecast
            return null;
        }
        
        // Get the last 6 months of data (or all available if less than 6)
        int dataPoints = Math.min(6, trends.size());
        List<MonthlyTrendDto> recentTrends = trends.subList(trends.size() - dataPoints, trends.size());
        
        // Calculate next month's date
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        
        // Use linear regression for income and expenses
        BigDecimal projectedIncome = calculateLinearRegressionProjection(recentTrends, "income");
        BigDecimal projectedExpenses = calculateLinearRegressionProjection(recentTrends, "expenses");
        
        // Calculate confidence bounds using standard deviation
        BigDecimal incomeStdDev = calculateStandardDeviation(recentTrends, "income");
        BigDecimal expensesStdDev = calculateStandardDeviation(recentTrends, "expenses");
        
        // Use the larger standard deviation for conservative estimate
        BigDecimal confidenceMargin = incomeStdDev.max(expensesStdDev);
        BigDecimal projectedNet = projectedIncome.subtract(projectedExpenses.abs());
        
        BigDecimal lowerBound = projectedNet.subtract(confidenceMargin);
        BigDecimal upperBound = projectedNet.add(confidenceMargin);
        
        return new ForecastDto(
            nextMonth.getYear(),
            nextMonth.getMonthValue(),
            projectedIncome,
            projectedExpenses,
            lowerBound,
            upperBound,
            "LINEAR_REGRESSION"
        );
    }
    
    private BigDecimal calculateLinearRegressionProjection(List<MonthlyTrendDto> trends, String type) {
        int n = trends.size();
        if (n < 2) return BigDecimal.ZERO;
        
        // Simple linear regression: y = mx + b
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1; // Month index (1, 2, 3, ...)
            double y = getValueForType(trends.get(i), type).doubleValue();
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        // Calculate slope (m) and intercept (b)
        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double b = (sumY - m * sumX) / n;
        
        // Project next month (x = n + 1)
        double projection = m * (n + 1) + b;
        
        // Ensure non-negative values and reasonable bounds
        projection = Math.max(0, projection);
        
        return BigDecimal.valueOf(projection).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateStandardDeviation(List<MonthlyTrendDto> trends, String type) {
        if (trends.size() < 2) return BigDecimal.ZERO;
        
        // Calculate mean
        double mean = trends.stream()
            .mapToDouble(trend -> getValueForType(trend, type).doubleValue())
            .average()
            .orElse(0.0);
        
        // Calculate variance
        double variance = trends.stream()
            .mapToDouble(trend -> {
                double value = getValueForType(trend, type).doubleValue();
                return Math.pow(value - mean, 2);
            })
            .average()
            .orElse(0.0);
        
        return BigDecimal.valueOf(Math.sqrt(variance)).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal getValueForType(MonthlyTrendDto trend, String type) {
        switch (type.toLowerCase()) {
            case "income":
                return trend.getIncome() != null ? trend.getIncome() : BigDecimal.ZERO;
            case "expenses":
                return trend.getExpenses() != null ? trend.getExpenses().abs() : BigDecimal.ZERO;
            case "net":
                return trend.getNetAmount() != null ? trend.getNetAmount() : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }
}
