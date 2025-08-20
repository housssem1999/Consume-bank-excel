package com.finance.dashboard.service;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    @Autowired
    private final CategoryRepository categoryRepository;
    
    private Map<String, String> categoryKeywords;
    private Category defaultCategory;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @PostConstruct
    public void initializeCategories() {
        createDefaultCategories();
        initializeCategoryKeywords();
    }
    
    private void createDefaultCategories() {
        String[][] defaultCategories = {
            {"Food & Dining", "Restaurants, groceries, food delivery", "#FF6B6B"},
            {"Transportation", "Gas, public transport, car maintenance", "#4ECDC4"},
            {"Shopping", "Clothing, electronics, general shopping", "#45B7D1"},
            {"Entertainment", "Movies, games, subscriptions", "#96CEB4"},
            {"Bills & Utilities", "Electricity, water, internet, phone", "#FFEAA7"},
            {"Healthcare", "Medical expenses, pharmacy, insurance", "#DDA0DD"},
            {"Education", "Books, courses, tuition", "#98D8C8"},
            {"Travel", "Hotels, flights, vacation expenses", "#F7DC6F"},
            // Income source categories for better breakdown
            {"Salary", "Regular employment income, payroll", "#82E0AA"},
            {"Freelance Income", "Freelance work, consulting, gig economy", "#7DCEA0"},
            {"Investment Income", "Dividends, capital gains, investment returns", "#76D7C4"},
            {"Interest Income", "Bank interest, savings interest", "#85C1E9"},
            {"Bonus Income", "Bonuses, commissions, performance pay", "#F8C471"},
            {"Other Income", "Miscellaneous income sources", "#D5A6BD"},
            {"Transfer", "Bank transfers, internal movements", "#AED6F1"},
            {"Other", "Miscellaneous expenses", "#D5DBDB"}
        };
        
        for (String[] categoryData : defaultCategories) {
            if (!categoryRepository.existsByName(categoryData[0])) {
                Category category = new Category(categoryData[0], categoryData[1], categoryData[2]);
                categoryRepository.save(category);
                logger.info("Created default category: {}", categoryData[0]);
            }
        }
        
        // Set default category
        defaultCategory = categoryRepository.findByName("Other").orElse(null);
    }
    
    private void initializeCategoryKeywords() {
        categoryKeywords = new HashMap<>();
        
        // Food & Dining keywords
        categoryKeywords.put("restaurant", "Food & Dining");
        categoryKeywords.put("mcdonalds", "Food & Dining");
        categoryKeywords.put("burger", "Food & Dining");
        categoryKeywords.put("pizza", "Food & Dining");
        categoryKeywords.put("grocery", "Food & Dining");
        categoryKeywords.put("supermarket", "Food & Dining");
        categoryKeywords.put("cafe", "Food & Dining");
        categoryKeywords.put("starbucks", "Food & Dining");
        categoryKeywords.put("food", "Food & Dining");
        
        // Transportation keywords
        categoryKeywords.put("gas", "Transportation");
        categoryKeywords.put("fuel", "Transportation");
        categoryKeywords.put("uber", "Transportation");
        categoryKeywords.put("taxi", "Transportation");
        categoryKeywords.put("bus", "Transportation");
        categoryKeywords.put("metro", "Transportation");
        categoryKeywords.put("parking", "Transportation");
        categoryKeywords.put("car", "Transportation");
        
        // Shopping keywords
        categoryKeywords.put("amazon", "Shopping");
        categoryKeywords.put("ebay", "Shopping");
        categoryKeywords.put("walmart", "Shopping");
        categoryKeywords.put("target", "Shopping");
        categoryKeywords.put("mall", "Shopping");
        categoryKeywords.put("store", "Shopping");
        categoryKeywords.put("clothing", "Shopping");
        
        // Entertainment keywords
        categoryKeywords.put("netflix", "Entertainment");
        categoryKeywords.put("spotify", "Entertainment");
        categoryKeywords.put("movie", "Entertainment");
        categoryKeywords.put("cinema", "Entertainment");
        categoryKeywords.put("game", "Entertainment");
        categoryKeywords.put("subscription", "Entertainment");
        
        // Bills & Utilities keywords
        categoryKeywords.put("electric", "Bills & Utilities");
        categoryKeywords.put("water", "Bills & Utilities");
        categoryKeywords.put("internet", "Bills & Utilities");
        categoryKeywords.put("phone", "Bills & Utilities");
        categoryKeywords.put("utility", "Bills & Utilities");
        categoryKeywords.put("bill", "Bills & Utilities");
        
        // Healthcare keywords
        categoryKeywords.put("pharmacy", "Healthcare");
        categoryKeywords.put("doctor", "Healthcare");
        categoryKeywords.put("hospital", "Healthcare");
        categoryKeywords.put("medical", "Healthcare");
        categoryKeywords.put("health", "Healthcare");
        categoryKeywords.put("insurance", "Healthcare");
        
        // Income keywords - specific categorization for income sources breakdown
        categoryKeywords.put("salary", "Salary");
        categoryKeywords.put("payroll", "Salary");
        categoryKeywords.put("wages", "Salary");
        categoryKeywords.put("employment", "Salary");
        
        categoryKeywords.put("freelance", "Freelance Income");
        categoryKeywords.put("consulting", "Freelance Income");
        categoryKeywords.put("contractor", "Freelance Income");
        categoryKeywords.put("gig", "Freelance Income");
        
        categoryKeywords.put("dividend", "Investment Income");
        categoryKeywords.put("investment", "Investment Income");
        categoryKeywords.put("capital gains", "Investment Income");
        categoryKeywords.put("stocks", "Investment Income");
        categoryKeywords.put("mutual fund", "Investment Income");
        
        categoryKeywords.put("interest", "Interest Income");
        categoryKeywords.put("savings interest", "Interest Income");
        categoryKeywords.put("bank interest", "Interest Income");
        
        categoryKeywords.put("bonus", "Bonus Income");
        categoryKeywords.put("commission", "Bonus Income");
        categoryKeywords.put("incentive", "Bonus Income");
        categoryKeywords.put("performance", "Bonus Income");
        
        // Transfer keywords
        categoryKeywords.put("transfer", "Transfer");
        categoryKeywords.put("atm", "Transfer");
        categoryKeywords.put("withdrawal", "Transfer");
        categoryKeywords.put("deposit", "Transfer");
    }
    
    public Category categorizeTransaction(String description) {
        if (description == null || description.trim().isEmpty()) {
            return defaultCategory;
        }
        
        String lowerDescription = description.toLowerCase();
        
        // Check for keyword matches
        for (Map.Entry<String, String> entry : categoryKeywords.entrySet()) {
            if (lowerDescription.contains(entry.getKey())) {
                Optional<Category> category = categoryRepository.findByName(entry.getValue());
                if (category.isPresent()) {
                    return category.get();
                }
            }
        }
        
        return defaultCategory;
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category createCategory(String name, String description, String color) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }
        
        Category category = new Category(name, description, color);
        return categoryRepository.save(category);
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
}
