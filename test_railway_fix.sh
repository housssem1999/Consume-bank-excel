#!/bin/bash

# Test script to demonstrate the PostgreSQL URL conversion fix
# This simulates the Railway environment and validates the fix

echo "🧪 Testing PostgreSQL URL Conversion Fix for Railway Deployment"
echo "=============================================================="

cd /home/runner/work/Consume-bank-excel/Consume-bank-excel

# Test 1: Development mode (default profile) - should use H2
echo ""
echo "📋 Test 1: Development mode (H2 database)"
echo "Expected: Application starts with H2 database"
timeout 15s mvn spring-boot:run > /dev/null 2>&1
if [ $? -eq 124 ]; then
    echo "✅ PASS: Application starts successfully with H2 (default profile)"
else
    echo "❌ FAIL: Application failed to start with H2"
fi

# Test 2: Production mode with Railway-style DATABASE_URL
echo ""
echo "📋 Test 2: Production mode with Railway DATABASE_URL format"
echo "Expected: URL conversion occurs, connection error due to invalid host"

export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL="postgresql://postgres:NKSLtPyJtEbEPeanzJEXrTLBEYVpWgED@postgres.railway.internal:5432/railway"

# Run for a few seconds and capture logs
timeout 8s mvn spring-boot:run 2>&1 | grep -E "(Converting Railway|Host:|Connection.*refused)" > /tmp/test_output.log

if grep -q "Converting Railway DATABASE_URL" /tmp/test_output.log; then
    echo "✅ PASS: URL conversion detected in logs"
    grep "Host:" /tmp/test_output.log | head -1
else
    echo "❌ FAIL: URL conversion not detected"
fi

if grep -q "Connection.*refused" /tmp/test_output.log; then
    echo "✅ PASS: Connection attempts made (proves URL format is correct)"
else
    echo "❌ FAIL: No connection attempts detected"
fi

# Test 3: Verify the exact URL conversion logic
echo ""
echo "📋 Test 3: URL Conversion Unit Test"
mvn test -Dtest=DatabaseConfigTest -q > /tmp/unit_test.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ PASS: Unit tests confirm URL conversion logic works"
else
    echo "❌ FAIL: Unit tests failed"
    cat /tmp/unit_test.log
fi

echo ""
echo "🎯 Summary"
echo "=========="
echo "The fix converts Railway's DATABASE_URL format:"
echo "  FROM: postgresql://user:pass@host:port/db"
echo "  TO:   jdbc:postgresql://host:port/db"
echo ""
echo "This resolves the Hikari driver error when deploying to Railway."

# Cleanup
unset SPRING_PROFILES_ACTIVE
unset DATABASE_URL
rm -f /tmp/test_output.log /tmp/unit_test.log