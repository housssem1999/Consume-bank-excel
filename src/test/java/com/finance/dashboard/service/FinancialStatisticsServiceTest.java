package com.finance.dashboard.service;

import com.finance.dashboard.dto.ForecastDto;
import com.finance.dashboard.dto.MonthlyTrendDto;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialStatisticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FinancialStatisticsService financialStatisticsService;

    @Test
    void testCalculateForecast_WithSufficientData() {
        // Mock data for the last few months
        List<Object[]> incomeResults = new ArrayList<>();
        incomeResults.add(new Object[]{2024, 6, new BigDecimal("3000.00")});
        incomeResults.add(new Object[]{2024, 7, new BigDecimal("3100.00")});
        incomeResults.add(new Object[]{2024, 8, new BigDecimal("3200.00")});
        incomeResults.add(new Object[]{2024, 11, new BigDecimal("3300.00")});
        incomeResults.add(new Object[]{2024, 12, new BigDecimal("3350.00")});
        
        List<Object[]> expenseResults = new ArrayList<>();
        expenseResults.add(new Object[]{2024, 6, new BigDecimal("451.45")});
        expenseResults.add(new Object[]{2024, 7, new BigDecimal("340.80")});
        expenseResults.add(new Object[]{2024, 8, new BigDecimal("633.00")});
        expenseResults.add(new Object[]{2024, 11, new BigDecimal("517.25")});
        expenseResults.add(new Object[]{2024, 12, new BigDecimal("857.00")});

        when(transactionRepository.findMonthlyTotalsByType(TransactionType.INCOME))
            .thenReturn(incomeResults);
        when(transactionRepository.findMonthlyTotalsByType(TransactionType.EXPENSE))
            .thenReturn(expenseResults);

        // Call the method
        ForecastDto forecast = financialStatisticsService.calculateForecast();

        // Verify results
        assertNotNull(forecast);
        assertEquals(2025, forecast.getYear());
        assertEquals(9, forecast.getMonth()); // Next month from now (August 2025)
        assertEquals("September", forecast.getMonthName());
        assertEquals("LINEAR_REGRESSION", forecast.getForecastMethod());
        
        // Verify that income forecast is reasonable (should be increasing trend)
        assertTrue(forecast.getProjectedIncome().compareTo(new BigDecimal("3000")) > 0);
        
        // Verify that expenses forecast is positive
        assertTrue(forecast.getProjectedExpenses().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify that confidence bounds are reasonable
        assertNotNull(forecast.getConfidenceLowerBound());
        assertNotNull(forecast.getConfidenceUpperBound());
        assertTrue(forecast.getConfidenceUpperBound().compareTo(forecast.getConfidenceLowerBound()) > 0);
        
        // Verify net amount calculation
        BigDecimal expectedNet = forecast.getProjectedIncome().subtract(forecast.getProjectedExpenses().abs());
        assertEquals(0, expectedNet.compareTo(forecast.getProjectedNetAmount()));
    }

    @Test
    void testCalculateForecast_WithInsufficientData() {
        // Mock insufficient data
        List<Object[]> incomeResults = new ArrayList<>();
        incomeResults.add(new Object[]{2024, 12, new BigDecimal("3000.00")});
        
        List<Object[]> expenseResults = new ArrayList<>();
        expenseResults.add(new Object[]{2024, 12, new BigDecimal("500.00")});

        when(transactionRepository.findMonthlyTotalsByType(TransactionType.INCOME))
            .thenReturn(incomeResults);
        when(transactionRepository.findMonthlyTotalsByType(TransactionType.EXPENSE))
            .thenReturn(expenseResults);

        // Call the method
        ForecastDto forecast = financialStatisticsService.calculateForecast();

        // Should return null when insufficient data
        assertNull(forecast);
    }
}