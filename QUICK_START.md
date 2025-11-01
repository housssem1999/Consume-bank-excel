# Quick Start Guide - Serverless Deployment

Get your Personal Finance Dashboard running in **10 minutes** with **zero monthly costs**!

## 🎯 What You'll Get

- ✅ Full-stack personal finance app
- ✅ Serverless backend on Vercel (FREE)
- ✅ MongoDB Atlas database (FREE 512MB)
- ✅ React frontend on Vercel (FREE)
- ✅ **Total Cost: $0/month**

## ⚡ 3-Step Deployment

### Step 1: MongoDB Atlas (3 minutes)

1. **Sign up** at [mongodb.com/cloud/atlas/register](https://www.mongodb.com/cloud/atlas/register)
   - Use Google/GitHub for quick signup
   - No credit card required

2. **Create cluster**:
   - Click "Build a Database"
   - Choose "Shared" (FREE)
   - Select any cloud provider & region
   - Click "Create Cluster"

3. **Create user**:
   - Go to "Database Access"
   - Click "Add New Database User"
   - Username: `financeuser`
   - Password: Generate a secure password (SAVE THIS!)
   - Role: "Read and write to any database"

4. **Allow connections**:
   - Go to "Network Access"
   - Click "Add IP Address"
   - Click "Allow Access from Anywhere"
   - Confirm

5. **Get connection string**:
   - Go to "Database" → Click "Connect"
   - Choose "Connect your application"
   - Copy the connection string
   - Replace `<password>` with your actual password
   - Add `/finance-dashboard` at the end

   Example:
   ```
   mongodb+srv://financeuser:YOUR_PASSWORD@cluster0.abcde.mongodb.net/finance-dashboard?retryWrites=true&w=majority
   ```

### Step 2: Deploy to Vercel (5 minutes)

1. **Go to** [vercel.com](https://vercel.com)
   - Sign in with GitHub

2. **Import project**:
   - Click "Add New..." → "Project"
   - Select this repository
   - Click "Import"

3. **Configure**:
   - Framework: Auto-detected ✅
   - Root Directory: `/`
   - Build Command: Auto-detected ✅
   - Output Directory: `frontend/build` ✅

4. **Add environment variables**:
   Click "Environment Variables" and add these three:

   **Variable 1:**
   - Name: `MONGODB_URI`
   - Value: Your MongoDB connection string from Step 1

   **Variable 2:**
   - Name: `JWT_SECRET`
   - Value: Generate with this command:
     ```bash
     node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
     ```
     Or use any random 64-character string

   **Variable 3:**
   - Name: `JWT_EXPIRATION`
   - Value: `24h`

5. **Deploy**:
   - Click "Deploy"
   - Wait 2-3 minutes
   - Done! 🎉

### Step 3: Use Your App (2 minutes)

1. **Open your app**:
   - Vercel will show you the URL (e.g., `https://your-app.vercel.app`)
   - Click to open

2. **Register**:
   - Click "Register" or "Sign Up"
   - Create your account
   - Default categories are automatically created!

3. **Upload transactions**:
   - Click "Upload" or "Import"
   - Select your Excel file (see format below)
   - Watch your dashboard populate!

## 📊 Excel File Format

Create an Excel file with this structure:

| Date       | Description      | Amount   | Reference |
|------------|-----------------|----------|-----------|
| 2024-01-15 | Grocery Store   | -85.50   | TXN001    |
| 2024-01-16 | Salary Deposit  | 3500.00  | SAL001    |
| 2024-01-17 | Gas Station     | -45.20   | TXN002    |

**Rules:**
- First row = headers
- Date: YYYY-MM-DD format
- Amount: Negative for expenses, positive for income
- Reference: Optional

## 🎨 Default Categories

Your app comes with 11 categories:
- 🍽️ Food & Dining
- 🚗 Transportation
- 🛍️ Shopping
- 💡 Bills & Utilities
- 🎬 Entertainment
- 🏥 Healthcare
- 💰 Income
- 💳 Transfer
- 🏠 Housing
- 🛡️ Insurance
- 📦 Other

## 🔧 Custom Domain (Optional)

1. In Vercel dashboard, go to your project
2. Click "Settings" → "Domains"
3. Add your custom domain
4. Follow DNS instructions
5. Done! SSL certificate is automatic

## 🐛 Troubleshooting

### "Cannot connect to database"
- Check MongoDB connection string is correct
- Verify password doesn't contain special characters that need URL encoding
- Ensure IP address 0.0.0.0/0 is whitelisted in MongoDB Network Access

### "Token expired" or login issues
- Clear browser cache and cookies
- Try registering a new account
- Check JWT_SECRET is set in Vercel

### Excel upload fails
- Verify file is .xlsx or .xls format
- Check file size is under 4MB
- Ensure columns are: Date, Description, Amount, Reference

### Application not loading
- Check Vercel deployment logs
- Verify all environment variables are set
- Try redeploying from Vercel dashboard

## 📱 Mobile Access

Your app works on mobile browsers! Just visit your Vercel URL on your phone.

For a better mobile experience, add to home screen:
- **iOS**: Safari → Share → Add to Home Screen
- **Android**: Chrome → Menu → Add to Home Screen

## 💡 Pro Tips

1. **Bookmark your dashboard** for quick access
2. **Upload regularly** - weekly or monthly imports keep data fresh
3. **Set budgets** for categories to track spending
4. **Export data** by connecting to MongoDB directly if needed
5. **Share carefully** - keep your login credentials secure

## 🚀 What's Next?

- **Customize categories**: Add your own spending categories
- **Set budgets**: Track spending against monthly budgets
- **Analyze trends**: Use the dashboard to spot patterns
- **Mobile access**: Access from anywhere on your phone
- **Export data**: All your data is in MongoDB, easy to export

## 📚 More Information

- **Full Documentation**: [SERVERLESS_DEPLOYMENT.md](./SERVERLESS_DEPLOYMENT.md)
- **API Reference**: [api/README.md](./api/README.md)
- **Migration Guide**: [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md)
- **Main README**: [README.md](./README.md)

## 🎉 Congratulations!

You now have a fully functional, serverless personal finance dashboard running at zero cost!

**Total time**: ~10 minutes  
**Total cost**: $0/month  
**Total awesomeness**: Unlimited! 🌟

---

Need help? Create an issue on GitHub or check the troubleshooting section above.
