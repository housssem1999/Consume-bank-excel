package com.finance.dashboard.controller;

import com.finance.dashboard.dto.BudgetComparisonDto;
import com.finance.dashboard.dto.CategorySummaryDto;
import com.finance.dashboard.dto.FinancialSummaryDto;
import com.finance.dashboard.dto.HeatmapDataDto;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.service.FinancialStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final FinancialStatisticsService financialStatisticsService;

    private final TransactionRepository transactionRepository;
    
    @Autowired
    public DashboardController(FinancialStatisticsService financialStatisticsService,
                              TransactionRepository transactionRepository) {
        this.financialStatisticsService = financialStatisticsService;
        this.transactionRepository = transactionRepository;
    }
    
    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDto> getFinancialSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        FinancialSummaryDto summary;
        
        if (startDate != null && endDate != null) {
            summary = financialStatisticsService.getFinancialSummary(startDate, endDate);
        } else {
            // Default to current year
            summary = financialStatisticsService.getCurrentYearSummary();
        }
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/summary/current-month")
    public ResponseEntity<FinancialSummaryDto> getCurrentMonthSummary() {
        FinancialSummaryDto summary = financialStatisticsService.getCurrentMonthSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/summary/current-year")
    public ResponseEntity<FinancialSummaryDto> getCurrentYearSummary() {
        FinancialSummaryDto summary = financialStatisticsService.getCurrentYearSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Pageable pageable;
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        } else {
            // Default sort by date descending
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        }
        
        Page<Transaction> transactionPage;
        
        if (startDate != null && endDate != null) {
            transactionPage = transactionRepository.findByDateBetween(startDate, endDate, pageable);
        } else {
            transactionPage = transactionRepository.findAll(pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", transactionPage.getContent());
        response.put("total", transactionPage.getTotalElements());
        response.put("page", transactionPage.getNumber());
        response.put("size", transactionPage.getSize());
        response.put("totalPages", transactionPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/top-expenses")
    public ResponseEntity<List<CategorySummaryDto>> getTopExpenseCategories(
            @RequestParam(defaultValue = "5") int limit) {
        
        List<CategorySummaryDto> topCategories = financialStatisticsService.getTopExpenseCategories(limit);
        return ResponseEntity.ok(topCategories);
    }
    
    @GetMapping("/average-monthly-expenses")
    public ResponseEntity<Map<String, Object>> getAverageMonthlyExpenses(
            @RequestParam(defaultValue = "12") int months) {
        
        BigDecimal average = financialStatisticsService.getAverageMonthlyExpenses(months);
        
        Map<String, Object> response = new HashMap<>();
        response.put("averageMonthlyExpenses", average);
        response.put("period", months + " months");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get current month and year summaries
        FinancialSummaryDto currentMonth = financialStatisticsService.getCurrentMonthSummary();
        FinancialSummaryDto currentYear = financialStatisticsService.getCurrentYearSummary();
        
        stats.put("currentMonth", Map.of(
            "income", currentMonth.getTotalIncome(),
            "expenses", currentMonth.getTotalExpenses(),
            "net", currentMonth.getNetIncome(),
            "transactions", currentMonth.getTotalTransactions()
        ));
        
        stats.put("currentYear", Map.of(
            "income", currentYear.getTotalIncome(),
            "expenses", currentYear.getTotalExpenses(),
            "net", currentYear.getNetIncome(),
            "transactions", currentYear.getTotalTransactions()
        ));
        
        // Get total transactions count
        long totalTransactions = transactionRepository.count();
        stats.put("totalTransactions", totalTransactions);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/budget-comparison")
    public ResponseEntity<List<BudgetComparisonDto>> getBudgetComparison() {
        List<BudgetComparisonDto> budgetComparison = financialStatisticsService.getBudgetComparison();
        return ResponseEntity.ok(budgetComparison);
    }
    
    @GetMapping("/budget-comparison/period")
    public ResponseEntity<List<BudgetComparisonDto>> getBudgetComparisonForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<BudgetComparisonDto> budgetComparison = financialStatisticsService.getBudgetComparisonForPeriod(startDate, endDate);
        return ResponseEntity.ok(budgetComparison);
    }

    @GetMapping("/expense-heatmap")
    public ResponseEntity<List<HeatmapDataDto>> getExpenseHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<HeatmapDataDto> heatmapData;
        
        if (startDate != null && endDate != null) {
            heatmapData = financialStatisticsService.getExpenseHeatmapData(startDate, endDate);
        } else {
            // Default to last year
            heatmapData = financialStatisticsService.getExpenseHeatmapDataLastYear();
        }
        
        return ResponseEntity.ok(heatmapData);
    }
}
