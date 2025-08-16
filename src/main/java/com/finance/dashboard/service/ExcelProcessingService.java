package com.finance.dashboard.service;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExcelProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelProcessingService.class);

    private final TransactionRepository transactionRepository;

    private final CategoryService categoryService;

    public ExcelProcessingService(TransactionRepository transactionRepository,
                                  CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    public List<Transaction> processExcelFile(MultipartFile file) throws IOException {
        logger.info("Processing Excel file: {}", file.getOriginalFilename());
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (assuming first row contains headers)
            boolean isFirstRow = true;
            
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                
                try {
                    Transaction transaction = parseRowToTransaction(row);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                } catch (Exception e) {
                    logger.warn("Error parsing row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }
        
        // Save all transactions
        List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);
        logger.info("Successfully processed {} transactions from Excel file", savedTransactions.size());
        
        return savedTransactions;
    }
    
    private Transaction parseRowToTransaction(Row row) {
        try {
            // Assuming Excel format: Date | Description | Amount | Reference
            // Adjust column indices based on your Excel format
            
            Cell dateCell = row.getCell(0);
            Cell descriptionCell = row.getCell(1);
            Cell amountCell = row.getCell(2);
            Cell referenceCell = row.getCell(3);
            
            if (dateCell == null || descriptionCell == null || amountCell == null) {
                return null;
            }
            
            // Parse date
            LocalDate date = parseDate(dateCell);
            if (date == null) {
                logger.warn("Invalid date in row {}", row.getRowNum());
                return null;
            }
            
            // Parse description
            String description = getCellValueAsString(descriptionCell);
            if (description == null || description.trim().isEmpty()) {
                logger.warn("Empty description in row {}", row.getRowNum());
                return null;
            }
            
            // Parse amount
            BigDecimal amount = parseAmount(amountCell);
            if (amount == null) {
                logger.warn("Invalid amount in row {}", row.getRowNum());
                return null;
            }
            
            // Parse reference (optional)
            String reference = referenceCell != null ? getCellValueAsString(referenceCell) : null;
            
            // Determine transaction type based on amount
            TransactionType type = amount.compareTo(BigDecimal.ZERO) >= 0 ? 
                                  TransactionType.INCOME : TransactionType.EXPENSE;
            
            // Create transaction
            Transaction transaction = new Transaction(date, description, amount, type);
            transaction.setReference(reference);
            
            // Auto-categorize transaction
            Category category = categoryService.categorizeTransaction(description);
            transaction.setCategory(category);
            
            return transaction;
            
        } catch (Exception e) {
            logger.error("Error parsing row {}: {}", row.getRowNum(), e.getMessage());
            return null;
        }
    }
    
    private LocalDate parseDate(Cell cell) {
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                // Try to parse string date - you might need to adjust the format
                String dateStr = cell.getStringCellValue();
                // Add custom date parsing logic here if needed
                return LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            logger.warn("Error parsing date: {}", e.getMessage());
        }
        return null;
    }
    
    private BigDecimal parseAmount(Cell cell) {
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String amountStr = cell.getStringCellValue().trim();
                // Remove thousands separators (commas), but do not allow malformed input
                amountStr = amountStr.replace(",", "");
                // Validate the string strictly: optional leading minus, digits, optional dot and digits
                if (amountStr.matches("^-?\\d+(\\.\\d+)?$")) {
                    return new BigDecimal(amountStr);
                } else {
                    logger.warn("Amount string '{}' is not a valid number format", amountStr);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing amount: {}", e.getMessage());
        }
        return null;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}
