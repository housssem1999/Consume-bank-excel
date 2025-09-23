package com.finance.dashboard.controller;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.service.ExcelProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    private final ExcelProcessingService excelProcessingService;

    public FileUploadController(ExcelProcessingService excelProcessingService) {
        this.excelProcessingService = excelProcessingService;
    }
    
    @PostMapping("/excel")
    public ResponseEntity<Map<String, Object>> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                response.put("success", false);
                response.put("message", "Please upload a valid Excel file (.xlsx or .xls)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Process the file
            List<Transaction> transactions = excelProcessingService.processExcelFile(file);
            
            response.put("success", true);
            response.put("message", "File uploaded and processed successfully");
            response.put("transactionsProcessed", transactions.size());
            response.put("transactions", transactions);
            
            logger.info("Successfully processed Excel file: {} with {} transactions", 
                       filename, transactions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing Excel file: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Error processing file: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/sample-format")
    public ResponseEntity<Map<String, Object>> getSampleFormat() {
        Map<String, Object> response = new HashMap<>();
        
        // Provide information about expected Excel format
        Map<String, String> format = new HashMap<>();
        format.put("Column A", "Date (YYYY-MM-DD or Excel date format)");
        format.put("Column B", "Description (Transaction description)");
        format.put("Column C", "Amount (Positive for income, negative for expenses)");
        format.put("Column D", "Reference (Optional - transaction reference)");
        
        response.put("expectedFormat", format);
        response.put("notes", List.of(
            "First row should contain headers",
            "Date should be in a recognizable format",
            "Amount should be numeric (positive for income, negative for expenses)",
            "Description will be used for automatic categorization"
        ));
        
        return ResponseEntity.ok(response);
    }
}
