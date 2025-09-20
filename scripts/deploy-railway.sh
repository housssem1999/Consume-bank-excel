#!/bin/bash

# Deploy to Railway Script
# Usage: ./scripts/deploy-railway.sh

set -e

echo "🚀 Deploying Personal Finance Dashboard to Railway..."

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Installing..."
    npm install -g @railway/cli
    echo "✅ Railway CLI installed successfully"
fi

# Login to Railway (if not already logged in)
echo "🔐 Logging into Railway..."
railway login

# Create project if it doesn't exist
echo "📦 Creating Railway project..."
railway project create finance-dashboard --team personal || echo "Project may already exist, continuing..."

# Link to existing project if needed
railway link

# Add PostgreSQL database
echo "🗄️ Adding PostgreSQL database..."
railway add postgresql || echo "Database may already exist, continuing..."

# Set environment variables
echo "⚙️ Setting environment variables..."
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set CORS_ALLOWED_ORIGINS=https://your-app.vercel.app

echo "📝 Note: Update CORS_ALLOWED_ORIGINS with your actual Vercel URL after frontend deployment"

# Deploy application
echo "🚀 Deploying application..."
railway up --detach

echo "✅ Deployment completed!"
echo ""
echo "📋 Next steps:"
echo "   1. Update CORS_ALLOWED_ORIGINS with your frontend URL"
echo "   2. Update your frontend's REACT_APP_API_URL"
echo "   3. Test your deployment"
echo ""
echo "🔗 Railway Dashboard: https://railway.app/dashboard"
echo "📝 Remember to update environment variables with actual URLs"