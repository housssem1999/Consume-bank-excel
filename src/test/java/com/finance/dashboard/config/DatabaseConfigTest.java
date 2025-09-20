package com.finance.dashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    void testDatabaseUrlConversion() throws URISyntaxException {
        // Test the URL conversion logic similar to what DatabaseConfig does
        String railwayUrl = "postgresql://postgres:NKSLtPyJtEbEPeanzJEXrTLBEYVpWgED@postgres.railway.internal:5432/railway";
        
        URI uri = new URI(railwayUrl);
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", 
            uri.getHost(), 
            uri.getPort(), 
            uri.getPath());
        
        String username = uri.getUserInfo().split(":")[0];
        String password = uri.getUserInfo().split(":")[1];
        
        assertEquals("jdbc:postgresql://postgres.railway.internal:5432/railway", jdbcUrl);
        assertEquals("postgres", username);
        assertEquals("NKSLtPyJtEbEPeanzJEXrTLBEYVpWgED", password);
    }
    
    @Test
    void testJdbcUrlCreation() {
        // Test that we can create a valid DataSource with converted URL
        String jdbcUrl = "jdbc:postgresql://postgres.railway.internal:5432/railway";
        String username = "postgres";
        String password = "testpassword";
        
        DataSource dataSource = DataSourceBuilder.create()
            .url(jdbcUrl)
            .username(username)
            .password(password)
            .driverClassName("org.postgresql.Driver")
            .build();
        
        assertNotNull(dataSource);
    }
}