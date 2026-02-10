#!/bin/bash

# ==============================================================================
# Padel System - Endpoint Test Suite
# ==============================================================================


echo ">>> Detecting Gateway IP..."
INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}' 2>/dev/null)

if [[ -z "$INGRESS_HOST" ]]; then INGRESS_HOST="127.0.0.1"; fi
if [[ -z "$INGRESS_PORT" ]]; then INGRESS_PORT="80"; fi

GATEWAY_URL="http://$INGRESS_HOST:$INGRESS_PORT"
echo ">>> Target: $GATEWAY_URL"

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
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


echo -e "\n${BLUE}>>> 1. Admin Setup & Security Checks${NC}"
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

echo -e "${YELLOW}  - [Security] Admin trying to create another ADMIN...${NC}"
ROGUE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/user" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"email\": \"rogue_admin_$RANDOM@test.com\", \"password\": \"password\", \"role\": \"ADMIN\"}")
check 403 "$ROGUE_CODE" "POST /user (Admin creating Admin -> Forbidden)"


echo -e "${YELLOW}  - [Security] Admin trying to delete OWNER (ID 3)...${NC}"
DEL_OWNER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/user/3" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
check 403 "$DEL_OWNER_CODE" "DELETE /user/3 (Admin deleting Owner -> Forbidden)"


#
echo -e "\n${BLUE}>>> 2. Public Access & Registration${NC}"

PUBLIC_EMAIL="public_$RANDOM@test.com"
echo "  - Registering new public user ($PUBLIC_EMAIL)..."
REG_RESP=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$PUBLIC_EMAIL\", \"password\": \"password\"}")
REG_CODE=$(echo "$REG_RESP" | tail -n1)
REG_BODY=$(echo "$REG_RESP" | head -n -1)
PUBLIC_ID=$(get_id "$REG_BODY")

check 200 "$REG_CODE" "POST /auth/register (Public Registration)"
if [[ -z "$PUBLIC_ID" ]]; then echo -e "${RED}    [FAIL] No ID returned for public user${NC}"; fi

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/club")
check 200 "$CODE" "GET /club (List Clubs)"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/club/$CLUB_ID")
check 200 "$CODE" "GET /club/$CLUB_ID (Club Details)"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/reservation" \
  -H "Content-Type: application/json" -d '{}')
if [[ "$CODE" == "401" || "$CODE" == "403" ]]; then
    check "$CODE" "$CODE" "POST /reservation (Should be Blocked)"
else
    check "403" "$CODE" "POST /reservation (Should be Blocked)"
fi



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

echo -e "${YELLOW}  - [Profile] Updating own profile...${NC}"
UPDATE_SELF=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$GATEWAY_URL/user/$USER_ID" \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"password\": \"new_password\"}")
check 200 "$UPDATE_SELF" "PUT /user/$USER_ID (Update Self)"

echo -e "${YELLOW}  - [Security] Updating another user's profile...${NC}"
UPDATE_OTHER=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$GATEWAY_URL/user/1" \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"password\": \"hacked\"}")
check 403 "$UPDATE_OTHER" "PUT /user/1 (Update Other -> Forbidden)"

DEL_CLUB_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/club/$CLUB_ID" \
  -H "Authorization: Bearer $USER_TOKEN")
check 403 "$DEL_CLUB_CODE" "DELETE /club (Should Fail)"



echo -e "\n${BLUE}>>> 4. Owner Role Tests (owner@uns.ac.rs)${NC}"
OWNER_TOKEN=$(login "owner@uns.ac.rs")

echo -e "${YELLOW}  - [Hierarchy] Owner creating new Admin...${NC}"
NEW_ADMIN_EMAIL="legit_admin_$RANDOM@test.com"
NEW_ADMIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY_URL/user" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"email\": \"$NEW_ADMIN_EMAIL\", \"password\": \"password\", \"role\": \"ADMIN\"}")
NEW_ADMIN_CODE=$(echo "$NEW_ADMIN_RESP" | tail -n1)
NEW_ADMIN_ID=$(get_id "$(echo "$NEW_ADMIN_RESP" | head -n -1)")
check 200 "$NEW_ADMIN_CODE" "POST /user (Owner creates Admin)"

