# Railway PostgreSQL Connection Fix

## Problem
When deploying to Railway, you might encounter this error:
```
java.lang.RuntimeException: Driver org.postgresql.Driver claims to not accept jdbcUrl, postgresql://postgres:password@host:port/database
```

## Root Cause
Railway automatically sets the `DATABASE_URL` environment variable in Heroku-style format:
```
postgresql://username:password@host:port/database
```

However, Spring Boot's PostgreSQL driver expects JDBC format:
```
jdbc:postgresql://host:port/database
```

## Solution
The application now includes automatic URL conversion in `DatabaseConfig.java` that:

1. **Detects Railway URLs**: Only activates when `DATABASE_URL` starts with `postgresql://`
2. **Converts Format**: Transforms to proper JDBC format
3. **Extracts Credentials**: Parses username and password from the URL
4. **Production Only**: Only runs with `prod` profile to avoid affecting development

## How It Works

```java
@Configuration
@Profile("prod")
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            URI uri = new URI(databaseUrl);
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", 
                uri.getHost(), uri.getPort(), uri.getPath());
            
            String username = uri.getUserInfo().split(":")[0];
            String password = uri.getUserInfo().split(":")[1];
            
            return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
        }
        
        // Fallback to application-prod.yml configuration
        return DataSourceBuilder.create().build();
    }
}
```

## Testing the Fix

Run the included test script:
```bash
./test_railway_fix.sh
```

This validates:
- ✅ Development mode uses H2 database
- ✅ Production mode converts Railway URLs
- ✅ Unit tests confirm conversion logic
- ✅ No impact on existing functionality

## Deploy to Railway

The fix is automatic - just deploy as normal:

1. **Set environment variables in Railway**:
   ```env
   SPRING_PROFILES_ACTIVE=prod
   CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
   ```

2. **Railway will automatically set**:
   ```env
   DATABASE_URL=postgresql://user:pass@host:port/db
   ```

3. **Application automatically converts to**:
   ```env
   spring.datasource.url=jdbc:postgresql://host:port/db
   ```

The connection error is now resolved! 🎉