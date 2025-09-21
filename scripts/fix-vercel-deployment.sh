#!/bin/bash

# Quick Vercel Deployment Fix Script
# Usage: ./scripts/fix-vercel-deployment.sh

set -e

echo "🔧 Fixing Vercel deployment configuration..."

# Backup current vercel.json
cp vercel.json vercel.json.backup

# Create minimal working vercel.json
cat > vercel.json << 'EOF'
{
  "buildCommand": "cd frontend && CI=false npm run build",
  "outputDirectory": "frontend/build",
  "routes": [
    {
      "src": "/static/(.*)",
      "dest": "/static/$1"
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ]
}
EOF

echo "✅ Updated vercel.json with minimal configuration"

# Test build locally
echo "🧪 Testing build locally..."
cd frontend
CI=false npm run build

if [ $? -eq 0 ]; then
    echo "✅ Local build successful"
else
    echo "❌ Local build failed"
    exit 1
fi

cd ..

echo ""
echo "🚀 Next steps:"
echo "1. Commit and push this change"
echo "2. Check Vercel deployment logs"
echo "3. Verify environment variables in Vercel dashboard:"
echo "   - CI=false"
echo "   - REACT_APP_API_URL=https://your-backend-url"
echo ""
echo "📖 For detailed troubleshooting, see VERCEL_TROUBLESHOOTING.md"