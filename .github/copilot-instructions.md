# Personal Finance Dashboard - GitHub Copilot Instructions

A comprehensive personal finance management application built with **Node.js Serverless Functions** backend and **React 18** frontend, deployed entirely on **Vercel** with **MongoDB Atlas** database. Features Excel file processing for bank statement imports with automatic transaction categorization.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Architecture Overview

### Serverless Stack
- **Frontend**: React 18 (deployed as static site)
- **Backend**: Node.js Serverless Functions (Vercel)
- **Database**: MongoDB Atlas (cloud-hosted)
- **Deployment**: Single Vercel deployment (no separate backend server)
- **Authentication**: JWT-based with bcrypt password hashing

## Working Effectively

### Prerequisites Verification
Ensure the following are available before proceeding:
- **Node.js 18** or higher (`node --version`) - Tested with Node.js 20.19.4
- **npm** (`npm --version`) - Tested with npm 10.8.2
- **Vercel CLI** (optional for local testing): `npm install -g vercel`
- **MongoDB Atlas account** with cluster created

### Serverless API Setup
**NEVER CANCEL builds or long-running commands. Set appropriate timeouts.**

1. **Install root dependencies (for serverless functions):**
   ```bash
   npm install
   ```
   - Installs: mongoose, bcryptjs, jsonwebtoken, xlsx, multer
   - Takes approximately 10-15 seconds

2. **Install API dependencies:**
   ```bash
   cd api && npm install
   ```
   - **Note**: No tests currently exist. This command completes in ~1 second.

3. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   - Edit `.env` with your MongoDB Atlas connection string
   - Add JWT secret (generate with: `node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"`)
   - Required variables:
     - `MONGODB_URI`: MongoDB Atlas connection string
     - `JWT_SECRET`: Secret key for JWT token generation
     - `JWT_EXPIRATION`: Token expiration time (e.g., 24h)

4. **Start local Vercel development server:**
   ```bash
   vercel dev
   ```
   - Simulates serverless environment locally
   - Runs on `http://localhost:3000`
   - Both frontend and API available on same port
   - Hot-reload enabled for development
   - **Note**: First run may prompt for Vercel project setup

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
   - Automatically proxies API requests to `http://localhost:3000/api`
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
   - **Note**: No tests currently exist. Use `npm test -- --passWithNoTests` to exit with code 0.

## Validation

### Manual Testing Requirements
**ALWAYS manually validate application functionality after making changes.**

1. **Start application with Vercel Dev:**
   ```bash
   vercel dev
   ```
   - Runs both frontend and serverless functions
   - Accessible at `http://localhost:3000`
   - API endpoints at `http://localhost:3000/api/*`

2. **Verify API functionality:**
   ```bash
   # Test health check
   curl http://localhost:3000/api/categories
   
   # Should return default categories after registering/logging in
   ```

3. **Complete user workflow validation:**
   - Open browser to `http://localhost:3000`
   - Register a new user account
   - Login with credentials
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

1. **Serverless API validation:**
   ```bash
   npm install                    # Install root dependencies
   cd api && npm install          # Install API dependencies
   vercel dev                     # Start local serverless environment
   ```

2. **Frontend validation:**
   ```bash
   cd frontend
   CI=false npm run build         # Must complete without errors
   CI=false npm start             # Must compile and serve
   ```

## Common Issues and Solutions

### Mongoose "Cannot find module" Error
- **Problem**: Serverless functions can't find mongoose or other dependencies
- **Solution**: Ensure dependencies are in root `package.json`, not just `api/package.json`
- **Fix**: Run `npm install` at project root

### "MissingSchemaError: Schema hasn't been registered"
- **Problem**: Mongoose model not imported before populate
- **Solution**: Import all models used in populate() calls at top of file
- **Example**: `const Category = require('../lib/server/models/Category')`

### Frontend Build Issues
- **Problem**: Build fails with ESLint errors
- **Solution**: Always use `CI=false` prefix for npm commands
- **Example**: `CI=false npm run build` instead of `npm run build`

### MongoDB Connection Issues
- **Problem**: Cannot connect to MongoDB Atlas
- **Solution**: Check `MONGODB_URI` in `.env` and Vercel environment variables
- **Verify**: Database user password is correct and IP is whitelisted
- **Note**: Use URL encoding for special characters in password

### Vercel Serverless Function Limit
- **Problem**: "No more than 12 Serverless Functions" error
- **Solution**: Keep helper files outside `/api` directory (use `/lib/server`)
- **Note**: Only files in `/api/*.js` count as serverless functions

### Port Conflicts
- **Local Development**: Vercel dev runs on `http://localhost:3000`
- **Change Port**: Set PORT environment variable: `PORT=3001 vercel dev`

## Key Project Information

### Technology Stack
- **Backend**: Node.js Serverless Functions, Mongoose, bcryptjs, JWT, XLSX
- **Frontend**: React 18, Ant Design, Chart.js, Axios, Node.js 20
- **Database**: MongoDB Atlas (cloud-hosted, NoSQL)
- **Deployment**: Vercel (frontend + serverless backend)

### Project Structure
```
finance-dashboard/
├── api/                                   # Serverless API functions
│   ├── auth.js                           # Authentication endpoints
│   ├── categories.js                     # Category management
│   ├── dashboard.js                      # Dashboard analytics
│   ├── transactions.js                   # Transaction CRUD
│   ├── upload.js                         # Excel file processing
│   └── package.json                      # API-specific dependencies
├── lib/server/                           # Shared backend code
│   ├── db.js                            # MongoDB connection
│   ├── models/                          # Mongoose schemas
│   │   ├── User.js
│   │   ├── Category.js
│   │   ├── Transaction.js
│   │   └── UserCategoryBudget.js
│   ├── middleware/
│   │   └── auth.js                      # JWT authentication
│   └── utils/
│       ├── categorizer.js               # Auto-categorization logic
│       └── jwt.js                       # JWT utilities
├── frontend/
│   ├── src/
│   │   ├── components/                    # React components
│   │   ├── services/                      # API service layer
│   │   └── charts/                        # Chart components
│   └── package.json                       # Frontend dependencies
├── package.json                           # Root dependencies (serverless)
├── vercel.json                            # Vercel configuration
└── .env.example                           # Environment variables template
```

### Key API Endpoints
- `POST /api/auth` - Register/Login (body determines action)
- `GET /api/auth/me` - Get current user
- `GET /api/categories` - List all categories
- `POST /api/categories` - Create category
- `PUT /api/categories/:id` - Update category
- `DELETE /api/categories/:id` - Delete category
- `POST /api/upload` - Upload and process Excel file
- `GET /api/dashboard?type=summary` - Financial summary
- `GET /api/dashboard?type=transactions` - Transaction list with filters
- `GET /api/transactions` - List transactions
- `POST /api/transactions` - Create transaction
- `PUT /api/transactions/:id` - Update transaction
- `DELETE /api/transactions/:id` - Delete transaction

### Automatic Categorization
The application automatically categorizes transactions based on description keywords:
- "grocery", "food" → Food & Dining
- "gas", "fuel" → Transportation  
- "salary", "income" → Income
- "atm", "withdrawal" → Transfer

### Configuration Files
- **Vercel config**: `vercel.json` (build, routes, headers)
- **Environment**: `.env` (local), Vercel dashboard (production)
- **Frontend config**: `frontend/package.json`
- **API dependencies**: Root `package.json` (mongoose, bcryptjs, etc.)
- **CORS**: Configured in `vercel.json` headers (allows all origins for API)

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

**NEVER CANCEL long-running commands. Wait for completion.**