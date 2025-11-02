# Serverless Deployment Guide

This guide explains how to deploy the Personal Finance Dashboard as a **fully serverless application** with **zero monthly costs**.

## 🎯 Serverless Architecture

The application has been transformed from a traditional Spring Boot + PostgreSQL setup to a modern serverless architecture:

### Before (Cost: ~$5/month)
- **Backend**: Spring Boot on Railway ($5/month)
- **Database**: PostgreSQL on Railway (included)
- **Frontend**: Vercel (Free)

### After (Cost: $0/month)
- **Backend**: Vercel Serverless Functions (Free)
- **Database**: MongoDB Atlas Free Tier (Free)
- **Frontend**: Vercel (Free)

## 📦 Technology Stack

### Backend API
- **Runtime**: Node.js 18+ (Serverless Functions)
- **Database**: MongoDB with Mongoose ODM
- **Authentication**: JWT (JSON Web Tokens)
- **File Processing**: xlsx library for Excel parsing
- **Hosting**: Vercel Serverless Functions

### Frontend
- **Framework**: React 18
- **UI Library**: Ant Design
- **Charts**: Chart.js
- **Hosting**: Vercel

### Database
- **Database**: MongoDB Atlas
- **Free Tier**: 512MB storage, shared cluster
- **Features**: Automatic backups, SSL connections

## 🚀 Deployment Steps

### Step 1: Set Up MongoDB Atlas (5 minutes)

