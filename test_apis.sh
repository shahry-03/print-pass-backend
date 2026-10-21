#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

if ! command -v jq &> /dev/null; then
    echo -e "${RED}Error: 'jq' is not installed. Please run: sudo apt install jq${NC}"
    exit 1
fi

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}🚀 Starting PrintPass API Tests (Skipping OTP)${NC}"
echo -e "${BLUE}====================================================${NC}"

SHOP_EMAIL="shahrayarsahito7@gmail.com" # Resend limit ki wajah se yahi rahega
SHOP_PASS="Password123"

# 1. ADMIN LOGIN
echo -e "\n${YELLOW}[1] Testing Admin Login...${NC}"
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"email": "admin@printpass.com", "password": "SuperSecureAdminPass123!"}')
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | jq -r '.token')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}❌ Admin Login Failed! Response: $ADMIN_RESPONSE${NC}"; exit 1
else
    echo -e "${GREEN}✅ Admin Login Successful!${NC}"
fi

# 2. SHOP REGISTRATION
echo -e "\n${YELLOW}[2] Testing Shop Registration (Email: $SHOP_EMAIL)...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/shops/register" -H "Content-Type: application/json" -d "{
    \"email\": \"$SHOP_EMAIL\", \"password\": \"$SHOP_PASS\", \"shopName\": \"Test Print Shop\",
    \"ownerName\": \"Shahryar\", \"phone\": \"03001234567\", \"address\": \"Main Road\", \"city\": \"Karachi\"
}")
SHOP_ID=$(echo $REGISTER_RESPONSE | jq -r '.id')

if [ "$SHOP_ID" == "null" ] || [ -z "$SHOP_ID" ]; then
    echo -e "${RED}❌ Shop Registration Failed! (Ho sakta hai email pehle se registered ho. NeonDB se user delete karein ya naya email use karein). Response: $REGISTER_RESPONSE${NC}"; exit 1
else
    echo -e "${GREEN}✅ Shop Registered Successfully! Shop ID: $SHOP_ID${NC}"
fi

# 3. MANUAL SQL STEP
echo -e "\n${YELLOW}[!] ACTION REQUIRED: User abhi 'isActive = false' hai.${NC}"
echo -e "1. NeonDB Console -> SQL Editor open karein."
echo -e "2. Yeh query run karein:"
echo -e "${BLUE}UPDATE users SET is_active = true, email_verified = true WHERE email = '$SHOP_EMAIL';${NC}"
echo -e "3. Query run karne ke baad niche ENTER dabayein..."
read -p ""

# 4. SHOPKEEPER LOGIN
echo -e "\n${YELLOW}[3] Testing Shopkeeper Login...${NC}"
SHOP_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d "{\"email\": \"$SHOP_EMAIL\", \"password\": \"$SHOP_PASS\"}")
SHOP_TOKEN=$(echo $SHOP_LOGIN_RESPONSE | jq -r '.token')

if [ "$SHOP_TOKEN" == "null" ] || [ -z "$SHOP_TOKEN" ]; then
    echo -e "${RED}❌ Shopkeeper Login Failed! Kya aapne SQL query run ki? Response: $SHOP_LOGIN_RESPONSE${NC}"; exit 1
else
    echo -e "${GREEN}✅ Shopkeeper Login Successful!${NC}"
fi

# 5. ADMIN VERIFIES SHOP
echo -e "\n${YELLOW}[4] Admin Verifying Shop...${NC}"
VERIFY_RESPONSE=$(curl -s -X PATCH "$BASE_URL/admin/shops/$SHOP_ID/verify" -H "Authorization: Bearer $ADMIN_TOKEN")
if echo "$VERIFY_RESPONSE" | grep -q "successfully"; then
    echo -e "${GREEN}✅ Shop Verified by Admin!${NC}"
else
    echo -e "${RED}❌ Shop Verification Failed! Response: $VERIFY_RESPONSE${NC}"; exit 1
fi

# 6. STUDENT FILE UPLOAD
echo -e "\n${YELLOW}[5] Testing Student File Upload...${NC}"
echo "This is a dummy PDF file for testing PrintPass." > dummy.pdf
UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/jobs/upload" -F "file=@dummy.pdf")
PIN=$(echo $UPLOAD_RESPONSE | jq -r '.secretKey')

if [ "$PIN" == "null" ] || [ -z "$PIN" ]; then
    echo -e "${RED}❌ File Upload Failed! Response: $UPLOAD_RESPONSE${NC}"; exit 1
else
    echo -e "${GREEN}✅ File Uploaded Successfully! PIN: $PIN${NC}"
fi
rm dummy.pdf

# 7. SHOPKEEPER FETCHES FILE
echo -e "\n${YELLOW}[6] Shopkeeper Fetching File by PIN: $PIN...${NC}"
GET_FILE_RESPONSE=$(curl -s -X GET "$BASE_URL/jobs/$PIN" -H "Authorization: Bearer $SHOP_TOKEN")
FILE_URL=$(echo $GET_FILE_RESPONSE | jq -r '.fileUrl')

if [ "$FILE_URL" == "null" ] || [ -z "$FILE_URL" ]; then
    echo -e "${RED}❌ Fetch File Failed! Response: $GET_FILE_RESPONSE${NC}"; exit 1
else
    echo -e "${GREEN}✅ File Fetched Successfully! Download URL: ${BLUE}$FILE_URL${NC}"
fi

# 8. SHOPKEEPER PRINTS JOB
echo -e "\n${YELLOW}[7] Shopkeeper Marking Job as Printed...${NC}"
PRINT_RESPONSE=$(curl -s -X POST "$BASE_URL/jobs/$PIN/print" -H "Authorization: Bearer $SHOP_TOKEN")
MSG=$(echo $PRINT_RESPONSE | jq -r '.message')

if [ "$MSG" == "null" ] || [ -z "$MSG" ]; then
    echo -e "${RED}❌ Print Action Failed! Response: $PRINT_RESPONSE${NC}"; exit 1
else
    echo -e "${GREEN}✅ Print Action Successful! Message: $MSG${NC}"
fi

echo -e "\n${BLUE}====================================================${NC}"
echo -e "${GREEN}🎉 ALL TESTS COMPLETED SUCCESSFULLY! 🎉${NC}"
echo -e "${BLUE}====================================================${NC}"