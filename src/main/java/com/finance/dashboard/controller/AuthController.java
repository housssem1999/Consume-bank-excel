package com.finance.dashboard.controller;

import com.finance.dashboard.model.User;
import com.finance.dashboard.security.JwtUtil;
import com.finance.dashboard.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;
            
            // Update last login time
            userService.updateLastLogin(user);
            
            String token = jwtUtil.generateToken(userDetails);
            
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", loginRequest.getUsernameOrEmail());
            response.put("success", false);
            response.put("message", "Invalid username/email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Login error for user: {}", loginRequest.getUsernameOrEmail(), e);
            response.put("success", false);
            response.put("message", "Login failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userService.existsByUsername(registerRequest.getUsername())) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (userService.existsByEmail(registerRequest.getEmail())) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            User user = userService.createUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(), 
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName()
            );
            
            String token = jwtUtil.generateToken(user);
            
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("token", token);
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Registration error for username: {}", registerRequest.getUsername(), e);
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) authentication.getPrincipal();
            response.put("success", true);
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting current user", e);
            response.put("success", false);
            response.put("message", "Error retrieving user information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("fullName", user.getFullName());
        userResponse.put("role", user.getRole());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("lastLoginAt", user.getLastLoginAt());
        return userResponse;
    }
    
    // Request DTOs
    public static class LoginRequest {
        @NotBlank(message = "Username or email is required")
        private String usernameOrEmail;
        
        @NotBlank(message = "Password is required")
        private String password;
        
        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        
        @Size(max = 100, message = "First name must be less than 100 characters")
        private String firstName;
        
        @Size(max = 100, message = "Last name must be less than 100 characters")
        private String lastName;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}