1. **Create MongoDB Atlas Account**:
   - Go to [mongodb.com/cloud/atlas/register](https://www.mongodb.com/cloud/atlas/register)
   - Sign up for free (no credit card required)

2. **Create a Free Cluster**:
   - Click "Build a Database"
   - Select "Shared" (Free tier)
   - Choose your preferred cloud provider and region
   - Click "Create Cluster"

3. **Configure Database Access**:
   - Go to "Database Access" in left sidebar
   - Click "Add New Database User"
   - Create username and password (save these!)
   - Set privileges to "Read and write to any database"

4. **Configure Network Access**:
   - Go to "Network Access" in left sidebar
   - Click "Add IP Address"
   - Click "Allow Access from Anywhere" (0.0.0.0/0)
   - This is safe for serverless functions with authentication

5. **Get Connection String**:
   - Go to "Database" → Click "Connect" on your cluster
   - Choose "Connect your application"
   - Copy the connection string (looks like: `mongodb+srv://username:<password>@cluster0.xxxxx.mongodb.net/`)
   - Replace `<password>` with your actual password
   - Add database name at the end: `...mongodb.net/finance-dashboard`

### Step 2: Deploy to Vercel (5 minutes)

1. **Create Vercel Account**:
   - Go to [vercel.com](https://vercel.com)
   - Sign up with GitHub (free)

2. **Import Repository**:
   - Click "Add New..." → "Project"
   - Import your GitHub repository
   - Vercel will auto-detect the configuration

3. **Configure Environment Variables**:
   Click "Environment Variables" and add:

   ```env
   MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/finance-dashboard?retryWrites=true&w=majority
   JWT_SECRET=your-super-secret-jwt-key-at-least-32-characters-long
   JWT_EXPIRATION=24h
   ```

   **Important**: Use a strong, random JWT secret. Generate one using:
   ```bash
   node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
   ```

4. **Deploy**:
   - Click "Deploy"
   - Wait 2-3 minutes for build to complete
   - Your app will be live at `https://your-project.vercel.app`

### Step 3: Update Frontend API URL

After deployment, update the frontend to use the new API:

1. In your Vercel project settings, note your deployment URL
2. The API will be available at: `https://your-project.vercel.app/api`
3. No frontend changes needed - the API routes are automatically configured!

## 📡 API Endpoints

All endpoints are available at `https://your-project.vercel.app/api`:

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration  
- `GET /api/auth/me` - Get current user

### Categories
- `GET /api/categories` - List all categories
- `POST /api/categories` - Create category

### Transactions
- `GET /api/dashboard/transactions` - List transactions
- `POST /api/transactions` - Create transaction
- `GET /api/transactions/[id]` - Get transaction by ID
- `PUT /api/transactions/[id]` - Update transaction
- `DELETE /api/transactions/[id]` - Delete transaction

### Dashboard
- `GET /api/dashboard/summary` - Financial summary
- `GET /api/dashboard/transactions` - Paginated transactions

### File Upload
- `POST /api/upload/excel` - Upload Excel file

## 🔐 Authentication

All API endpoints (except login/register) require authentication:

```javascript
// Include JWT token in Authorization header
headers: {
  'Authorization': 'Bearer your-jwt-token-here',
  'Content-Type': 'application/json'
}
```

## 📊 Excel File Upload

The Excel upload endpoint expects a base64-encoded file:

```javascript
// Frontend example
const fileInput = document.querySelector('input[type="file"]');
const file = fileInput.files[0];
const reader = new FileReader();

reader.onload = async (e) => {
  const base64 = e.target.result.split(',')[1];
  
  const response = await fetch('/api/upload/excel', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      file: base64,
      filename: file.name
    })
  });
  
  const data = await response.json();
  console.log(`Processed ${data.transactionsProcessed} transactions`);
};

reader.readAsDataURL(file);
```

## 🔧 Local Development

### Prerequisites
- Node.js 18 or higher
- MongoDB Atlas account (or local MongoDB)

### Setup

1. **Install API dependencies**:
   ```bash
   cd api
   npm install
   ```

2. **Install Frontend dependencies**:
   ```bash
   cd frontend
   npm install
   ```

3. **Create `.env` file** in project root:
   ```env
   MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/finance-dashboard
   JWT_SECRET=your-development-secret-key-at-least-32-characters
   JWT_EXPIRATION=24h
   ```

4. **Run locally with Vercel CLI**:
   ```bash
   # Install Vercel CLI
   npm i -g vercel

   # Run dev server
   vercel dev
   ```

   This will:
   - Start serverless functions on `http://localhost:3000/api`
   - Start frontend on `http://localhost:3000`
   - Auto-reload on file changes

### Testing API Endpoints

```bash
# Test login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"testuser","password":"password123"}'

# Test get categories (with token)
curl http://localhost:3000/api/categories \
  -H "Authorization: Bearer your-jwt-token-here"
```

## 📈 Monitoring & Limits

### Vercel Free Tier Limits
- **Bandwidth**: 100GB/month
- **Serverless Function Executions**: Unlimited
- **Serverless Function Duration**: 10 seconds max
- **Build Time**: 6000 minutes/month
- **Deployments**: Unlimited

### MongoDB Atlas Free Tier Limits
- **Storage**: 512MB
- **RAM**: Shared
- **Connections**: 500 concurrent
- **Bandwidth**: Unlimited

### Estimated Usage
For a single user with moderate usage:
- **Storage**: ~10-50MB (thousands of transactions)
- **Bandwidth**: ~1-5GB/month
- **Function Executions**: ~1000-5000/month

**All within free tier limits!** ✅

## 🔄 Migration from Spring Boot

If you have existing data in the old Spring Boot PostgreSQL database, you'll need to migrate it:

### Option 1: Manual Export/Import
1. Export PostgreSQL data to CSV
2. Import CSV files using the Excel upload feature
3. Manually recreate categories and budgets

### Option 2: Migration Script
Create a custom migration script to:
1. Connect to both databases
2. Read data from PostgreSQL
3. Write to MongoDB with appropriate schema transformation

## 🐛 Troubleshooting

### API Returns 500 Error
- **Check MongoDB connection**: Verify `MONGODB_URI` is correct
- **Check logs**: View logs in Vercel dashboard
- **Test locally**: Run `vercel dev` to debug

### CORS Errors
- Vercel.json already includes CORS headers
- If issues persist, check browser console for specific error

### Authentication Fails
- **Verify JWT_SECRET**: Must be set in environment variables
- **Check token**: Ensure Bearer token format is correct
- **Token expired**: Login again to get a new token

### File Upload Fails
- **File size**: Vercel has 4.5MB request limit
- **Solution**: Large files should be split or compressed
- **Format**: Must be .xlsx or .xls

## 💰 Cost Comparison

| Service | Old Architecture | New Architecture | Savings |
|---------|-----------------|------------------|---------|
| Backend Hosting | Railway: $5/mo | Vercel: $0 | $5/mo |
| Database | Railway: Included | MongoDB Atlas: $0 | $0 |
| Frontend | Vercel: $0 | Vercel: $0 | $0 |
| **Total** | **$5/month** | **$0/month** | **$5/month** |

**Annual Savings**: $60/year 🎉

## 🎓 Architecture Benefits

### Serverless Advantages
✅ **Zero infrastructure management**: No servers to maintain  
✅ **Auto-scaling**: Handles traffic spikes automatically  
✅ **Pay-per-use**: Only pay for what you use (nothing in free tier!)  
✅ **Global CDN**: Fast response times worldwide  
✅ **Built-in HTTPS**: Automatic SSL certificates  
✅ **Atomic deployments**: Zero-downtime updates  

### MongoDB Advantages
✅ **Flexible schema**: Easy to modify data structure  
✅ **Document-based**: Natural JSON/JavaScript integration  
✅ **Powerful queries**: Rich query language and aggregations  
✅ **Automatic backups**: Daily backups included  
✅ **Global clusters**: Data replication across regions  

## 📚 Additional Resources

- [Vercel Documentation](https://vercel.com/docs)
- [MongoDB Atlas Documentation](https://docs.atlas.mongodb.com/)
- [Mongoose Documentation](https://mongoosejs.com/docs/)
- [JWT Documentation](https://jwt.io/introduction)

## 🆘 Support

If you encounter any issues:

1. Check the [GitHub Issues](https://github.com/housssem1999/Consume-bank-excel/issues)
2. Review Vercel deployment logs
3. Test API endpoints locally with `vercel dev`
4. Verify environment variables are set correctly

## ✅ Deployment Checklist

- [ ] MongoDB Atlas cluster created
- [ ] Database user created with password
- [ ] Network access configured (0.0.0.0/0)
- [ ] Connection string obtained
- [ ] Vercel project created
- [ ] Environment variables configured
- [ ] Project deployed successfully
- [ ] API endpoints tested
- [ ] Frontend can connect to API
- [ ] Authentication works
- [ ] Excel upload tested
- [ ] Dashboard displays data correctly

---

**🎉 Congratulations!** You now have a fully serverless, zero-cost personal finance dashboard deployed and running!
