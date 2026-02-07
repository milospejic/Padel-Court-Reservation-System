#!/bin/bash

# ==============================================================================
# Padel System - Resilience, Circuit Breaker & Fallback Test Suite
# ==============================================================================

NAMESPACE="padel-dev"
FORTIO_DEPLOY_URL="https://raw.githubusercontent.com/istio/istio/release-1.20/samples/httpbin/sample-client/fortio-deploy.yaml"

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'


echo -e "${YELLOW}IMPORTANT: This script assumes you have already executed 'run-on-minikube.sh' and the cluster is running.${NC}"
read -p "Has 'run-on-minikube.sh' been executed successfully? (y/N): " confirm

if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo -e "${RED}Aborting. Please run the deployment script first.${NC}"
    exit 1
fi

echo -e "${BLUE}>>> Starting Resilience Tests in namespace: ${NAMESPACE}${NC}"


echo -e "\n${BLUE}>>> 1. Setting up Test Client (Fortio)...${NC}"

if ! kubectl get deployment fortio-deploy -n $NAMESPACE > /dev/null 2>&1; then
    kubectl apply -f $FORTIO_DEPLOY_URL -n $NAMESPACE
    echo "Waiting for Fortio pod to be ready..."
    kubectl wait --for=condition=ready pod -l app=fortio -n $NAMESPACE --timeout=120s
else
    echo -e "${GREEN}Fortio is already deployed.${NC}"
fi

FORTIO_POD=$(kubectl get pods -n $NAMESPACE -l app=fortio -o 'jsonpath={.items[0].metadata.name}')
echo -e "Test Client Pod: ${GREEN}$FORTIO_POD${NC}"

echo -e "${YELLOW}Warming up review-service (ignoring errors)...${NC}"
kubectl exec "$FORTIO_POD" -n $NAMESPACE -c fortio -- fortio load -c 1 -n 5 -qps 0 -timeout 5s http://review-service:80/review?clubId=1 > /dev/null 2>&1


echo -e "\n${BLUE}>>> 2. Testing Connection Pooling (Overflow Protection)${NC}"
echo "Applying STRICT Circuit Breaker (Max 1 Connection) from file..."

kubectl apply -f kubernetes/base/resilience-test/strict-review-cb.yml

echo "Waiting for rule propagation (5s)..."
sleep 5

echo -e "${YELLOW}Running Load Test: 20 calls with 2 concurrent connections...${NC}"

kubectl exec "$FORTIO_POD" -n $NAMESPACE -c fortio -- fortio load -c 2 -qps 0 -n 20 -timeout 10s -json - http://review-service:80/review?clubId=1 > fortio_results.json

count_200=$(grep -o '"200": *[0-9]*' fortio_results.json | awk -F ':' '{print $2}' | tr -d ' ')
count_503=$(grep -o '"503": *[0-9]*' fortio_results.json | awk -F ':' '{print $2}' | tr -d ' ')

count_200=${count_200:-0}
count_503=${count_503:-0}

echo -e "Results:"
echo -e "  Success (200): ${GREEN}${count_200}${NC}"
echo -e "  Blocked (503): ${RED}${count_503}${NC}"

if [[ "$count_503" -gt 0 ]]; then
    echo -e "${GREEN}[PASS] Circuit Breaker triggered successfully! Excess requests were blocked.${NC}"
else
    echo -e "${RED}[FAIL] No requests were blocked. Check Istio configuration or logs.${NC}"
fi
rm fortio_results.json

kubectl apply -f kubernetes/base/istio/istio-circuit-breaker.yml > /dev/null 2>&1

echo -e "\n${BLUE}>>> 3. Testing Fault Injection & Application Fallback${NC}"
echo "Injecting 500 Errors into Review Service..."

kubectl apply -f kubernetes/base/resilience-test/review-fault-injection.yml
echo "Waiting for rule propagation (5s)..."
sleep 5

echo -e "${YELLOW}Step 3.1: Verifying Review Service returns 500 (Injection active)...${NC}"
OUTPUT_DIRECT=$(kubectl exec "$FORTIO_POD" -n $NAMESPACE -c fortio -- fortio curl -quiet http://review-service:80/review?clubId=1 2>&1)

if [[ "$OUTPUT_DIRECT" == *"500 Internal Server Error"* ]]; then
    echo -e "${GREEN}[PASS] Review Service returned 500 as expected.${NC}"
else
    echo -e "${RED}[FAIL] Expected 500, got something else.${NC}"
    echo "$OUTPUT_DIRECT" | head -n 3
fi

echo -e "${YELLOW}Step 3.2: Verifying Club Composite Service handles the failure (Fallback)...${NC}"
OUTPUT_COMPOSITE=$(kubectl exec "$FORTIO_POD" -n $NAMESPACE -c fortio -- fortio curl -quiet http://club-composite-service:80/club-composite/1 2>&1)

if [[ "$OUTPUT_COMPOSITE" == *"200 OK"* ]]; then
    echo -e "${GREEN}[PASS] Composite Service returned 200 OK (Fallback Active).${NC}"
else
    echo -e "${RED}[FAIL] Composite Service crashed or returned error. Fallback failed.${NC}"
    echo "$OUTPUT_COMPOSITE" | head -n 3
fi


echo -e "\n${BLUE}>>> 4. Cleaning Up Test Rules${NC}"

kubectl delete -f kubernetes/base/resilience-test/review-fault-injection.yml --ignore-not-found=true
kubectl delete -f kubernetes/base/resilience-test/strict-review-cb.yml --ignore-not-found=true

echo "Restoring original Circuit Breaker policies..."
kubectl apply -f kubernetes/base/istio/istio-circuit-breaker.yml

echo -e "\n${GREEN}>>> Resilience Tests Completed.${NC}"