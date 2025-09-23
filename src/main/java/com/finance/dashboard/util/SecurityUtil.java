package com.finance.dashboard.util;

import com.finance.dashboard.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("No authenticated user found");
    }
    
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && authentication.getPrincipal() instanceof User;
    }
}