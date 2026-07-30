package com.finance.dashboard.service;

import com.finance.dashboard.dto.ExpensePredictionDto;
import com.finance.dashboard.dto.SpendingInsightDto;
import com.finance.dashboard.dto.TransactionAnalysisDto;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MLAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MLAnalyticsService.class);
    
    private final HuggingFaceService huggingFaceService;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public MLAnalyticsService(HuggingFaceService huggingFaceService, 
                             TransactionRepository transactionRepository,
                             CategoryRepository categoryRepository) {
        this.huggingFaceService = huggingFaceService;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Predict future expenses based on historical data and ML analysis
     */
    public List<ExpensePredictionDto> predictFutureExpenses(int daysAhead) {
        User currentUser = SecurityUtil.getCurrentUser();
        List<ExpensePredictionDto> predictions = new ArrayList<>();
        
        try {
            // Get historical data for the last 90 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(90);
            
            List<Transaction> historicalTransactions = transactionRepository
                    .findByUserAndDateBetweenAndType(currentUser, startDate, endDate, TransactionType.EXPENSE);
            
            if (historicalTransactions.isEmpty()) {
                return predictions;
            }
            
            // Group transactions by category
            Map<String, List<Transaction>> transactionsByCategory = historicalTransactions.stream()
                    .collect(Collectors.groupingBy(t -> 
                            t.getCategory() != null ? t.getCategory().getName() : "Other"));
            
            // Generate predictions for each category
            for (Map.Entry<String, List<Transaction>> entry : transactionsByCategory.entrySet()) {
                String categoryName = entry.getKey();
                List<Transaction> categoryTransactions = entry.getValue();
                
                ExpensePredictionDto prediction = predictCategoryExpense(categoryName, categoryTransactions, daysAhead);
                if (prediction != null) {
                    predictions.add(prediction);
                }
            }
            
            // Sort by predicted amount (highest first)
            predictions.sort((a, b) -> b.getPredictedAmount().compareTo(a.getPredictedAmount()));
            
        } catch (Exception e) {
            logger.error("Error predicting future expenses: {}", e.getMessage(), e);
        }
        
        return predictions;
    }
    
    /**
     * Analyze transaction and suggest better categorization
     */
    public TransactionAnalysisDto analyzeTransaction(String description) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            List<Category> availableCategories = categoryRepository.findAvailableCategoriesForUser(currentUser);
            List<String> categoryNames = availableCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            
            // Use Hugging Face for categorization
            Map<String, Object> categorizationResult = huggingFaceService.categorizeTransaction(description, categoryNames);
            
            // Use Hugging Face for sentiment analysis
            Map<String, Object> sentimentResult = huggingFaceService.analyzeSentiment(description);
            
            TransactionAnalysisDto analysis = new TransactionAnalysisDto();
            analysis.setTransactionDescription(description);
            analysis.setSuggestedCategory((String) categorizationResult.get("suggestedCategory"));
            analysis.setConfidence((Double) categorizationResult.get("confidence"));
            analysis.setAlternativeCategories((List<String>) categorizationResult.get("alternatives"));
            analysis.setSentiment((String) sentimentResult.get("sentiment"));
            analysis.setSentimentScore((Double) sentimentResult.get("confidence"));
            analysis.setExtractedKeywords(extractKeywords(description));
            
            return analysis;
            
        } catch (Exception e) {
            logger.error("Error analyzing transaction: {}", e.getMessage(), e);
            return createFallbackAnalysis(description);
        }
    }
    
    /**
     * Generate spending insights using ML analysis
     */
    public List<SpendingInsightDto> generateSpendingInsights() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<SpendingInsightDto> insights = new ArrayList<>();
        
        try {
            // Get last 6 months of data
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(6);
            
            List<Transaction> recentTransactions = transactionRepository
                    .findByUserAndDateBetween(currentUser, startDate, endDate);
            
            if (recentTransactions.isEmpty()) {
                return insights;
            }
            
            // Generate various types of insights
            insights.addAll(generateSpendingPatternInsights(recentTransactions));
            insights.addAll(generateBudgetInsights(recentTransactions));
            insights.addAll(generateAnomalyInsights(recentTransactions));
            
            // Use Hugging Face to enhance insights
            String financialSummary = createFinancialSummary(recentTransactions);
            List<String> mlInsights = huggingFaceService.generateSpendingInsights(financialSummary);
            
            for (String insight : mlInsights) {
                SpendingInsightDto dto = new SpendingInsightDto();
                dto.setInsightType("ML_RECOMMENDATION");
                dto.setTitle("AI-Generated Insight");
                dto.setDescription(insight);
                dto.setSeverity("MEDIUM");
                dto.setConfidence(0.7);
                insights.add(dto);
            }
            
        } catch (Exception e) {
            logger.error("Error generating spending insights: {}", e.getMessage(), e);
        }
        
        return insights;
    }
    
    /**
     * Get spending anomalies using ML detection
     */
    public List<Transaction> detectSpendingAnomalies(LocalDate startDate, LocalDate endDate) {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Transaction> anomalies = new ArrayList<>();
        
        try {
            List<Transaction> transactions = transactionRepository
                    .findByUserAndDateBetweenAndType(currentUser, startDate, endDate, TransactionType.EXPENSE);
            
            // Calculate statistics for anomaly detection
            Map<String, DoubleSummaryStatistics> categoryStats = transactions.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getCategory() != null ? t.getCategory().getName() : "Other",
                            Collectors.summarizingDouble(t -> t.getAmount().abs().doubleValue())
                    ));
            
            // Find transactions that are significantly higher than average
            for (Transaction transaction : transactions) {
                String categoryName = transaction.getCategory() != null ? transaction.getCategory().getName() : "Other";
                DoubleSummaryStatistics stats = categoryStats.get(categoryName);
                
                if (stats != null && stats.getCount() > 1) {
                    double transactionAmount = transaction.getAmount().abs().doubleValue();
                    double mean = stats.getAverage();
                    double threshold = mean * 2.5; // 2.5x the average
                    
                    if (transactionAmount > threshold) {
                        anomalies.add(transaction);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error detecting spending anomalies: {}", e.getMessage(), e);
        }
        
        return anomalies;
    }
    
    private ExpensePredictionDto predictCategoryExpense(String categoryName, List<Transaction> transactions, int daysAhead) {
        try {
            // Calculate daily average
            double totalAmount = transactions.stream()
                    .mapToDouble(t -> t.getAmount().abs().doubleValue())
                    .sum();
            
            long daysCovered = ChronoUnit.DAYS.between(
                    transactions.stream().map(Transaction::getDate).min(LocalDate::compareTo).orElse(LocalDate.now()),
                    transactions.stream().map(Transaction::getDate).max(LocalDate::compareTo).orElse(LocalDate.now())
            ) + 1;
            
            double dailyAverage = totalAmount / Math.max(daysCovered, 1);
            
            // Apply trend analysis (simple linear trend)
            double trend = calculateTrend(transactions);
            double adjustedDailyAverage = dailyAverage * (1 + trend);
            
            BigDecimal predictedAmount = BigDecimal.valueOf(adjustedDailyAverage * daysAhead)
                    .setScale(2, RoundingMode.HALF_UP);
            
            ExpensePredictionDto prediction = new ExpensePredictionDto();
            prediction.setPredictionDate(LocalDate.now().plusDays(daysAhead));
            prediction.setPredictedAmount(predictedAmount);
            prediction.setCategoryName(categoryName);
            prediction.setConfidence(calculatePredictionConfidence(transactions));
            prediction.setPredictionModel("Linear Trend + Historical Average");
            prediction.setInsights(generatePredictionInsights(categoryName, transactions, trend));
            
            return prediction;
            
        } catch (Exception e) {
            logger.warn("Error predicting expenses for category {}: {}", categoryName, e.getMessage());
            return null;
        }
    }
    
    private double calculateTrend(List<Transaction> transactions) {
        if (transactions.size() < 2) return 0.0;
        
        // Simple linear trend calculation
        List<Transaction> sortedTransactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());
        
        double firstHalfAverage = sortedTransactions.stream()
                .limit(sortedTransactions.size() / 2)
                .mapToDouble(t -> t.getAmount().abs().doubleValue())
                .average()
                .orElse(0.0);
        
        double secondHalfAverage = sortedTransactions.stream()
                .skip(sortedTransactions.size() / 2)
                .mapToDouble(t -> t.getAmount().abs().doubleValue())
                .average()
                .orElse(0.0);
        
        return firstHalfAverage > 0 ? (secondHalfAverage - firstHalfAverage) / firstHalfAverage : 0.0;
    }
    
    private double calculatePredictionConfidence(List<Transaction> transactions) {
        if (transactions.size() < 3) return 0.3;
        if (transactions.size() < 10) return 0.6;
        return 0.8;
    }
    
    private List<String> generatePredictionInsights(String categoryName, List<Transaction> transactions, double trend) {
        List<String> insights = new ArrayList<>();
        
        if (trend > 0.1) {
            insights.add("Spending in " + categoryName + " is trending upward by " + String.format("%.1f", trend * 100) + "%");
        } else if (trend < -0.1) {
            insights.add("Spending in " + categoryName + " is trending downward by " + String.format("%.1f", Math.abs(trend) * 100) + "%");
        } else {
            insights.add("Spending in " + categoryName + " is relatively stable");
        }
        
        double avgAmount = transactions.stream()
                .mapToDouble(t -> t.getAmount().abs().doubleValue())
                .average()
                .orElse(0.0);
        
        insights.add("Average transaction amount: $" + String.format("%.2f", avgAmount));
        
        return insights;
    }
    
    private List<String> extractKeywords(String description) {
        // Simple keyword extraction
        String[] words = description.toLowerCase().split("\\s+");
        return Arrays.stream(words)
                .filter(word -> word.length() > 3)
                .filter(word -> !isStopWord(word))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "for", "are", "but", "not", "you", "all", "can", "had", "her", "was", "one", "our", "out", "day", "get", "has", "him", "his", "how", "its", "may", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "man", "men", "use", "she", "run", "put", "set", "too", "use", "yet");
        return stopWords.contains(word);
    }
    
    private TransactionAnalysisDto createFallbackAnalysis(String description) {
        TransactionAnalysisDto analysis = new TransactionAnalysisDto();
        analysis.setTransactionDescription(description);
        analysis.setSuggestedCategory("Other");
        analysis.setConfidence(0.5);
        analysis.setSentiment("neutral");
        analysis.setSentimentScore(0.5);
        analysis.setExtractedKeywords(extractKeywords(description));
        return analysis;
    }
    
    private List<SpendingInsightDto> generateSpendingPatternInsights(List<Transaction> transactions) {
        List<SpendingInsightDto> insights = new ArrayList<>();
        
        // Analyze spending by day of week
        Map<Integer, Double> spendingByDay = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getDate().getDayOfWeek().getValue(),
                        Collectors.summingDouble(t -> t.getAmount().abs().doubleValue())
                ));
        
        if (!spendingByDay.isEmpty()) {
            int maxDay = Collections.max(spendingByDay.entrySet(), Map.Entry.comparingByValue()).getKey();
            String dayName = getDayName(maxDay);
            
            SpendingInsightDto insight = new SpendingInsightDto();
            insight.setInsightType("SPENDING_PATTERN");
            insight.setTitle("Peak Spending Day");
            insight.setDescription("You spend the most on " + dayName + "s");
            insight.setSeverity("LOW");
            insight.setConfidence(0.8);
            insights.add(insight);
        }
        
        return insights;
    }
    
    private List<SpendingInsightDto> generateBudgetInsights(List<Transaction> transactions) {
        List<SpendingInsightDto> insights = new ArrayList<>();
        
        // Calculate monthly average
        double monthlyExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(t -> t.getAmount().abs().doubleValue())
                .sum() / 6; // 6 months of data
        
        if (monthlyExpenses > 5000) {
            SpendingInsightDto insight = new SpendingInsightDto();
            insight.setInsightType("BUDGET_ALERT");
            insight.setTitle("High Monthly Spending");
            insight.setDescription("Your average monthly expenses are $" + String.format("%.2f", monthlyExpenses));
            insight.setSeverity("HIGH");
            insight.setImpact(BigDecimal.valueOf(monthlyExpenses));
            insight.setRecommendations(Arrays.asList(
                "Review your largest expense categories",
                "Consider setting spending limits",
                "Look for subscription services you might not need"
            ));
            insights.add(insight);
        }
        
        return insights;
    }
    
    private List<SpendingInsightDto> generateAnomalyInsights(List<Transaction> transactions) {
        List<SpendingInsightDto> insights = new ArrayList<>();
        
        List<Transaction> anomalies = detectSpendingAnomalies(
                LocalDate.now().minusMonths(1), 
                LocalDate.now()
        );
        
        if (!anomalies.isEmpty()) {
            SpendingInsightDto insight = new SpendingInsightDto();
            insight.setInsightType("ANOMALY_DETECTION");
            insight.setTitle("Unusual Spending Detected");
            insight.setDescription("Found " + anomalies.size() + " unusual transactions in the last month");
            insight.setSeverity("MEDIUM");
            insight.setConfidence(0.7);
            insights.add(insight);
        }
        
        return insights;
    }
    
    private String createFinancialSummary(List<Transaction> transactions) {
        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        
        double totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(t -> t.getAmount().abs().doubleValue())
                .sum();
        
        return String.format("Total income: $%.2f, Total expenses: $%.2f, Net: $%.2f, Transaction count: %d", 
                totalIncome, totalExpenses, totalIncome - totalExpenses, transactions.size());
    }
    
    private String getDayName(int dayOfWeek) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return days[dayOfWeek - 1];
    }
}