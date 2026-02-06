#!/bin/bash

# ==============================================================================
# Padel System - Endpoint Test Suite
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. CONFIGURATION
# ------------------------------------------------------------------------------
echo ">>> Detecting Gateway IP..."
INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}' 2>/dev/null)

if [[ -z "$INGRESS_HOST" ]]; then INGRESS_HOST="127.0.0.1"; fi
if [[ -z "$INGRESS_PORT" ]]; then INGRESS_PORT="80"; fi

GATEWAY_URL="http://$INGRESS_HOST:$INGRESS_PORT"
echo ">>> Target: $GATEWAY_URL"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

check() {
    expected=$1
    actual=$2
    label=$3
    if [[ "$actual" == "$expected" ]]; then
        echo -e "${GREEN}    [PASS] $label ($actual)${NC}"
    else
        echo -e "${RED}    [FAIL] $label (Expected $expected, Got $actual)${NC}"
    fi
}

login() {
    response=$(curl -s -X POST "$GATEWAY_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$1\", \"password\":\"password\"}")
    echo "$response" | tr -d '"' | tr -d '\r'
}

get_id() {
    echo "$1" | grep -o '"id":[0-9]*' | head -n1 | sed 's/"id"://'
}

# ==============================================================================
# 2. ADMIN SETUP (Prepare Data)
# ==============================================================================
echo -e "\n${BLUE}>>> 1. Admin Setup (Creating Test Data)${NC}"
ADMIN_TOKEN=$(login "admin@uns.ac.rs")

echo -n "  - Creating Club... "
CLUB_RESP=$(curl -s -X POST "$GATEWAY_URL/club" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name": "FullTestClub", "location": "Test Loc", "phoneNumber": "555"}')
CLUB_ID=$(get_id "$CLUB_RESP")
echo "Done (ID: $CLUB_ID)"

echo -n "  - Creating Regular User... "
USER_RESP=$(curl -s -X POST "$GATEWAY_URL/user" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"email": "regular_user@test.com", "password": "password", "role": "USER"}')
USER_ID=$(get_id "$USER_RESP")
echo "Done (ID: $USER_ID)"

echo -n "  - Creating Victim User (to be deleted)... "
VICTIM_RESP=$(curl -s -X POST "$GATEWAY_URL/user" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"email": "victim@test.com", "password": "123", "role": "USER"}')
VICTIM_ID=$(get_id "$VICTIM_RESP")
echo "Done (ID: $VICTIM_ID)"


# ==============================================================================
# 3. PUBLIC ENDPOINTS (No Token)
# ==============================================================================
echo -e "\n${BLUE}>>> 2. Public Access Tests (No Login)${NC}"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/club")
check 200 "$CODE" "GET /club (List Clubs)"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/club/$CLUB_ID")
check 200 "$CODE" "GET /club/$CLUB_ID (Club Details)"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/club-composite/$CLUB_ID")
check 200 "$CODE" "GET /club-composite/$CLUB_ID"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/review?clubId=$CLUB_ID")
check 200 "$CODE" "GET /review?clubId=..."

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/reservation" \
  -H "Content-Type: application/json" -d '{}')
if [[ "$CODE" == "401" || "$CODE" == "403" ]]; then
    check "$CODE" "$CODE" "POST /reservation (Should be Blocked)"
else
    check "403" "$CODE" "POST /reservation (Should be Blocked)"
fi


# ==============================================================================
# 4. USER ROLE TESTS
# ==============================================================================
echo -e "\n${BLUE}>>> 3. User Role Tests (regular_user@test.com)${NC}"
USER_TOKEN=$(login "regular_user@test.com")

RES_RESP=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY_URL/reservation" \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"clubId\": $CLUB_ID, \"courtNumber\": 1, \"reservationTime\": \"2026-11-11T10:00:00\", \"userEmail\": \"regular_user@test.com\"}")
RES_CODE=$(echo "$RES_RESP" | tail -n1)
RES_ID=$(get_id "$(echo "$RES_RESP" | head -n -1)")
check 200 "$RES_CODE" "POST /reservation (Create)"

REV_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/review" \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"clubId\": $CLUB_ID, \"rating\": 4, \"comment\": \"User Test\", \"userEmail\": \"regular_user@test.com\"}")
check 200 "$REV_CODE" "POST /review (Create)"

GET_RES_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/reservation?email=regular_user@test.com" \
  -H "Authorization: Bearer $USER_TOKEN")
check 200 "$GET_RES_CODE" "GET /reservation (Own History)"

# Negative Tests (Security Check)
DEL_CLUB_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/club/$CLUB_ID" \
  -H "Authorization: Bearer $USER_TOKEN")
check 403 "$DEL_CLUB_CODE" "DELETE /club (Should Fail)"

DEL_USER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/user/$VICTIM_ID" \
  -H "Authorization: Bearer $USER_TOKEN")
check 403 "$DEL_USER_CODE" "DELETE /user (Should Fail)"


# ==============================================================================
# 5. OWNER ROLE TESTS
# ==============================================================================
echo -e "\n${BLUE}>>> 4. Owner Role Tests (owner@uns.ac.rs)${NC}"
OWNER_TOKEN=$(login "owner@uns.ac.rs")

GET_USER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/user/$VICTIM_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN")
check 200 "$GET_USER_CODE" "GET /user/{id} (View User)"

DEL_USER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/user/$VICTIM_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN")
check 200 "$DEL_USER_CODE" "DELETE /user/{id} (Delete User)"

DEL_CLUB_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/club/$CLUB_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN")
check 403 "$DEL_CLUB_CODE" "DELETE /club (Should Fail)"

NOTIF_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/notification" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d '{}')
check 403 "$NOTIF_CODE" "POST /notification (Should Fail)"


# ==============================================================================
# 6. ADMIN & CLEANUP
# ==============================================================================
echo -e "\n${BLUE}>>> 5. Final Cleanup (Admin)${NC}"

NOTIF_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/notification" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"recipientEmail": "admin@test.com", "subject": "Test", "message": "Hello"}')
if [[ "$NOTIF_CODE" != "404" ]]; then
    check 200 "$NOTIF_CODE" "POST /notification (Admin Allowed)"
fi

if [[ -n "$RES_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/reservation/$RES_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
    echo "  - Deleted Reservation $RES_ID"
fi
if [[ -n "$CLUB_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/club/$CLUB_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
    echo "  - Deleted Club $CLUB_ID"
fi
if [[ -n "$USER_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/user/$USER_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
    echo "  - Deleted User $USER_ID"
fi

echo -e "\n${GREEN}>>> Test Suite Complete.${NC}"