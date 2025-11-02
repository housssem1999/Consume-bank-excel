# Migration Guide: Spring Boot → Serverless

This guide helps you migrate from the Spring Boot + PostgreSQL architecture to the new serverless architecture.

## 🎯 Architecture Comparison

| Component | Old Architecture | New Architecture |
|-----------|-----------------|------------------|
| Backend | Spring Boot (Java 17) | Vercel Serverless Functions (Node.js 18+) |
| Database | PostgreSQL (Railway) | MongoDB Atlas (Free Tier) |
| ORM | JPA/Hibernate | Mongoose ODM |
| Authentication | Spring Security + JWT | JWT with bcrypt |
| File Upload | Multipart/FormData | Base64 encoding |
| Hosting | Railway ($5/mo) | Vercel (Free) |
| **Total Cost** | **$5/month** | **$0/month** |

## 📋 Prerequisites for Migration

### What You Need
1. MongoDB Atlas account (free)
2. Vercel account (free)
3. Node.js 18+ installed locally
4. Your existing application data (if any)

### What Gets Replaced
- ❌ Spring Boot application (`src/main/java`)
- ❌ PostgreSQL database
- ❌ Railway deployment
- ❌ Maven dependencies
- ❌ `application.yml` configuration

### What Stays the Same
- ✅ React frontend (no changes to UI)
- ✅ Business logic (reimplemented in Node.js)
- ✅ API endpoints (same URLs)
- ✅ Authentication flow (still JWT-based)
- ✅ Data structure (adapted to MongoDB)

## 🔄 Data Migration Steps

### Option 1: Fresh Start (Recommended for Development)

If you don't have critical data or want to start fresh:

1. **Deploy the serverless application** following `SERVERLESS_DEPLOYMENT.md`
2. **Register a new account** through the UI
3. **Upload your Excel files** to populate transactions

### Option 2: Migrate Existing Data

If you have existing data in PostgreSQL that you want to migrate:

#### Step 1: Export Data from PostgreSQL

```bash
# Connect to your Railway PostgreSQL database
# Get connection details from Railway dashboard

# Export users
psql $DATABASE_URL -c "COPY users TO STDOUT WITH CSV HEADER" > users.csv

# Export categories
psql $DATABASE_URL -c "COPY categories TO STDOUT WITH CSV HEADER" > categories.csv

# Export transactions
psql $DATABASE_URL -c "COPY transactions TO STDOUT WITH CSV HEADER" > transactions.csv
```

#### Step 2: Transform Data

Create a Node.js migration script:

```javascript
// migrate.js
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const fs = require('fs');
const csv = require('csv-parser');

// Import models
const User = require('./lib/api/models/User');
const Category = require('./lib/api/models/Category');
const Transaction = require('./lib/api/models/Transaction');

async function migrate() {
  await mongoose.connect(process.env.MONGODB_URI);
  
  // Migrate users
  const users = [];
  fs.createReadStream('users.csv')
    .pipe(csv())
    .on('data', (row) => {
      users.push({
        username: row.username,
        email: row.email,
        password: row.password, // Already hashed
        firstName: row.first_name,
        lastName: row.last_name,
        role: row.role,
        createdAt: row.created_at
      });
    })
    .on('end', async () => {
      await User.insertMany(users);
      console.log('Users migrated');
    });

  // Similar for categories and transactions...
}

migrate();
```

#### Step 3: Run Migration

```bash
# Install dependencies
npm install mongoose bcrypt csv-parser

# Set MongoDB URI
export MONGODB_URI="your-mongodb-connection-string"

# Run migration
node migrate.js
```

### Option 3: Parallel Running (Zero Downtime)

Run both architectures in parallel during migration:

1. **Keep Railway running** with existing Spring Boot app
2. **Deploy serverless version** to a staging URL
3. **Test serverless version** thoroughly
4. **Export data** from PostgreSQL
5. **Import data** to MongoDB
6. **Switch frontend** to use serverless API
7. **Decommission Railway** after verification

## 🔧 Code Migration Details

### Authentication Migration

**Old (Spring Security):**
```java
@Autowired
private AuthenticationManager authenticationManager;

Authentication auth = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);
```

**New (Node.js):**
```javascript
const bcrypt = require('bcryptjs');
const isValid = await bcrypt.compare(password, user.password);
```

### Database Query Migration

**Old (JPA Repository):**
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}

