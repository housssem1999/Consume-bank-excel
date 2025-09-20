#!/bin/bash

# Deploy to Heroku Script
# Usage: ./scripts/deploy-heroku.sh your-app-name

set -e

APP_NAME=${1:-finance-dashboard-$(date +%s)}

echo "🚀 Deploying Personal Finance Dashboard to Heroku..."
echo "📱 App name: $APP_NAME"

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo "❌ Heroku CLI not found. Please install it first:"
    echo "   npm install -g heroku"
    exit 1
fi

# Login to Heroku
echo "🔐 Logging into Heroku..."
heroku login

# Create Heroku app
echo "📦 Creating Heroku app..."
heroku create $APP_NAME

# Add PostgreSQL addon
echo "🗄️ Adding PostgreSQL addon..."
heroku addons:create heroku-postgresql:hobby-dev -a $APP_NAME

# Set environment variables
echo "⚙️ Setting environment variables..."
heroku config:set SPRING_PROFILES_ACTIVE=prod -a $APP_NAME
heroku config:set CORS_ALLOWED_ORIGINS=https://$APP_NAME.herokuapp.com -a $APP_NAME

# Add buildpacks
echo "🔨 Adding buildpacks..."
heroku buildpacks:add heroku/java -a $APP_NAME
heroku buildpacks:add heroku/nodejs -a $APP_NAME

# Deploy application
echo "🚀 Deploying to Heroku..."
git push heroku main

echo "✅ Deployment completed!"
echo "📋 App Details:"
echo "   🌐 URL: https://$APP_NAME.herokuapp.com"
echo "   📊 Dashboard: https://dashboard.heroku.com/apps/$APP_NAME"
echo ""
echo "🔗 Next steps:"
echo "   1. Wait for deployment to complete"
echo "   2. Check logs: heroku logs --tail -a $APP_NAME"
echo "   3. Test your application"