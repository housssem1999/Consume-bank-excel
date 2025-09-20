package com.finance.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            // Convert Railway/Heroku style URL to JDBC format
            try {
                URI uri = new URI(databaseUrl);
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", 
                    uri.getHost(), 
                    uri.getPort(), 
                    uri.getPath());
                
                String username = uri.getUserInfo().split(":")[0];
                String password = uri.getUserInfo().split(":")[1];
                
                logger.info("Converting Railway DATABASE_URL to JDBC format");
                logger.info("Host: {}, Port: {}, Database: {}", uri.getHost(), uri.getPort(), uri.getPath());
                
                return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
                    
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL format: " + databaseUrl, e);
            }
        }
        
        logger.info("No Railway DATABASE_URL found, using default configuration");
        // Fallback to default configuration from application-prod.yml
        return DataSourceBuilder.create().build();
    }
}