// Usage
List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, start, end);
```

**New (Mongoose):**
```javascript
// Usage
const transactions = await Transaction.find({
  user: userId,
  date: { $gte: start, $lte: end }
}).populate('category');
```

### File Upload Migration

**Old (Spring Boot):**
```java
@PostMapping("/upload/excel")
public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
    // Process file
}
```

**New (Serverless):**
```javascript
// Backend expects base64
const { file, filename } = req.body; // base64 string
const buffer = Buffer.from(file, 'base64');

// Frontend sends base64
const reader = new FileReader();
reader.onload = (e) => {
  const base64 = e.target.result.split(',')[1];
  fetch('/api/upload/excel', {
    method: 'POST',
    body: JSON.stringify({ file: base64, filename: file.name })
  });
};
reader.readAsDataURL(file);
```

## 🧪 Testing Migration

### 1. Test Authentication
```bash
# Register
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"test123"}'

# Login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"test","password":"test123"}'
```

### 2. Test Categories
```bash
# Get categories (with token from login)
curl http://localhost:3000/api/categories \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Test Transactions
```bash
# Create transaction
curl -X POST http://localhost:3000/api/transactions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date":"2024-01-15",
    "description":"Test",
    "amount":100,
    "type":"EXPENSE"
  }'
```

### 4. Test Dashboard
```bash
# Get summary
curl http://localhost:3000/api/dashboard/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🔍 Verification Checklist

After migration, verify these features work:

- [ ] User registration and login
- [ ] JWT token authentication
- [ ] Get categories list
- [ ] Create custom category
- [ ] Upload Excel file
- [ ] View transactions list
- [ ] Create manual transaction
- [ ] Update transaction
- [ ] Delete transaction
- [ ] Dashboard summary displays correctly
- [ ] Monthly trends calculation
- [ ] Category breakdown
- [ ] Budget tracking
- [ ] Pagination works
- [ ] Sorting and filtering work

## 🚨 Common Issues & Solutions

### Issue: "Cannot connect to MongoDB"
**Solution**: Check MongoDB Atlas network access allows your IP (0.0.0.0/0 for serverless)

### Issue: "Authentication failed"
**Solution**: Verify JWT_SECRET is set in environment variables

### Issue: "Excel upload fails"
**Solution**: Check file size (Vercel has 4.5MB limit for request body)

### Issue: "Categories not showing"
**Solution**: Default categories are created on first user registration

### Issue: "Transactions not displaying"
**Solution**: Check MongoDB connection and verify data was migrated correctly

## 📊 Performance Comparison

### Old Architecture (Railway)
- **Cold start**: 5-10 seconds (container startup)
- **Warm response**: 50-200ms
- **Database query**: 10-50ms (PostgreSQL)
- **Concurrent users**: Limited by RAM
- **Scaling**: Manual (upgrade plan)

### New Architecture (Serverless)
- **Cold start**: 1-2 seconds (first request)
- **Warm response**: 100-300ms
- **Database query**: 50-100ms (MongoDB Atlas)
- **Concurrent users**: Auto-scaling (thousands)
- **Scaling**: Automatic and instant

## 💡 Best Practices

### For MongoDB
1. **Indexes**: Already added for common queries
2. **Connection pooling**: Implemented with cached connections
3. **Query optimization**: Use `.lean()` for read-only operations
4. **Aggregations**: Use for complex statistics

### For Serverless Functions
1. **Keep functions small**: Each endpoint is a separate function
2. **Reuse connections**: MongoDB connections are cached
3. **Optimize imports**: Only import what you need
4. **Handle timeouts**: Vercel has 10-second limit

### For Security
1. **Environment variables**: Never commit secrets
2. **JWT expiration**: Set appropriate token lifetime
3. **Password hashing**: Use bcrypt with 10+ rounds
4. **Input validation**: Validate all user input

## 📚 Additional Resources

- [MongoDB Atlas Documentation](https://docs.atlas.mongodb.com/)
- [Vercel Serverless Functions](https://vercel.com/docs/functions)
- [Mongoose Guide](https://mongoosejs.com/docs/guide.html)
- [JWT Best Practices](https://jwt.io/introduction)

## 🎉 Deployment

Once migration is complete, follow `SERVERLESS_DEPLOYMENT.md` to deploy to production.

---

**Questions?** Create an issue on GitHub or check the deployment documentation.
