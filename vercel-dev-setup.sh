#!/bin/bash

# Vercel Development Setup Script
# This script helps set up local development with Vercel Dev

echo "🚀 Setting up Vercel Development Environment"
echo ""

# Check if Vercel CLI is installed
if ! command -v vercel &> /dev/null; then
    echo "📦 Installing Vercel CLI..."
    npm install -g vercel
else
    echo "✅ Vercel CLI is already installed"
fi

# Check if .env exists
if [ ! -f .env ]; then
    echo ""
    echo "⚠️  No .env file found!"
    echo "📝 Creating .env from .env.example..."
    
    if [ -f .env.example ]; then
        cp .env.example .env
        echo "✅ Created .env file"
        echo ""
        echo "⚠️  IMPORTANT: Edit .env and add your MongoDB connection string:"
        echo "   - MONGODB_URI=your-connection-string"
        echo "   - JWT_SECRET=your-secret-key"
        echo ""
    else
        echo "❌ .env.example not found"
        exit 1
    fi
else
    echo "✅ .env file exists"
fi

# Check if API dependencies are installed
if [ ! -d "api/node_modules" ]; then
    echo ""
    echo "📦 Installing API dependencies..."
    cd api
    npm install
    cd ..
    echo "✅ API dependencies installed"
else
    echo "✅ API dependencies are installed"
fi

# Check if frontend dependencies are installed
if [ ! -d "frontend/node_modules" ]; then
    echo ""
    echo "📦 Installing frontend dependencies..."
    cd frontend
    npm install
    cd ..
    echo "✅ Frontend dependencies installed"
else
    echo "✅ Frontend dependencies are installed"
fi

echo ""
echo "✨ Setup complete!"
echo ""
echo "📋 Next steps:"
echo "   1. Edit .env and add your MongoDB connection string"
echo "   2. Run: vercel dev"
echo "   3. Open http://localhost:3000"
echo ""
echo "💡 For MongoDB Atlas:"
echo "   1. Sign up at https://www.mongodb.com/cloud/atlas/register"
echo "   2. Create a free cluster"
echo "   3. Get connection string and add to .env"
echo ""