GET_USER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$GATEWAY_URL/user/$VICTIM_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN")
check 200 "$GET_USER_CODE" "GET /user/{id} (View User)"

DEL_USER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/user/$VICTIM_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN")
check 200 "$DEL_USER_CODE" "DELETE /user/{id} (Delete User)"

DEL_CLUB_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$GATEWAY_URL/club/$CLUB_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN")
check 403 "$DEL_CLUB_CODE" "DELETE /club (Should Fail)"



echo -e "\n${BLUE}>>> 5. Data Privacy & Reservations${NC}"

echo -n "  - Creating  Club... "
CLUB_P2_RESP=$(curl -s -X POST "$GATEWAY_URL/club" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name": "Phase2Club", "location": "Privacy City", "phoneNumber": "999"}')
CLUB_P2_ID=$(get_id "$CLUB_P2_RESP")
echo "Done (ID: $CLUB_P2_ID)"

echo -n "  - Creating User A (Victim)... "
USER_A_RESP=$(curl -s -X POST "$GATEWAY_URL/user" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"email": "usera@test.com", "password": "password", "role": "USER"}')
USER_A_ID=$(get_id "$USER_A_RESP")
echo "Done (ID: $USER_A_ID)"

echo -n "  - Creating User B (Attacker)... "
USER_B_RESP=$(curl -s -X POST "$GATEWAY_URL/user" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"email": "userb@test.com", "password": "password", "role": "USER"}')
USER_B_ID=$(get_id "$USER_B_RESP")
echo "Done (ID: $USER_B_ID)"

TOKEN_A=$(login "usera@test.com")
TOKEN_B=$(login "userb@test.com")

echo "  - User A creating reservation..."
RES_A_RESP=$(curl -s -X POST "$GATEWAY_URL/reservation" \
  -H "Authorization: Bearer $TOKEN_A" -H "Content-Type: application/json" \
  -d "{\"clubId\": $CLUB_P2_ID, \"courtNumber\": 1, \"reservationTime\": \"2026-12-01T10:00:00\", \"userEmail\": \"usera@test.com\"}")
RES_A_ID=$(get_id "$(echo "$RES_A_RESP" | head -n -1)")

echo "  - User B creating reservation..."
RES_B_RESP=$(curl -s -X POST "$GATEWAY_URL/reservation" \
  -H "Authorization: Bearer $TOKEN_B" -H "Content-Type: application/json" \
  -d "{\"clubId\": $CLUB_P2_ID, \"courtNumber\": 2, \"reservationTime\": \"2026-12-01T11:00:00\", \"userEmail\": \"userb@test.com\"}")
RES_B_ID=$(get_id "$(echo "$RES_B_RESP" | head -n -1)")

echo -e "${YELLOW}  - [Privacy] User B requests ALL reservations...${NC}"
ALL_RES=$(curl -s -X GET "$GATEWAY_URL/reservation" -H "Authorization: Bearer $TOKEN_B")

if echo "$ALL_RES" | grep -q "\"courtNumber\":1"; then
    echo -e "${RED}    [FAIL] User B can see User A's reservation!${NC}"
else
    echo -e "${GREEN}    [PASS] User B cannot see User A's reservation (Filtered).${NC}"
fi

echo -e "${YELLOW}  - [Privacy] User B requests User A's data explicitly...${NC}"
ATTACK_RES=$(curl -s -X GET "$GATEWAY_URL/reservation?email=usera@test.com" -H "Authorization: Bearer $TOKEN_B")

if echo "$ATTACK_RES" | grep -q "\"courtNumber\":1"; then
    echo -e "${RED}    [FAIL] User B successfully accessed User A's data!${NC}"
else
    echo -e "${GREEN}    [PASS] User B blocked from accessing User A's data.${NC}"
fi

echo -e "${YELLOW}  - [Admin] Admin requests ALL reservations...${NC}"
ADMIN_VIEW=$(curl -s -X GET "$GATEWAY_URL/reservation" -H "Authorization: Bearer $ADMIN_TOKEN")
if echo "$ADMIN_VIEW" | grep -q "\"courtNumber\":1" && echo "$ADMIN_VIEW" | grep -q "\"courtNumber\":2"; then
    echo -e "${GREEN}    [PASS] Admin can see all reservations.${NC}"
else
    echo -e "${RED}    [FAIL] Admin cannot see all data.${NC}"
fi


echo -e "\n${BLUE}>>> 6. Cascading Deletion Logic${NC}"

echo -n "  - Creating Disposable Club... "
DISP_CLUB_RESP=$(curl -s -X POST "$GATEWAY_URL/club" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name": "DeleteMeClub", "location": "Void", "phoneNumber": "000"}')
DISP_CLUB_ID=$(get_id "$DISP_CLUB_RESP")
echo "Done (ID: $DISP_CLUB_ID)"

echo -n "  - Adding Review to Disposable Club... "
REV_RESP=$(curl -s -X POST "$GATEWAY_URL/review" \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"clubId\": $DISP_CLUB_ID, \"rating\": 5, \"comment\": \"Will vanish\", \"userEmail\": \"regular_user@test.com\"}")
echo "Done"

echo -n "  - Verifying Review exists... "
REVIEWS=$(curl -s -X GET "$GATEWAY_URL/review?clubId=$DISP_CLUB_ID")
if echo "$REVIEWS" | grep -q "\"comment\":\"Will vanish\""; then
    echo -e "${GREEN}[OK]${NC}"
else
    echo -e "${RED}[FAIL] Review not created! Response: $REV_RESP${NC}"
fi

echo "  - Deleting Club..."
curl -s -o /dev/null -X DELETE "$GATEWAY_URL/club/$DISP_CLUB_ID" -H "Authorization: Bearer $ADMIN_TOKEN"

echo "  - Waiting 3 seconds for async deletion..."
sleep 3

# 6. Verify Review is GONE
echo -e "${YELLOW}  - [Logic] Checking if reviews were deleted...${NC}"
REVIEWS_AFTER=$(curl -s -X GET "$GATEWAY_URL/review?clubId=$DISP_CLUB_ID")
if echo "$REVIEWS_AFTER" | grep -q "\"comment\":\"Will vanish\""; then
    echo -e "${RED}    [FAIL] Review still exists! Cascading delete failed.${NC}"
else
    echo -e "${GREEN}    [PASS] Review successfully deleted by event system.${NC}"
fi


echo -e "\n${BLUE}>>> 7. Final Cleanup${NC}"

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
if [[ -n "$PUBLIC_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/user/$PUBLIC_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
    echo "  - Deleted Public User $PUBLIC_ID"
fi
if [[ -n "$NEW_ADMIN_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/user/$NEW_ADMIN_ID" -H "Authorization: Bearer $OWNER_TOKEN"
    echo "  - Owner Deleted New Admin $NEW_ADMIN_ID"
fi

if [[ -n "$RES_A_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/reservation/$RES_A_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
fi
if [[ -n "$RES_B_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/reservation/$RES_B_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
fi
if [[ -n "$CLUB_P2_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/club/$CLUB_P2_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
    echo "  - Deleted Club $CLUB_P2_ID"
fi
if [[ -n "$USER_A_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/user/$USER_A_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
fi
if [[ -n "$USER_B_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/user/$USER_B_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
    echo "  - Deleted Users"
fi

if [[ -n "$DISP_CLUB_ID" ]]; then
    curl -s -o /dev/null -X DELETE "$GATEWAY_URL/club/$DISP_CLUB_ID" -H "Authorization: Bearer $ADMIN_TOKEN"
fi

echo -e "\n${GREEN}>>> Test Suite Complete.${NC}"