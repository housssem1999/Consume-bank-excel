package com.finance.dashboard.controller;

import com.finance.dashboard.dto.ExpensePredictionDto;
import com.finance.dashboard.dto.SpendingInsightDto;
import com.finance.dashboard.dto.TransactionAnalysisDto;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.service.MLAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ml-analytics")
@CrossOrigin(origins = "http://localhost:3000")
public class MLAnalyticsController {
    
    private final MLAnalyticsService mlAnalyticsService;
    
    @Autowired
    public MLAnalyticsController(MLAnalyticsService mlAnalyticsService) {
        this.mlAnalyticsService = mlAnalyticsService;
    }
    
    /**
     * Get expense predictions for the next N days
     */
    @GetMapping("/predictions/expenses")
    public ResponseEntity<Map<String, Object>> getExpensePredictions(
            @RequestParam(defaultValue = "30") int daysAhead) {
        
        List<ExpensePredictionDto> predictions = mlAnalyticsService.predictFutureExpenses(daysAhead);
        
        Map<String, Object> response = new HashMap<>();
        response.put("predictions", predictions);
        response.put("predictionPeriod", daysAhead + " days");
        response.put("generatedAt", LocalDate.now());
        response.put("model", "Hugging Face + Statistical Analysis");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Analyze a transaction description and suggest categorization
     */
    @PostMapping("/analyze/transaction")
    public ResponseEntity<TransactionAnalysisDto> analyzeTransaction(
            @RequestBody Map<String, String> request) {
        
        String description = request.get("description");
        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        TransactionAnalysisDto analysis = mlAnalyticsService.analyzeTransaction(description);
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Get spending insights using ML analysis
     */
    @GetMapping("/insights/spending")
    public ResponseEntity<Map<String, Object>> getSpendingInsights() {
        
        List<SpendingInsightDto> insights = mlAnalyticsService.generateSpendingInsights();
        
        Map<String, Object> response = new HashMap<>();
        response.put("insights", insights);
        response.put("totalInsights", insights.size());
        response.put("generatedAt", LocalDate.now());
        response.put("analysisModel", "Hugging Face ML + Pattern Analysis");
        
        // Group insights by severity
        Map<String, Long> insightsBySeverity = insights.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        SpendingInsightDto::getSeverity,
                        java.util.stream.Collectors.counting()));
        
        response.put("insightsBySeverity", insightsBySeverity);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Detect spending anomalies in a given date range
     */
    @GetMapping("/anomalies/spending")
    public ResponseEntity<Map<String, Object>> getSpendingAnomalies(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<Transaction> anomalies = mlAnalyticsService.detectSpendingAnomalies(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("anomalies", anomalies);
        response.put("totalAnomalies", anomalies.size());
        response.put("analysisPeriod", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "totalDays", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
        ));
        response.put("detectionModel", "Statistical Anomaly Detection");
        
        // Calculate total anomaly amount
        double totalAnomalyAmount = anomalies.stream()
                .mapToDouble(t -> t.getAmount().abs().doubleValue())
                .sum();
        
        response.put("totalAnomalyAmount", totalAnomalyAmount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get comprehensive ML-powered financial analysis
     */
    @GetMapping("/analysis/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveAnalysis(
            @RequestParam(defaultValue = "30") int predictionDays) {
        
        Map<String, Object> analysis = new HashMap<>();
        
        // Get predictions
        List<ExpensePredictionDto> predictions = mlAnalyticsService.predictFutureExpenses(predictionDays);
        analysis.put("expensePredictions", predictions);
        
        // Get insights
        List<SpendingInsightDto> insights = mlAnalyticsService.generateSpendingInsights();
        analysis.put("spendingInsights", insights);
        
        // Get recent anomalies
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<Transaction> anomalies = mlAnalyticsService.detectSpendingAnomalies(startDate, endDate);
        analysis.put("recentAnomalies", anomalies);
        
        // Summary statistics
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPredictions", predictions.size());
        summary.put("totalInsights", insights.size());
        summary.put("totalAnomalies", anomalies.size());
        summary.put("predictionPeriod", predictionDays + " days");
        summary.put("generatedAt", LocalDate.now());
        
        analysis.put("summary", summary);
        
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Batch analyze multiple transaction descriptions
     */
    @PostMapping("/analyze/transactions/batch")
    public ResponseEntity<Map<String, Object>> batchAnalyzeTransactions(
            @RequestBody Map<String, List<String>> request) {
        
        List<String> descriptions = request.get("descriptions");
        if (descriptions == null || descriptions.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<TransactionAnalysisDto> analyses = descriptions.stream()
                .limit(50) // Limit to 50 transactions to prevent abuse
                .map(mlAnalyticsService::analyzeTransaction)
                .toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("analyses", analyses);
        response.put("totalAnalyzed", analyses.size());
        response.put("processedAt", LocalDate.now());
        
        return ResponseEntity.ok(response);
    }
}