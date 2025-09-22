package com.finance.dashboard.controller;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.service.CategoryService;
import com.finance.dashboard.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Autowired
    public TransactionController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTransaction(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Transaction> transaction = transactionService.getTransactionById(id);
            if (transaction.isPresent()) {
                response.put("success", true);
                response.put("transaction", transaction.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Transaction not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Validate transaction exists
            Optional<Transaction> existingTransaction = transactionService.getTransactionById(id);
            if (!existingTransaction.isPresent()) {
                response.put("success", false);
                response.put("message", "Transaction not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Additional business validation
            Map<String, String> validationErrors = validateTransactionUpdate(request);
            if (!validationErrors.isEmpty()) {
                response.put("success", false);
                response.put("message", "Validation failed");
                response.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Update transaction
            Transaction transaction = existingTransaction.get();
            updateTransactionFromRequest(transaction, request);
            
            Transaction updatedTransaction = transactionService.saveTransaction(transaction);
            
            response.put("success", true);
            response.put("message", "Transaction updated successfully");
            response.put("transaction", updatedTransaction);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Additional business validation
            Map<String, String> validationErrors = validateTransactionCreate(request);
            if (!validationErrors.isEmpty()) {
                response.put("success", false);
                response.put("message", "Validation failed");
                response.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Create transaction
            Transaction transaction = createTransactionFromRequest(request);
            Transaction savedTransaction = transactionService.saveTransaction(transaction);
            
            response.put("success", true);
            response.put("message", "Transaction created successfully");
            response.put("transaction", savedTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Transaction> transaction = transactionService.getTransactionById(id);
            if (!transaction.isPresent()) {
                response.put("success", false);
                response.put("message", "Transaction not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            transactionService.deleteTransaction(id);
            
            response.put("success", true);
            response.put("message", "Transaction deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper methods for validation and conversion
    private Map<String, String> validateTransactionUpdate(TransactionUpdateRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        // Validate date format
        try {
            if (request.getDate() != null && !request.getDate().trim().isEmpty()) {
                LocalDate.parse(request.getDate());
            }
        } catch (DateTimeParseException e) {
            errors.put("date", "Invalid date format. Please use YYYY-MM-DD format.");
        }
        
        // Validate amount
        if (request.getAmount() != null) {
            try {
                BigDecimal amount = new BigDecimal(request.getAmount());
                if (amount.scale() > 2) {
                    errors.put("amount", "Amount cannot have more than 2 decimal places.");
                }
            } catch (NumberFormatException e) {
                errors.put("amount", "Invalid amount format.");
            }
        }
        
        // Validate transaction type
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            try {
                TransactionType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.put("type", "Invalid transaction type. Valid types are: INCOME, EXPENSE, TRANSFER");
            }
        }
        
        // Validate category
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
            if (!category.isPresent()) {
                errors.put("categoryId", "Category not found with ID: " + request.getCategoryId());
            }
        }
        
        // Validate description length
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            errors.put("description", "Description cannot exceed 500 characters.");
        }
        
        return errors;
    }
    
    private Map<String, String> validateTransactionCreate(TransactionCreateRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        // All fields are required for creation
        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            errors.put("date", "Date is required.");
        } else {
            try {
                LocalDate.parse(request.getDate());
            } catch (DateTimeParseException e) {
                errors.put("date", "Invalid date format. Please use YYYY-MM-DD format.");
            }
        }
        
        if (request.getAmount() == null || request.getAmount().trim().isEmpty()) {
            errors.put("amount", "Amount is required.");
        } else {
            try {
                BigDecimal amount = new BigDecimal(request.getAmount());
                if (amount.scale() > 2) {
                    errors.put("amount", "Amount cannot have more than 2 decimal places.");
                }
            } catch (NumberFormatException e) {
                errors.put("amount", "Invalid amount format.");
            }
        }
        
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            errors.put("description", "Description is required.");
        } else if (request.getDescription().length() > 500) {
            errors.put("description", "Description cannot exceed 500 characters.");
        }
        
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            errors.put("type", "Transaction type is required.");
        } else {
            try {
                TransactionType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.put("type", "Invalid transaction type. Valid types are: INCOME, EXPENSE, TRANSFER");
            }
        }
        
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
            if (!category.isPresent()) {
                errors.put("categoryId", "Category not found with ID: " + request.getCategoryId());
            }
        }
        
        return errors;
    }
    
    private void updateTransactionFromRequest(Transaction transaction, TransactionUpdateRequest request) {
        if (request.getDate() != null && !request.getDate().trim().isEmpty()) {
            transaction.setDate(LocalDate.parse(request.getDate()));
        }
        
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            transaction.setDescription(request.getDescription().trim());
        }
        
        if (request.getAmount() != null && !request.getAmount().trim().isEmpty()) {
            transaction.setAmount(new BigDecimal(request.getAmount()));
        }
        
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            transaction.setType(TransactionType.valueOf(request.getType().toUpperCase()));
        }
        
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
            category.ifPresent(transaction::setCategory);
        }
        
        if (request.getReference() != null) {
            transaction.setReference(request.getReference().trim());
        }
    }
    
    private Transaction createTransactionFromRequest(TransactionCreateRequest request) {
        Transaction transaction = new Transaction();
        
        transaction.setDate(LocalDate.parse(request.getDate()));
        transaction.setDescription(request.getDescription().trim());
        transaction.setAmount(new BigDecimal(request.getAmount()));
        transaction.setType(TransactionType.valueOf(request.getType().toUpperCase()));
        
        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryService.getCategoryById(request.getCategoryId());
            if (category.isPresent()) {
                transaction.setCategory(category.get());
            }
        } else {
            // Auto-categorize if no category provided
            Category autoCategory = categoryService.categorizeTransaction(request.getDescription());
            transaction.setCategory(autoCategory);
        }
        
        if (request.getReference() != null) {
            transaction.setReference(request.getReference().trim());
        }
        
        return transaction;
    }

    // Request DTOs
    public static class TransactionUpdateRequest {
        private String date;
        private String description;
        private String amount;
        private String type;
        private Long categoryId;
        private String reference;

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
    public static class TransactionCreateRequest {
        private String date;
        private String description;
        private String amount;
        private String type;
        private Long categoryId;
        private String reference;

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
}