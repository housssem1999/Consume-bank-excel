# Personal Finance Dashboard

A comprehensive personal finance management application that helps you track your income, expenses, and financial trends by importing bank statements from Excel files.

## ⚡ **NEW: Serverless Architecture - Zero Cost Deployment!**

This application now uses a **fully serverless architecture** with **$0/month hosting costs**:
- 🆓 **Vercel Serverless Functions** (Free tier - was $5/month on Railway)
- 🆓 **MongoDB Atlas** (Free tier 512MB - was PostgreSQL on Railway)
- 🆓 **Vercel Frontend Hosting** (Already free)

**Total Monthly Cost: $0** (Previously $5/month)

See [SERVERLESS_DEPLOYMENT.md](./SERVERLESS_DEPLOYMENT.md) for full deployment guide.

## 🚀 Features

### Backend (Serverless Functions)
- **Excel File Processing**: Import bank statements from .xlsx/.xls files
- **Automatic Categorization**: Smart categorization of transactions based on description
- **Financial Statistics**: Calculate income, expenses, trends, and category breakdowns
- **RESTful API**: Clean serverless API endpoints
- **JWT Authentication**: Secure user authentication with JSON Web Tokens
- **MongoDB Database**: Flexible NoSQL database with free tier

### Frontend (React)
- **Interactive Dashboard**: Visual overview of your financial data
- **Chart Visualizations**: Pie charts, doughnut charts, and line graphs using Chart.js
- **File Upload Interface**: Drag-and-drop Excel file upload
- **Transaction Management**: View and filter transaction history
- **Responsive Design**: Works on desktop and mobile devices
- **User Authentication**: Secure login and registration

### Key Statistics
- Monthly and yearly income/expense summaries
- Category-wise spending analysis
- Monthly trends and patterns
- Top expense categories
- Average monthly expenses
- Net income calculations
- Budget tracking and comparisons

## 🛠️ Technology Stack

### Backend (Serverless)
- **Node.js 18+** - Runtime
- **Vercel Serverless Functions** - Hosting
- **MongoDB with Mongoose** - Database and ODM
- **JWT & bcrypt** - Authentication and security
- **xlsx** - Excel file processing

### Frontend
- **React 18** - Frontend framework
- **Ant Design** - UI component library
- **Chart.js & react-chartjs-2** - Data visualization
- **Axios** - HTTP client
- **React Router** - Navigation

### Legacy Backend (Optional)
The original Spring Boot backend is still available in the `src/` directory for those who prefer it.
- **Spring Boot 3.2.0** - Main framework
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Production database
- **Apache POI** - Excel file processing

## 📋 Prerequisites

### For Serverless Deployment (Recommended)
- **Node.js 18** or higher
- **MongoDB Atlas account** (free)
- **Vercel account** (free)

### For Local Development
- **Node.js 18** or higher
- **npm** or **yarn**

### For Legacy Spring Boot (Optional)
- **Java 17** or higher
- **Maven 3.6** or higher

## 🚀 Quick Start (Serverless)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Consume-bank-excel
```

### 2. Set Up MongoDB Atlas (Free)
1. Go to [mongodb.com/cloud/atlas/register](https://www.mongodb.com/cloud/atlas/register)
2. Create a free cluster
3. Create database user and get connection string
4. Note your connection string for step 3

### 3. Deploy to Vercel
1. Go to [vercel.com](https://vercel.com) and sign up
2. Click "Add New..." → "Project"
3. Import this repository
4. Add environment variables:
   ```env
   MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/finance-dashboard
   JWT_SECRET=your-secret-key-at-least-32-characters
   JWT_EXPIRATION=24h
   ```
5. Click "Deploy" - Done! 🎉

Your app will be live at `https://your-project.vercel.app`

**Full deployment guide**: See [SERVERLESS_DEPLOYMENT.md](./SERVERLESS_DEPLOYMENT.md)

## 💻 Local Development (Serverless)

### 1. Install Dependencies
```bash
# Install API dependencies
cd api
npm install

# Install frontend dependencies
cd ../frontend
npm install
cd ..
```

