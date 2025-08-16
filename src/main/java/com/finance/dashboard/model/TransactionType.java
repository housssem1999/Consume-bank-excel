package com.finance.dashboard.model;

public enum TransactionType {
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Transfer");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static TransactionType fromAmount(double amount) {
        return amount >= 0 ? INCOME : EXPENSE;
    }
}
