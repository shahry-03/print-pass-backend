#!/bin/bash

# ==============================================================================
# PrintPass Enterprise E2E API Testing Suite (via API Gateway)
# ==============================================================================

# API Gateway Base URL
# Pehle (Local):
# GATEWAY="http://localhost:8080/api/v1"
# Ab (Azure):
GATEWAY="https://api-gateway.greenisland-a3d56c74.malaysiawest.azurecontainerapps.io/api/v1"

# ANSI Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# Counters
TOTAL=0
PASSED=0
FAILED=0

# Admin Credentials
ADMIN_EMAIL="admin@printpass.com"
ADMIN_PASS="SuperSecureAdminPass123!"

# Generate Random Shop Email
RANDOM_SUFFIX=$((RANDOM % 10000))
SHOP_EMAIL="shahrayarsahito7+test${RANDOM_SUFFIX}@gmail.com"
SHOP_PASS="Password123"

# Check dependencies
if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ 'jq' is required. Run: sudo apt install jq${NC}"
    exit 1
fi

print_header() {
    echo -e "\n${BLUE}================================================================${NC}"
    echo -e "${BLUE}🚀 $1${NC}"
    echo -e "${BLUE}================================================================${NC}"
}

assert_equals() {
    local name="$1"
    local actual="$2"
    local expected="$3"
    local body="$4"

    TOTAL=$((TOTAL + 1))
    if [ "$actual" -eq "$expected" ]; then
        echo -e "${GREEN}  ✔ [PASS]${NC} $name (HTTP $actual)"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}  ✖ [FAIL]${NC} $name (Expected: $expected, Got: $actual)"
        echo -e "${YELLOW}    Response: $body${NC}"
        FAILED=$((FAILED + 1))
    fi
}

# ==============================================================================
# SUITE 1: ADMIN LOGIN & SWAGGER ACCESS
# ==============================================================================
print_header "SUITE 1: Auth Service (via Gateway)"

echo -e "${CYAN}-> Testing Admin Login...${NC}"
ADMIN_RES=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$ADMIN_EMAIL\", \"password\": \"$ADMIN_PASS\"}")

ADMIN_CODE=$(echo "$ADMIN_RES" | tail -n1)
ADMIN_BODY=$(echo "$ADMIN_RES" | sed '$d')
ADMIN_TOKEN=$(echo "$ADMIN_BODY" | jq -r '.token // empty')

assert_equals "Admin Login API" "$ADMIN_CODE" "200" "$ADMIN_BODY"

if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}❌ Admin token empty. Stopping tests.${NC}"
    exit 1
fi

# ==============================================================================
# SUITE 2: SHOP REGISTRATION & OTP FLOW
# ==============================================================================
print_header "SUITE 2: User Service - Shop Registration & OTP"

echo -e "${CYAN}-> Registering New Shop: $SHOP_EMAIL${NC}"
REG_RES=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/shops/register" \
    -H "Content-Type: application/json" \
    -d "{
      \"email\": \"$SHOP_EMAIL\",
      \"password\": \"$SHOP_PASS\",
      \"shopName\": \"Test Shop $RANDOM_SUFFIX\",
      \"ownerName\": \"Shahryar\",
      \"phone\": \"03001234567\",
      \"address\": \"Main Road\",
      \"city\": \"Karachi\"
    }")

REG_CODE=$(echo "$REG_RES" | tail -n1)
REG_BODY=$(echo "$REG_RES" | sed '$d')
SHOP_ID=$(echo "$REG_BODY" | jq -r '.id // empty')

assert_equals "Shop Registration API" "$REG_CODE" "201" "$REG_BODY"

echo -e "\n${YELLOW}⚠ ACTION REQUIRED: Verify OTP${NC}"
echo -e "1. Check email for $SHOP_EMAIL"
echo -e "2. Enter the 6-digit OTP below:"
read -p "OTP > " USER_OTP

echo -e "${CYAN}-> Verifying OTP...${NC}"
OTP_RES=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/otp/verify" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$SHOP_EMAIL\", \"otp\": \"$USER_OTP\"}")

OTP_CODE=$(echo "$OTP_RES" | tail -n1)
OTP_BODY=$(echo "$OTP_RES" | sed '$d')
assert_equals "OTP Verification API" "$OTP_CODE" "200" "$OTP_BODY"

# ==============================================================================
# SUITE 3: SHOPKEEPER LOGIN & ADMIN VERIFICATION
# ==============================================================================
print_header "SUITE 3: Shopkeeper Login & Admin Moderation"

echo -e "${CYAN}-> Testing Shopkeeper Login...${NC}"
SHOP_RES=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$SHOP_EMAIL\", \"password\": \"$SHOP_PASS\"}")