### 2. Set Up Environment Variables
Create `.env` file in root:
```env
MONGODB_URI=your-mongodb-connection-string
JWT_SECRET=your-secret-key-at-least-32-characters
JWT_EXPIRATION=24h
```

### 3. Run Development Server
```bash
# Install Vercel CLI
npm i -g vercel

# Run both frontend and API
vercel dev
```

The app will start on `http://localhost:3000` with API at `http://localhost:3000/api`

## 💻 Legacy Local Development (Spring Boot)

If you want to run the old Spring Boot backend:

### 1. Install Dependencies
```bash
mvn clean install
```

### 2. Run Spring Boot
```bash
mvn spring-boot:run
```
Backend starts on `http://localhost:8080`

### 3. Run Frontend
```bash
cd frontend
npm install
npm start
```
Frontend starts on `http://localhost:3000`

## 📊 Excel File Format

Your Excel file should follow this structure:

| Column A | Column B | Column C | Column D |
|----------|----------|----------|----------|
| Date | Description | Amount | Reference (Optional) |
| 2024-01-15 | Grocery Store | -85.50 | TXN001 |
| 2024-01-16 | Salary Deposit | 3500.00 | SAL001 |
| 2024-01-17 | Gas Station | -45.20 | TXN002 |

### Important Notes:
- First row should contain headers
- **Date**: YYYY-MM-DD format preferred (Excel date format also supported)
- **Amount**: Positive for income, negative for expenses
- **Description**: Used for automatic categorization
- **Reference**: Optional transaction reference

## 🎯 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user

### Dashboard
- `GET /api/dashboard/summary` - Get financial summary
- `GET /api/dashboard/transactions` - Get transactions with pagination

### Transactions
- `GET /api/transactions/[id]` - Get transaction by ID
- `POST /api/transactions` - Create transaction
- `PUT /api/transactions/[id]` - Update transaction
- `DELETE /api/transactions/[id]` - Delete transaction

### File Upload
- `POST /api/upload/excel` - Upload and process Excel file

### Categories
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create new category

**Full API documentation**: See [api/README.md](./api/README.md)

## 🏗️ Project Structure

```
Consume-bank-excel/
├── api/                     # Serverless API (Node.js)
│   ├── auth/               # Authentication endpoints
│   ├── categories/         # Category management
│   ├── transactions/       # Transaction CRUD
│   ├── dashboard/          # Dashboard statistics
│   ├── upload/             # File upload processing
│   ├── models/             # MongoDB schemas
│   ├── utils/              # Utility functions
│   └── middleware/         # Authentication middleware
├── frontend/               # React frontend
│   ├── src/
│   │   ├── components/    # React components
│   │   ├── services/      # API services
│   │   ├── contexts/      # React contexts
│   │   └── charts/        # Chart components
│   └── public/
├── src/                    # Legacy Spring Boot (optional)
│   └── main/java/         # Java source code
├── vercel.json            # Vercel configuration
└── SERVERLESS_DEPLOYMENT.md  # Deployment guide
```

## 💰 Cost Comparison

| Service | Old Cost | New Cost | Savings |
|---------|----------|----------|---------|
| Backend | Railway: $5/mo | Vercel: $0 | $5/mo |
| Database | Included | MongoDB Atlas: $0 | $0 |
| Frontend | Vercel: $0 | Vercel: $0 | $0 |
| **Total** | **$5/month** | **$0/month** | **$60/year** |

## 🔧 Configuration

### Serverless Configuration (Current)
Environment variables in Vercel dashboard:
- `MONGODB_URI` - MongoDB Atlas connection string
- `JWT_SECRET` - Secret key for JWT tokens
- `JWT_EXPIRATION` - Token expiration time (e.g., 24h)

### Legacy Spring Boot Configuration (Optional)
Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:financedb  # Change for production DB
    username: sa
    password: password
