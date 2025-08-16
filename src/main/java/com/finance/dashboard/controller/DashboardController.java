package com.finance.dashboard.controller;

import com.finance.dashboard.dto.CategorySummaryDto;
import com.finance.dashboard.dto.FinancialSummaryDto;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.service.FinancialStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage;
        
        if (startDate != null && endDate != null) {
            transactionPage = transactionRepository.findByDateBetween(startDate, endDate, pageable);
        } else {
            transactionPage = transactionRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(transactionPage.getContent());
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
}
