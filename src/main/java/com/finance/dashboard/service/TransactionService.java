package com.finance.dashboard.service;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Get transaction by ID with error handling
     */
    public Optional<Transaction> getTransactionById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to fetch transaction with null ID");
                return Optional.empty();
            }
            
            logger.debug("Fetching transaction with ID: {}", id);
            return transactionRepository.findById(id);
            
        } catch (Exception e) {
            logger.error("Error fetching transaction with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve transaction", e);
        }
    }
    
    /**
     * Save or update transaction with comprehensive error handling
     */
    public Transaction saveTransaction(Transaction transaction) {
        try {
            if (transaction == null) {
                throw new IllegalArgumentException("Transaction cannot be null");
            }
            
            // Validate required fields
            validateTransaction(transaction);
            
            logger.debug("Saving transaction: {}", transaction);
            Transaction savedTransaction = transactionRepository.save(transaction);
            logger.info("Successfully saved transaction with ID: {}", savedTransaction.getId());
            
            return savedTransaction;
            
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while saving transaction: {}", e.getMessage());
            throw new RuntimeException("Transaction data violates database constraints", e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid transaction data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error saving transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save transaction", e);
        }
    }
    
    /**
     * Delete transaction with error handling
     */
    public void deleteTransaction(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Transaction ID cannot be null");
            }
            
            if (!transactionRepository.existsById(id)) {
                throw new IllegalArgumentException("Transaction not found with ID: " + id);
            }
            
            logger.debug("Deleting transaction with ID: {}", id);
            transactionRepository.deleteById(id);
            logger.info("Successfully deleted transaction with ID: {}", id);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request to delete transaction: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting transaction with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }
    
    /**
     * Check if transaction exists
     */
    public boolean existsById(Long id) {
        try {
            if (id == null) {
                return false;
            }
            return transactionRepository.existsById(id);
        } catch (Exception e) {
            logger.error("Error checking if transaction exists with ID {}: {}", id, e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate transaction data
     */
    private void validateTransaction(Transaction transaction) {
        if (transaction.getDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        
        if (transaction.getDescription() == null || transaction.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction description is required");
        }
        
        if (transaction.getDescription().length() > 500) {
            throw new IllegalArgumentException("Transaction description cannot exceed 500 characters");
        }
        
        if (transaction.getAmount() == null) {
            throw new IllegalArgumentException("Transaction amount is required");
        }
        
        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        
        // Validate amount precision (max 2 decimal places)
        if (transaction.getAmount().scale() > 2) {
            throw new IllegalArgumentException("Transaction amount cannot have more than 2 decimal places");
        }
        
        logger.debug("Transaction validation passed for: {}", transaction);
    }
}