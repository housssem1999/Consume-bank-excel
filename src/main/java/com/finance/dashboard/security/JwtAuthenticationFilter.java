package com.finance.dashboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
                logger.debug("Extracted username from JWT: {}", username);
            } catch (Exception e) {
                logger.error("Unable to get JWT Token or JWT Token has expired", e);
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String. Header: {}", requestTokenHeader);
        }

        // Once we get the token validate it
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Username from token: {}. Proceeding to load user.", username);
            UserDetails userDetails = null;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                logger.error("UserDetailsService failed to load user: {}", username, e);
            }

            if (userDetails == null) {
                logger.warn("UserDetails is null for username: {}", username);
            } else {
                // if token is valid configure Spring Security to manually set authentication
                boolean valid = jwtUtil.validateToken(jwtToken, userDetails);
                logger.debug("Token validation for user {}: {}", username, valid);
                if (valid) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    logger.warn("JWT token is invalid for user: {}", username);
                }
            }
        } else if (username == null) {
            logger.warn("Username extracted from JWT is null. Token: {}", jwtToken);
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("SecurityContext already has authentication for user: {}", username);
        }
        filterChain.doFilter(request, response);
    }
}