# Personal Finance Dashboard

A comprehensive personal finance management application that helps you track your income, expenses, and financial trends by importing bank statements from Excel files.

## 🚀 Features

### Backend (Spring Boot)
- **Excel File Processing**: Import bank statements from .xlsx/.xls files
- **Automatic Categorization**: Smart categorization of transactions based on description
- **Financial Statistics**: Calculate income, expenses, trends, and category breakdowns
- **RESTful API**: Clean API endpoints for frontend integration
- **H2 Database**: In-memory database for development (easily configurable for production)

### Frontend (React)
- **Interactive Dashboard**: Visual overview of your financial data
- **Chart Visualizations**: Pie charts, doughnut charts, and line graphs using Chart.js
- **File Upload Interface**: Drag-and-drop Excel file upload
- **Transaction Management**: View and filter transaction history
- **Responsive Design**: Works on desktop and mobile devices

### Key Statistics
- Monthly and yearly income/expense summaries
- Category-wise spending analysis
- Monthly trends and patterns
- Top expense categories
- Average monthly expenses
- Net income calculations

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.2.0** - Main framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Apache POI** - Excel file processing
- **Maven** - Dependency management

### Frontend
- **React 18** - Frontend framework
- **Ant Design** - UI component library
- **Chart.js & react-chartjs-2** - Data visualization
- **Axios** - HTTP client
- **React Router** - Navigation

## 📋 Prerequisites

- **Java 17** or higher
- **Node.js 16** or higher
- **npm** or **yarn**
- **Maven 3.6** or higher

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd finance-dashboard
```

### 2. Backend Setup

#### Install Dependencies
```bash
mvn clean install
```

#### Run the Spring Boot Application
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### Access H2 Database Console (Optional)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: `password`

### 3. Frontend Setup

#### Navigate to Frontend Directory
```bash
cd frontend
```

#### Install Dependencies
```bash
npm install
```

#### Start the React Application
```bash
npm start
```

The frontend will start on `http://localhost:3000`

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

### Dashboard
- `GET /api/dashboard/summary` - Get financial summary
- `GET /api/dashboard/summary/current-month` - Current month summary
- `GET /api/dashboard/summary/current-year` - Current year summary
- `GET /api/dashboard/transactions` - Get transactions with filters
- `GET /api/dashboard/top-expenses` - Top expense categories
- `GET /api/dashboard/stats` - Quick statistics

### File Upload
- `POST /api/upload/excel` - Upload Excel file
- `GET /api/upload/sample-format` - Get expected format info

### Categories
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create new category

## 🏗️ Project Structure

```
finance-dashboard/
├── src/main/java/com/finance/dashboard/
│   ├── controller/          # REST controllers
│   ├── service/            # Business logic
│   ├── model/              # Entity classes
│   ├── repository/         # Data access layer
│   └── dto/                # Data transfer objects
├── src/main/resources/
│   └── application.yml     # Configuration
├── frontend/
│   ├── src/
│   │   ├── components/     # React components
│   │   ├── services/       # API services
│   │   └── charts/         # Chart components
│   └── public/
└── pom.xml                 # Maven configuration
```

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

**✅ Railway PostgreSQL**: The application automatically handles Railway's DATABASE_URL format conversion. See [RAILWAY_FIX.md](RAILWAY_FIX.md) for details.

### CORS Configuration
The backend is configured to allow requests from `http://localhost:3000`. Update the `@CrossOrigin` annotations in controllers for production.

## 🚀 Deployment

Deploy your Personal Finance Dashboard using **Vercel (Frontend) + Railway (Backend)** - the recommended deployment option leveraging free resources from GitHub Student Pack.

### Quick Deployment: Vercel + Railway

**Frontend: Vercel (Free)**
- 100GB bandwidth/month
- Unlimited sites and custom domains
- Automatic HTTPS and global CDN

**Backend: Railway ($5/month student credit)**
- 500 hours runtime with 1GB RAM
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
