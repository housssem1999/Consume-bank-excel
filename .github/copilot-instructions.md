# Personal Finance Dashboard - GitHub Copilot Instructions

A comprehensive personal finance management application built with Spring Boot 3.2.0 (Java 17) backend and React 18 frontend, featuring Excel file processing for bank statement imports with automatic transaction categorization.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Prerequisites Verification
Ensure the following are available before proceeding:
- **Java 17** or higher (`java -version`)
- **Maven 3.6** or higher (`mvn -version`) 
- **Node.js 16** or higher (`node --version`) - Tested with Node.js 20.19.4
- **npm** (`npm --version`) - Tested with npm 10.8.2

### Backend Setup and Build
**NEVER CANCEL builds or long-running commands. Set appropriate timeouts.**

1. **Install backend dependencies:**
   ```bash
   mvn clean install -DskipTests
   ```
   - **NEVER CANCEL**: Takes approximately 2 minutes on first run. Set timeout to 180+ seconds.
   - Downloads all Maven dependencies including Spring Boot, Apache POI, H2 Database
   - Creates executable JAR in `target/finance-dashboard-0.0.1-SNAPSHOT.jar`

2. **Run backend tests:**
   ```bash
   mvn test
   ```
   - **Note**: No tests currently exist in the backend. This command completes in ~1 second.

3. **Start Spring Boot application:**
   ```bash
   mvn spring-boot:run
   ```
   - Starts in approximately 4 seconds
   - Runs on `http://localhost:8080`
   - Creates H2 in-memory database with default categories
   - H2 Console available at `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:financedb`
     - Username: `sa`
     - Password: (empty)

### Frontend Setup and Build
**CRITICAL**: All frontend commands must use `CI=false` to avoid build failures due to linting warnings.

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install frontend dependencies:**
   ```bash
   npm install
   ```
   - **NEVER CANCEL**: Takes approximately 6 minutes on first run. Set timeout to 420+ seconds.
   - Downloads React 18, Ant Design, Chart.js, Axios, and all dependencies
   - Creates `node_modules/` directory (excluded from git via `.gitignore`)

3. **Start development server:**
   ```bash
   CI=false npm start
   ```
   - **CRITICAL**: Must use `CI=false` to ignore linting warnings
   - Compiles with warnings but runs successfully
   - Runs on `http://localhost:3000`
   - Automatically proxies API requests to `http://localhost:8080`
   - Hot-reload enabled for development

4. **Build for production:**
   ```bash
   CI=false npm run build
   ```
   - **CRITICAL**: Must use `CI=false` to ignore linting warnings
   - Takes approximately 30 seconds
   - Creates optimized build in `build/` directory (excluded from git via `.gitignore`)

5. **Run frontend tests:**
   ```bash
   npm test
   ```
   - **Note**: No tests currently exist in the frontend. Use `npm test -- --passWithNoTests` to exit with code 0.

## Validation

### Manual Testing Requirements
**ALWAYS manually validate application functionality after making changes.**

1. **Start both applications:**
   ```bash
   # Terminal 1: Backend
   mvn spring-boot:run
   
   # Terminal 2: Frontend  
   cd frontend && CI=false npm start
   ```

2. **Verify backend API functionality:**
   ```bash
   # Test categories endpoint
   curl http://localhost:8080/api/categories
   
   # Should return 11 default categories: Food & Dining, Transportation, Shopping, etc.
   ```

3. **Complete user workflow validation:**
   - Open browser to `http://localhost:3000`
   - Verify dashboard loads with empty state
   - **CRITICAL TEST**: Upload an Excel file:
     - Create test Excel file with columns: Date, Description, Amount, Reference
     - Upload via the file upload component
     - Verify transactions are processed and categorized automatically
     - Verify dashboard shows updated statistics and charts

4. **Expected Excel file format for testing:**
   ```
   Date       | Description          | Amount  | Reference
   2024-01-15 | Grocery Store       | -85.50  | TXN001
   2024-01-16 | Salary Deposit      | 3000.00 | SAL001
   2024-01-17 | Gas Station         | -45.20  | TXN002
   ```

### Build Validation
**ALWAYS run these commands before committing changes:**

1. **Backend validation:**
   ```bash
   mvn clean install -DskipTests  # Must complete without errors
   mvn spring-boot:run            # Must start successfully
   ```

2. **Frontend validation:**
   ```bash
   cd frontend
   CI=false npm run build         # Must complete without errors
   CI=false npm start            # Must compile and serve
   ```

## Common Issues and Solutions

### Frontend Build Issues
- **Problem**: Build fails with ESLint errors
- **Solution**: Always use `CI=false` prefix for npm commands
- **Example**: `CI=false npm run build` instead of `npm run build`

### Backend Database Issues
- **Problem**: Database connection errors
- **Solution**: H2 is in-memory; restart Spring Boot application to reset database

### Port Conflicts
- **Backend**: Change port in `src/main/resources/application.yml` (server.port)
- **Frontend**: Set PORT environment variable: `PORT=3001 CI=false npm start`

## Key Project Information

### Technology Stack
- **Backend**: Spring Boot 3.2.0, Java 17, Maven, H2 Database, Apache POI
- **Frontend**: React 18, Ant Design, Chart.js, Axios, Node.js 20

### Project Structure
```
finance-dashboard/
├── src/main/java/com/finance/dashboard/    # Backend Java source
│   ├── controller/                         # REST API controllers
│   ├── service/                           # Business logic
│   ├── model/                             # JPA entities
│   ├── repository/                        # Data access layer
│   └── dto/                               # Data transfer objects
├── src/main/resources/
│   └── application.yml                    # Backend configuration
├── frontend/
│   ├── src/
│   │   ├── components/                    # React components
│   │   ├── services/                      # API service layer
│   │   └── charts/                        # Chart components
│   └── package.json                       # Frontend dependencies
└── pom.xml                                # Maven configuration
```

### Key API Endpoints
- `GET /api/categories` - List all categories
- `POST /api/upload/excel` - Upload and process Excel file
- `GET /api/dashboard/summary` - Financial summary
- `GET /api/dashboard/transactions` - Transaction list

### Automatic Categorization
The application automatically categorizes transactions based on description keywords:
- "grocery", "food" → Food & Dining
- "gas", "fuel" → Transportation  
- "salary", "income" → Income
- "atm", "withdrawal" → Transfer

### Configuration Files
- **Backend config**: `src/main/resources/application.yml`
- **Frontend config**: `frontend/package.json`
- **CORS**: Configured for `http://localhost:3000` in backend controllers

## Development Guidelines

### Making Changes
1. **Always start both backend and frontend** before making changes
2. **Test Excel upload functionality** after any changes to processing logic
3. **Verify dashboard displays** updated data correctly
4. **Use `CI=false`** for all frontend npm commands
5. **Set appropriate timeouts** for build commands (180+ seconds for Maven, 420+ seconds for npm install)

### Performance Expectations
- Maven clean install: ~2 minutes (first run)
- npm install: ~6 minutes (first run)
- Frontend build: ~30 seconds
- Backend startup: ~4 seconds
- Frontend dev server startup: ~30 seconds

**NEVER CANCEL long-running commands. Wait for completion.**