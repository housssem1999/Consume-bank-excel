#!/bin/bash

# Local Development Setup Script
# Usage: ./scripts/setup-dev.sh

set -e

echo "🚀 Setting up Personal Finance Dashboard for local development..."

# Check prerequisites
echo "🔍 Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6 or higher."
    exit 1
fi

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js 16 or higher."
    exit 1
fi

# Check npm
if ! command -v npm &> /dev/null; then
    echo "❌ npm not found. Please install npm."
    exit 1
fi

echo "✅ All prerequisites found!"

# Install backend dependencies
echo "📦 Installing backend dependencies..."
mvn clean install -DskipTests

# Install frontend dependencies
echo "📦 Installing frontend dependencies..."
cd frontend
npm install
cd ..

# Create environment files
echo "⚙️ Creating environment files..."

# Backend environment (development)
cat > .env.development << EOF
# Development Environment Variables
SPRING_PROFILES_ACTIVE=default
DB_PASSWORD=
PORT=8080
EOF

# Frontend environment (development)
cat > frontend/.env.local << EOF
# Development Environment Variables
REACT_APP_API_URL=http://localhost:8080
REACT_APP_ENVIRONMENT=development
EOF

echo "✅ Setup completed successfully!"
echo ""
echo "🚀 To start the application:"
echo "   Backend:  mvn spring-boot:run"
echo "   Frontend: cd frontend && CI=false npm start"
echo ""
echo "🌐 URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend:  http://localhost:8080"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""
echo "📋 H2 Database Connection:"
echo "   JDBC URL: jdbc:h2:mem:financedb"
echo "   Username: sa"
echo "   Password: (empty)"