```

## 🚀 Deployment

### Serverless Deployment (Recommended - $0/month)

Deploy as a fully serverless application with zero monthly costs:

**Backend + Frontend: Vercel (Free)**
- Unlimited serverless function invocations
- 100GB bandwidth/month
- Automatic HTTPS and global CDN
- Zero infrastructure management

**Database: MongoDB Atlas (Free)**
- 512MB storage (thousands of transactions)
- Shared cluster
- Automatic backups
- SSL connections

**📚 Complete Guide**: [SERVERLESS_DEPLOYMENT.md](./SERVERLESS_DEPLOYMENT.md)

**🔄 Migration Guide**: If migrating from Spring Boot: [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md)

### Legacy Deployment (Optional - Railway + PostgreSQL)

For those who prefer the Spring Boot backend:
- PostgreSQL database included
- Zero-cost with GitHub Student Pack

### 🚀 Quick Start Scripts

```bash
# Setup local development
./scripts/setup-dev.sh

# Deploy backend to Railway
./scripts/deploy-railway.sh
```

### 📋 Manual Deployment Steps

1. **Deploy Backend to Railway**:
   - Sign up at [railway.app](https://railway.app)
   - Connect GitHub repository
   - Add PostgreSQL database
   - Set environment variables:
     ```env
     SPRING_PROFILES_ACTIVE=prod
     CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
     ```

2. **Deploy Frontend to Vercel**:
   - Sign up at [vercel.com](https://vercel.com)
   - Import GitHub repository
   - Configure build settings:
     - Build Command: `cd frontend && CI=false npm run build`
     - Output Directory: `frontend/build`
   - Set environment variables:
     ```env
     CI=false
     REACT_APP_API_URL=https://your-backend.railway.app
     ```

### 💰 Cost: $0/month
All costs covered by GitHub Student Pack benefits:
- **Vercel**: Free tier
- **Railway**: $5/month credit for students
- **PostgreSQL**: Included with Railway

📚 **See [DEPLOYMENT.md](DEPLOYMENT.md) for complete deployment guide**  
⚡ **See [QUICK_DEPLOY.md](QUICK_DEPLOY.md) for 15-minute setup**

## 🔧 Configuration

### Database Configuration
Edit `src/main/resources/application.yml` to change database settings:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:financedb  # Change for production DB
    username: sa
    password: password
```

### CORS Configuration
CORS is configured globally via `CorsConfig.java` and can be customized using the `CORS_ALLOWED_ORIGINS` environment variable.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔍 SEO & Search Engine Optimization

This application is fully optimized for search engines including Google, Bing, and others.

### Implemented Features
- ✅ **robots.txt** - Allows search engine crawlers
- ✅ **sitemap.xml** - Helps search engines discover pages
- ✅ **Meta tags** - Rich descriptions and keywords
- ✅ **Open Graph tags** - Better social media sharing
- ✅ **Twitter Cards** - Enhanced Twitter previews
- ✅ **Structured data (JSON-LD)** - Rich search results
- ✅ **Canonical URLs** - Prevents duplicate content

### Setup Instructions
After deploying to production, update these files with your actual domain:
1. `frontend/public/robots.txt` - Update sitemap URL
2. `frontend/public/sitemap.xml` - Replace `your-domain.vercel.app` with your domain
3. `frontend/public/index.html` - Update all meta tag URLs

For detailed instructions, see [SEO_IMPLEMENTATION.md](./SEO_IMPLEMENTATION.md)

### Submit to Search Engines
1. **Google Search Console**: https://search.google.com/search-console
   - Add your site and submit sitemap
2. **Bing Webmaster Tools**: https://www.bing.com/webmasters
   - Add your site and submit sitemap

## 🆘 Support

If you encounter any issues or have questions:
1. Check the console logs for error messages
2. Ensure all dependencies are properly installed
3. Verify the Excel file format matches the expected structure
4. Check that both backend and frontend are running on correct ports

## 🔮 Future Enhancements

- User authentication and multi-user support
- Budget planning and alerts
- Export functionality (PDF reports)
- Mobile app development
- Integration with bank APIs
- Advanced analytics and predictions
- Custom category management
- Recurring transaction detection
