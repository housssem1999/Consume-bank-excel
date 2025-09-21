#!/bin/bash

# Vercel Deployment Validation Script
# Usage: ./scripts/test-vercel-deployment.sh <vercel-url>

set -e

VERCEL_URL="$1"

if [ -z "$VERCEL_URL" ]; then
    echo "❌ Usage: $0 <vercel-url>"
    echo "   Example: $0 https://finance-dashboard-abc123.vercel.app"
    exit 1
fi

echo "🔍 Testing Vercel deployment at: $VERCEL_URL"
echo ""

# Test 1: Main page loads
echo "1️⃣ Testing main page..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$VERCEL_URL")
if [ "$HTTP_STATUS" = "200" ]; then
    echo "   ✅ Main page loads successfully (HTTP $HTTP_STATUS)"
else
    echo "   ❌ Main page failed to load (HTTP $HTTP_STATUS)"
    exit 1
fi

# Test 2: Static assets are served correctly
echo "2️⃣ Testing static assets..."
STATIC_JS_URL=$(curl -s "$VERCEL_URL" | grep -o '/static/js/[^"]*\.js' | head -1)
if [ -n "$STATIC_JS_URL" ]; then
    JS_HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$VERCEL_URL$STATIC_JS_URL")
    CONTENT_TYPE=$(curl -s -I "$VERCEL_URL$STATIC_JS_URL" | grep -i content-type | head -1)
    
    if [ "$JS_HTTP_STATUS" = "200" ] && echo "$CONTENT_TYPE" | grep -q "javascript\|text/plain"; then
        echo "   ✅ JavaScript files served correctly (HTTP $JS_HTTP_STATUS)"
    else
        echo "   ❌ JavaScript files not served correctly (HTTP $JS_HTTP_STATUS)"
        echo "   Content-Type: $CONTENT_TYPE"
        exit 1
    fi
else
    echo "   ❌ Could not find JavaScript files in HTML"
    exit 1
fi

# Test 3: CSS assets are served correctly
echo "3️⃣ Testing CSS assets..."
STATIC_CSS_URL=$(curl -s "$VERCEL_URL" | grep -o '/static/css/[^"]*\.css' | head -1)
if [ -n "$STATIC_CSS_URL" ]; then
    CSS_HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$VERCEL_URL$STATIC_CSS_URL")
    
    if [ "$CSS_HTTP_STATUS" = "200" ]; then
        echo "   ✅ CSS files served correctly (HTTP $CSS_HTTP_STATUS)"
    else
        echo "   ❌ CSS files not served correctly (HTTP $CSS_HTTP_STATUS)"
        exit 1
    fi
else
    echo "   ⚠️  No CSS files found (this might be okay)"
fi

# Test 4: Favicon is accessible
echo "4️⃣ Testing favicon..."
FAVICON_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$VERCEL_URL/favicon.ico")
if [ "$FAVICON_STATUS" = "200" ]; then
    echo "   ✅ Favicon accessible (HTTP $FAVICON_STATUS)"
else
    echo "   ⚠️  Favicon not found (HTTP $FAVICON_STATUS) - not critical"
fi

# Test 5: SPA routing works (any path should return index.html)
echo "5️⃣ Testing SPA routing..."
SPA_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$VERCEL_URL/upload")
if [ "$SPA_STATUS" = "200" ]; then
    echo "   ✅ SPA routing works correctly (HTTP $SPA_STATUS)"
else
    echo "   ❌ SPA routing failed (HTTP $SPA_STATUS)"
    exit 1
fi

# Test 6: Check for common error patterns
echo "6️⃣ Checking for common errors..."
PAGE_CONTENT=$(curl -s "$VERCEL_URL")
if echo "$PAGE_CONTENT" | grep -q "Uncaught SyntaxError"; then
    echo "   ❌ Found 'Uncaught SyntaxError' in page content"
    exit 1
elif echo "$PAGE_CONTENT" | grep -q "<div id=\"root\">"; then
    echo "   ✅ React root element found"
else
    echo "   ⚠️  React root element not found - check if page loads correctly"
fi

echo ""
echo "🎉 All tests passed! Vercel deployment appears to be working correctly."
echo ""
echo "📋 Manual verification checklist:"
echo "   □ Open $VERCEL_URL in browser"
echo "   □ Check that the Finance Dashboard loads without white screen"
echo "   □ Test navigation (Dashboard, Upload, Transactions)"
echo "   □ Check browser console for JavaScript errors"
echo "   □ Verify API calls work (if backend is running)"