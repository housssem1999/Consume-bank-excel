package com.finance.dashboard.service;

import com.finance.dashboard.dto.CategorySummaryDto;
import com.finance.dashboard.dto.FinancialSummaryDto;
import com.finance.dashboard.dto.MonthlyTrendDto;
import com.finance.dashboard.dto.RecurringTransactionDto;
import com.finance.dashboard.model.Transaction;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    
    public List<RecurringTransactionDto> getRecurringTransactions() {
        logger.info("Detecting recurring transactions");
        
        // Get all transactions from the last 12 months to have enough data for pattern detection
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);
        
        List<Transaction> allTransactions = transactionRepository.findByDateBetween(startDate, endDate);
                
        // Group transactions by similar merchant/description
        Map<String, List<Transaction>> groupedTransactions = groupTransactionsByMerchant(allTransactions);
        
        List<RecurringTransactionDto> recurringTransactions = new ArrayList<>();
        
        for (Map.Entry<String, List<Transaction>> entry : groupedTransactions.entrySet()) {
            List<Transaction> transactions = entry.getValue();
            
            // Skip if we don't have enough transactions to detect a pattern
            if (transactions.size() < 3) {
                continue;
            }
            
            // Sort transactions by date
            transactions.sort(Comparator.comparing(Transaction::getDate));
            
            // Analyze for recurring patterns
            RecurringTransactionDto recurringPattern = analyzeRecurringPattern(transactions);
            if (recurringPattern != null) {
                recurringTransactions.add(recurringPattern);
            }
        }
        
        // Sort by transaction count (most frequent first)
        recurringTransactions.sort((a, b) -> b.getTransactionCount().compareTo(a.getTransactionCount()));
        
        logger.info("Found {} recurring transaction patterns", recurringTransactions.size());
        return recurringTransactions;
    }
    
    private Map<String, List<Transaction>> groupTransactionsByMerchant(List<Transaction> transactions) {
        Map<String, List<Transaction>> groups = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            String description = transaction.getDescription();
            if (description == null || description.trim().isEmpty()) {
                continue;
            }
            
            // Extract merchant name (first few words or key terms)
            String merchantKey = extractMerchantKey(description);
            
            // Group by similar merchants and amounts
            String groupKey = merchantKey + "_" + getAmountRange(transaction.getAmount());
            
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(transaction);
        }
        
        return groups;
    }
    
    private String extractMerchantKey(String description) {
        // Simple merchant extraction - take first 3 words or up to common keywords
        String[] words = description.toLowerCase().trim().split("\\s+");
        StringBuilder merchant = new StringBuilder();
        
        for (int i = 0; i < Math.min(3, words.length); i++) {
            String word = words[i];
            // Skip common transaction words
            if (!word.matches("payment|transfer|deposit|withdrawal|txn|ref|\\d+")) {
                if (merchant.length() > 0) {
                    merchant.append(" ");
                }
                merchant.append(word);
            }
        }
        
        return merchant.toString().trim().isEmpty() ? description.toLowerCase() : merchant.toString();
    }
    
    private String getAmountRange(BigDecimal amount) {
        // Group amounts in ranges to account for small variations
        BigDecimal absAmount = amount.abs();
        
        if (absAmount.compareTo(BigDecimal.valueOf(50)) < 0) {
            return "0-50";
        } else if (absAmount.compareTo(BigDecimal.valueOf(100)) < 0) {
            return "50-100";
        } else if (absAmount.compareTo(BigDecimal.valueOf(500)) < 0) {
            return "100-500";
        } else if (absAmount.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return "500-1000";
        } else {
            return "1000+";
        }
    }
    
    private RecurringTransactionDto analyzeRecurringPattern(List<Transaction> transactions) {
        if (transactions.size() < 3) {
            return null;
        }
        
        // Calculate intervals between consecutive transactions
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < transactions.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(
                transactions.get(i - 1).getDate(), 
                transactions.get(i).getDate()
            );
            intervals.add(daysBetween);
        }
        
        // Check for consistent intervals
        double avgInterval = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
        
        // Check if intervals are consistent (within 20% tolerance)
        boolean isConsistent = intervals.stream()
                .allMatch(interval -> Math.abs(interval - avgInterval) / avgInterval <= 0.3);
        
        if (!isConsistent) {
            return null;
        }
        
        // Determine frequency based on average interval
        String frequency;
        int intervalDays = (int) Math.round(avgInterval);
        
        if (intervalDays >= 6 && intervalDays <= 8) {
            frequency = "WEEKLY";
        } else if (intervalDays >= 28 && intervalDays <= 35) {
            frequency = "MONTHLY";
        } else if (intervalDays >= 84 && intervalDays <= 95) {
            frequency = "QUARTERLY";
        } else {
            return null; // Not a recognized pattern
        }
        
        // Check amount consistency (within 20% tolerance)
        BigDecimal avgAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
        
        boolean amountsConsistent = transactions.stream()
                .allMatch(t -> {
                    BigDecimal diff = t.getAmount().subtract(avgAmount).abs();
                    return diff.divide(avgAmount.abs(), 4, RoundingMode.HALF_UP)
                            .compareTo(BigDecimal.valueOf(0.2)) <= 0;
                });
        
        if (!amountsConsistent) {
            return null;
        }
        
        // Create recurring transaction DTO
        Transaction firstTransaction = transactions.get(0);
        Transaction lastTransaction = transactions.get(transactions.size() - 1);
        
        String merchantName = extractMerchantKey(firstTransaction.getDescription());
        String categoryName = firstTransaction.getCategory() != null ? 
            firstTransaction.getCategory().getName() : "Uncategorized";
        
        List<LocalDate> transactionDates = transactions.stream()
                .map(Transaction::getDate)
                .collect(Collectors.toList());
        
        LocalDate nextExpectedDate = lastTransaction.getDate().plusDays(intervalDays);
        
        return new RecurringTransactionDto(
            merchantName,
            firstTransaction.getDescription(),
            avgAmount,
            frequency,
            intervalDays,
            transactions.size(),
            firstTransaction.getDate(),
            lastTransaction.getDate(),
            nextExpectedDate,
            transactionDates,
            categoryName
        );
    }
}