SHOP_CODE=$(echo "$SHOP_RES" | tail -n1)
SHOP_BODY=$(echo "$SHOP_RES" | sed '$d')
SHOP_TOKEN=$(echo "$SHOP_BODY" | jq -r '.token // empty')

assert_equals "Shopkeeper Login API" "$SHOP_CODE" "200" "$SHOP_BODY"

echo -e "${CYAN}-> Admin Verifying the Shop...${NC}"
VERIFY_RES=$(curl -s -w "\n%{http_code}" -X PATCH "$GATEWAY/admin/shops/$SHOP_ID/verify" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
VERIFY_CODE=$(echo "$VERIFY_RES" | tail -n1)
VERIFY_BODY=$(echo "$VERIFY_RES" | sed '$d')
assert_equals "Admin Verify Shop" "$VERIFY_CODE" "200" "$VERIFY_BODY"

# ==============================================================================
# SUITE 4: JOB SERVICE - UPLOAD, FETCH, PRINT
# ==============================================================================
print_header "SUITE 4: Job Service - Workflow"

echo "This is a dummy PDF for PrintPass E2E Testing." > test_payload.pdf

echo -e "${CYAN}-> Uploading File (Student)...${NC}"
UPLOAD_RES=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/jobs/upload" \
    -F "file=@test_payload.pdf")

UPLOAD_CODE=$(echo "$UPLOAD_RES" | tail -n1)
UPLOAD_BODY=$(echo "$UPLOAD_RES" | sed '$d')
JOB_PIN=$(echo "$UPLOAD_BODY" | jq -r '.secretKey // empty')

assert_equals "Student File Upload" "$UPLOAD_CODE" "201" "$UPLOAD_BODY"
rm test_payload.pdf

if [ -n "$JOB_PIN" ]; then
    echo -e "${GREEN}   Captured PIN: $JOB_PIN${NC}"

    echo -e "${CYAN}-> Fetching File by PIN (Shopkeeper)...${NC}"
    FETCH_RES=$(curl -s -w "\n%{http_code}" -X GET "$GATEWAY/jobs/$JOB_PIN" \
        -H "Authorization: Bearer $SHOP_TOKEN")
    FETCH_CODE=$(echo "$FETCH_RES" | tail -n1)
    FETCH_BODY=$(echo "$FETCH_RES" | sed '$d')
    FILE_URL=$(echo "$FETCH_BODY" | jq -r '.fileUrl // empty')
    
    assert_equals "Shopkeeper Fetch File" "$FETCH_CODE" "200" "$FETCH_BODY"
    if [ -n "$FILE_URL" ]; then
        echo -e "${GREEN}   Backblaze URL: $FILE_URL${NC}"
    fi

    echo -e "${CYAN}-> Marking Job as Printed...${NC}"
    PRINT_RES=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/jobs/$JOB_PIN/print" \
        -H "Authorization: Bearer $SHOP_TOKEN")
    PRINT_CODE=$(echo "$PRINT_RES" | tail -n1)
    PRINT_BODY=$(echo "$PRINT_RES" | sed '$d')
    assert_equals "Print Job API" "$PRINT_CODE" "200" "$PRINT_BODY"
fi

# ==============================================================================
# SUITE 5: NEGATIVE TESTING (SECURITY)
# ==============================================================================
print_header "SUITE 5: Negative & Security Testing"

echo -e "${CYAN}-> Testing Access to Admin Route without Token...${NC}"
SEC_RES=$(curl -s -w "\n%{http_code}" -X GET "$GATEWAY/admin/shops")
SEC_CODE=$(echo "$SEC_RES" | tail -n1)
# Spring Security returns 403 Forbidden if token is missing
if [ "$SEC_CODE" -eq 403 ] || [ "$SEC_CODE" -eq 401 ]; then
    echo -e "${GREEN}  ✔ [PASS] Unauthorized Access Blocked (HTTP $SEC_CODE)${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "${RED}  ✖ [FAIL] Security Flaw! Unauthorized Access Allowed (HTTP $SEC_CODE)${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))

# ==============================================================================
# SUMMARY
# ==============================================================================
echo -e "\n${BLUE}================================================================${NC}"
echo -e "${BLUE}📊 FINAL TEST SUMMARY${NC}"
echo -e "${BLUE}================================================================${NC}"
echo -e " Total Tests : $TOTAL"
echo -e " ${GREEN}Passed      : $PASSED${NC}"
echo -e " ${RED}Failed      : $FAILED${NC}"
echo -e "${BLUE}================================================================${NC}"

if [ "$FAILED" -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL SYSTEMS OPERATIONAL! 🎉${NC}"
else
    echo -e "${RED}⚠ SOME TESTS FAILED. CHECK LOGS. ⚠${NC}"
fi