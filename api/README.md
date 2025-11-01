# Finance Dashboard - Serverless API

This directory contains the Node.js serverless functions that power the Personal Finance Dashboard backend.

## 📁 Structure

**Consolidated for Vercel Free Tier (5 functions max)**

```
api/
├── auth.js                  # Authentication endpoints (consolidated)
│                           # POST /api/auth/login
│                           # POST /api/auth/register
│                           # GET /api/auth/me
├── categories.js           # Category management (consolidated)
│                           # GET/POST /api/categories
├── transactions.js         # Transaction CRUD (consolidated)
│                           # POST /api/transactions
│                           # GET/PUT/DELETE /api/transactions/:id
├── dashboard.js            # Dashboard endpoints (consolidated)
│                           # GET /api/dashboard/summary
│                           # GET /api/dashboard/transactions
├── upload.js               # File upload (consolidated)
│                           # POST /api/upload/excel
├── lib/                    # Internal libraries
│   └── db.js              # MongoDB connection
├── models/                 # MongoDB schemas
│   ├── User.js
│   ├── Category.js
│   ├── Transaction.js
│   └── UserCategoryBudget.js
├── utils/                  # Utility functions
│   ├── jwt.js              # JWT token handling
│   └── categorizer.js      # Auto-categorization logic
├── middleware/             # Middleware functions
│   └── auth.js             # Authentication middleware
└── package.json            # Dependencies
```

**Note:** Each top-level `.js` file in `/api` becomes a Vercel serverless function. This structure uses only **5 functions**, well under the Hobby plan limit of 12.

## 🔧 Dependencies

- **mongoose** (^8.0.0) - MongoDB ODM
- **bcryptjs** (^2.4.3) - Password hashing
- **jsonwebtoken** (^9.0.2) - JWT authentication
- **xlsx** (^0.18.5) - Excel file processing
- **multer** (^1.4.5-lts.1) - File upload handling

## 🚀 Deployment

These functions are automatically deployed to Vercel as serverless functions. Each `.js` file in the subdirectories becomes an API endpoint.

### Local Development

```bash
# Install dependencies
cd api
npm install

# Run with Vercel CLI
vercel dev
```

## 📡 API Endpoints

### Authentication

#### POST /api/auth/register
Register a new user.

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securepassword",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "token": "jwt-token-here",
  "user": {
    "id": "user-id",
    "username": "john_doe",
    "email": "john@example.com",
    ...
  }
}
```

#### POST /api/auth/login
Login with username/email and password.

**Request:**
```json
{
  "usernameOrEmail": "john_doe",
  "password": "securepassword"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "jwt-token-here",
  "user": { ... }
}
```

#### GET /api/auth/me
Get current user information (requires authentication).

**Headers:**
```
Authorization: Bearer jwt-token-here
```

**Response:**
```json
{
  "success": true,
  "user": {
    "id": "user-id",
    "username": "john_doe",
    ...
  }
}
```

### Categories

#### GET /api/categories
Get all categories for the current user.

**Response:**
```json
[
  {
    "id": "category-id",
    "name": "Food & Dining",
    "description": "Groceries and restaurants",
    "color": "#FF6384",
    "monthlyBudget": 500,
    "isSystemCategory": true
  },
  ...
]
```

#### POST /api/categories
Create a new category.

**Request:**
```json
{
  "name": "Custom Category",
  "description": "My custom category",
  "color": "#FF5733",
  "monthlyBudget": 200
}
```

### Transactions

#### POST /api/transactions
Create a new transaction.

**Request:**
```json
{
  "date": "2024-01-15",
  "description": "Grocery Shopping",
  "amount": 85.50,
  "type": "EXPENSE",
  "categoryId": "category-id",
  "reference": "TXN001"
}
```

#### GET /api/transactions/[id]
Get a specific transaction.

#### PUT /api/transactions/[id]
Update a transaction.

#### DELETE /api/transactions/[id]
Delete a transaction.

### Dashboard

#### GET /api/dashboard/summary
Get financial summary for a period.

**Query Parameters:**
- `startDate` (optional): Start date (YYYY-MM-DD)
- `endDate` (optional): End date (YYYY-MM-DD)

**Response:**
```json
{
  "totalIncome": 5000,
  "totalExpenses": 3200,
  "netIncome": 1800,
  "totalTransactions": 45,
  "categoryBreakdown": [
    { "name": "Food & Dining", "amount": 850 },
    ...
  ],
  "monthlyTrends": [
    { "month": "2024-01", "income": 5000, "expenses": 3200 },
    ...
  ]
}
```

#### GET /api/dashboard/transactions
Get paginated transactions.

**Query Parameters:**
- `startDate` (optional): Filter start date
- `endDate` (optional): Filter end date
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 50)
- `sortBy` (optional): Sort field (default: 'date')
- `sortDir` (optional): Sort direction (default: 'desc')

### File Upload

#### POST /api/upload/excel
Upload and process an Excel file.

**Request:**
```json
{
  "file": "base64-encoded-file-content",
  "filename": "transactions.xlsx"
}
```

**Response:**
```json
{
  "success": true,
  "message": "File uploaded and processed successfully",
  "transactionsProcessed": 42,
  "transactions": [ ... ]
}
```

## 🔐 Authentication

All endpoints except `/api/auth/login` and `/api/auth/register` require authentication.

Include the JWT token in the Authorization header:

```
Authorization: Bearer your-jwt-token-here
```

## 🗄️ Database Schema

### User
- username (unique)
- email (unique)
- password (hashed)
- firstName, lastName
- role (USER/ADMIN)
- enabled, account flags
- timestamps

### Category
- name
- description
- color
- monthlyBudget
- user (null for system categories)
- timestamps

### Transaction
- date
- description
- amount
- type (INCOME/EXPENSE/TRANSFER)
- category (reference)
- user (reference)
- reference
- timestamps

### UserCategoryBudget
- user (reference)
- category (reference)
- monthlyBudget
- year, month
- timestamps

## 🔄 Auto-Categorization

The `utils/categorizer.js` module automatically categorizes transactions based on description keywords:

```javascript
const keywords = {
  'Food & Dining': ['grocery', 'food', 'restaurant', ...],
  'Transportation': ['gas', 'fuel', 'uber', ...],
  'Shopping': ['amazon', 'walmart', ...],
  ...
};
```

Transactions are matched against these keywords and assigned appropriate categories.

## ⚡ Performance

- **Cold start**: ~1-2 seconds (first request)
- **Warm start**: ~100-300ms (subsequent requests)
- **MongoDB queries**: ~50-100ms average
- **Excel processing**: ~500ms per 100 transactions

## 🐛 Error Handling

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description"
}
```

HTTP status codes:
- 200: Success
- 201: Created
- 400: Bad Request
- 401: Unauthorized
- 404: Not Found
- 405: Method Not Allowed
- 500: Internal Server Error

## 🔒 Security

- Passwords hashed with bcrypt (10 rounds)
- JWT tokens with configurable expiration
- MongoDB injection protection via Mongoose
- Input validation on all endpoints
- CORS configured in vercel.json

## 📊 Monitoring

View logs in Vercel dashboard:
1. Go to your project in Vercel
2. Click "Functions" tab
3. Select a function to view logs
4. Monitor performance and errors

## 🧪 Testing

```bash
# Test locally
vercel dev

# Test specific endpoint
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"test","password":"test123"}'
```

## 📝 Environment Variables

Required environment variables:

```env
MONGODB_URI=mongodb+srv://...
JWT_SECRET=your-secret-key
JWT_EXPIRATION=24h
```

Set these in Vercel project settings under "Environment Variables".
