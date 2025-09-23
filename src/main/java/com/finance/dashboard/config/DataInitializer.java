package com.finance.dashboard.config;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserService userService;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }
    
    private void initializeDefaultUsers() {
        // Create default admin user if not exists
        if (!userService.existsByUsername("admin")) {
            try {
                User admin = userService.createUser(
                    "admin",
                    "admin@example.com",
                    "admin123",
                    "System",
                    "Administrator"
                );
                admin.setRole(Role.ADMIN);
                userService.updateUser(admin);
                logger.info("Created default admin user: admin / admin123");
            } catch (Exception e) {
                logger.error("Failed to create default admin user", e);
            }
        }
        
        // Create default demo user if not exists
        if (!userService.existsByUsername("demo")) {
            try {
                userService.createUser(
                    "demo",
                    "demo@example.com",
                    "demo123",
                    "Demo",
                    "User"
                );
                logger.info("Created default demo user: demo / demo123");
            } catch (Exception e) {
                logger.error("Failed to create default demo user", e);
            }
        }
    }
}