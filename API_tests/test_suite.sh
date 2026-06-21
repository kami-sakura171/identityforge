#!/bin/bash
# API test suite - runs curl-based endpoint tests against a running instance

API_BASE="${API_BASE:-http://localhost:8080}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

echo "API Base URL: $API_BASE"
echo ""

# Helper: run a test and report result
run_test() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_status="$5"
    local token="$6"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    local headers=(-s -o /dev/null -w "%{http_code}")
    if [ -n "$token" ]; then
        headers+=(-H "Authorization: Bearer $token")
    fi
    headers+=(-H "Content-Type: application/json")

    if [ "$method" = "POST" ] || [ "$method" = "PUT" ]; then
        local status=$(curl "${headers[@]}" -X "$method" -d "$data" "$API_BASE$url")
    else
        local status=$(curl "${headers[@]}" -X "$method" "$API_BASE$url")
    fi

    if [ "$status" = "$expected_status" ]; then
        echo "  ✓ $name (HTTP $status)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo "  ✗ $name (expected $expected_status, got $status)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

echo "=== Auth API Tests ==="

# Health check
run_test "Health check" "GET" "/api/health" "" "200"

# Security questions
run_test "Get security questions" "GET" "/api/security-questions" "" "200"

# Registration
run_test "Register valid user" "POST" "/api/auth/register" \
    '{"username":"testuser1","password":"TestPass123!","firstName":"Test","lastName":"User","displayName":"Test User","dateOfBirth":"01/15/1990","securityQuestionId":1,"securityAnswer":"fluffy","acceptTos":true}' \
    "200"

run_test "Register duplicate username" "POST" "/api/auth/register" \
    '{"username":"testuser1","password":"TestPass123!","firstName":"Test","lastName":"User","displayName":"Test User","dateOfBirth":"01/15/1990","securityQuestionId":1,"securityAnswer":"fluffy","acceptTos":true}' \
    "400"

run_test "Register weak password" "POST" "/api/auth/register" \
    '{"username":"testuser2","password":"weak","firstName":"Test","lastName":"User","displayName":"Test User 2","dateOfBirth":"01/15/1990","securityQuestionId":1,"securityAnswer":"fluffy","acceptTos":true}' \
    "400"

# Login
LOGIN_RESULT=$(curl -s -X POST "$API_BASE/api/auth/login" -H "Content-Type: application/json" -d '{"username":"testuser1","password":"TestPass123!"}')
ACCESS_TOKEN=$(echo "$LOGIN_RESULT" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo "  Login result: token=$( [ -n "$ACCESS_TOKEN" ] && echo 'obtained' || echo 'failed' )"

run_test "Login valid" "POST" "/api/auth/login" \
    '{"username":"testuser1","password":"TestPass123!"}' \
    "200"

run_test "Login bad password" "POST" "/api/auth/login" \
    '{"username":"testuser1","password":"WrongPassword1!"}' \
    "401"

run_test "Login non-existent user" "POST" "/api/auth/login" \
    '{"username":"nonexistent","password":"TestPass123!"}' \
    "401"

# Admin login
ADMIN_RESULT=$(curl -s -X POST "$API_BASE/api/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"Admin@12345"}')
ADMIN_TOKEN=$(echo "$ADMIN_RESULT" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo "  Admin login: token=$( [ -n "$ADMIN_TOKEN" ] && echo 'obtained' || echo 'failed' )"

# Token validate
if [ -n "$ACCESS_TOKEN" ]; then
    run_test "Validate token" "GET" "/api/auth/validate" "" "200" "$ACCESS_TOKEN"
fi

echo ""
echo "=== Customer API Tests ==="

if [ -n "$ACCESS_TOKEN" ]; then
    run_test "Get profile" "GET" "/api/customer/profile" "" "200" "$ACCESS_TOKEN"
    run_test "Get notifications" "GET" "/api/customer/notifications" "" "200" "$ACCESS_TOKEN"
    run_test "Get unread count" "GET" "/api/customer/notifications/unread-count" "" "200" "$ACCESS_TOKEN"
    run_test "Get preferences" "GET" "/api/customer/preferences" "" "200" "$ACCESS_TOKEN"
    run_test "Get consents status" "GET" "/api/customer/consents/status" "" "200" "$ACCESS_TOKEN"
    run_test "Get roles" "GET" "/api/customer/roles" "" "200" "$ACCESS_TOKEN"
fi

echo ""
echo "=== Admin API Tests ==="

if [ -n "$ADMIN_TOKEN" ]; then
    run_test "Get admin dashboard" "GET" "/api/admin/dashboard" "" "200" "$ADMIN_TOKEN"
    run_test "Get registration trend" "GET" "/api/admin/dashboard/registration-trend" "" "200" "$ADMIN_TOKEN"
    run_test "List customers" "GET" "/api/admin/customers" "" "200" "$ADMIN_TOKEN"
    run_test "Get custom fields" "GET" "/api/admin/custom-fields" "" "200" "$ADMIN_TOKEN"
    run_test "Get ToS versions" "GET" "/api/admin/tos" "" "200" "$ADMIN_TOKEN"
    run_test "Get session config" "GET" "/api/admin/session-config" "" "200" "$ADMIN_TOKEN"
fi

echo ""
echo "=== Security Tests ==="

# Test unauthorized access
run_test "Access without token" "GET" "/api/customer/profile" "" "401"

# Test customer trying admin endpoint
if [ -n "$ACCESS_TOKEN" ]; then
    run_test "Customer accessing admin endpoint" "GET" "/api/admin/dashboard" "" "403" "$ACCESS_TOKEN"
fi

echo ""
echo "========================================="
echo "  API Test Summary"
echo "========================================="
echo "  Total:  $TOTAL_TESTS"
echo "  Passed: $PASSED_TESTS"
echo "  Failed: $FAILED_TESTS"
echo "========================================="

if [ "$FAILED_TESTS" -eq 0 ]; then
    echo "  ALL API TESTS PASSED ✓"
    exit 0
else
    echo "  SOME API TESTS FAILED ✗"
    exit 1
fi
