#!/bin/bash

# Deploy to Railway Script
# Usage: ./scripts/deploy-railway.sh

set -e

echo "🚀 Deploying Personal Finance Dashboard to Railway..."

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Installing..."
    npm install -g @railway/cli
fi

# Login to Railway (if not already logged in)
echo "🔐 Logging into Railway..."
railway login

# Create project if it doesn't exist
echo "📦 Creating Railway project..."
railway project create finance-dashboard --team personal

# Add PostgreSQL database
echo "🗄️ Adding PostgreSQL database..."
railway add postgresql

# Set environment variables
echo "⚙️ Setting environment variables..."
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set CORS_ALLOWED_ORIGINS=https://your-app.vercel.app

# Deploy application
echo "🚀 Deploying application..."
railway up

echo "✅ Deployment completed!"
echo "📋 Next steps:"
echo "   1. Update CORS_ALLOWED_ORIGINS with your frontend URL"
echo "   2. Update your frontend's REACT_APP_API_URL"
echo "   3. Test your deployment"
echo ""
echo "🔗 Railway Dashboard: https://railway.app/dashboard"