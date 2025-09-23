package com.finance.dashboard.model;

public enum Role {
    USER("User"),
    ADMIN("Administrator");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}