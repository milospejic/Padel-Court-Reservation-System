#!/bin/bash

# ==============================================================================
# Padel System - Alarm Trigger Test Suite
# Triggers: CircuitBreakerTripped & ServiceDown
# ==============================================================================

NAMESPACE="padel-dev"
FORTIO_DEPLOY_URL="https://raw.githubusercontent.com/istio/istio/release-1.20/samples/httpbin/sample-client/fortio-deploy.yaml"

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}>>> Starting Alarm System Tests...${NC}"

echo -e "\n${BLUE}>>> 1. Checking Environment...${NC}"
if ! minikube status | grep -q "Running"; then
    echo -e "${RED}[ERROR] Minikube is not running.${NC}"
    echo "Please execute 'run-on-minikube.sh' first."
    exit 1
fi

if ! kubectl get namespace $NAMESPACE > /dev/null 2>&1; then
    echo -e "${RED}[ERROR] Namespace '$NAMESPACE' does not exist.${NC}"
    echo "Please execute 'run-on-minikube.sh' first."
    exit 1
fi
echo -e "${GREEN}[OK] Environment looks ready.${NC}"


echo -e "\n${BLUE}>>> 2. Verifying Test Client (Fortio)...${NC}"
if ! kubectl get deployment fortio-deploy -n $NAMESPACE > /dev/null 2>&1; then
    echo "Deploying Fortio..."
    kubectl apply -f $FORTIO_DEPLOY_URL -n $NAMESPACE
    echo "Waiting for Fortio pod..."
    kubectl wait --for=condition=ready pod -l app=fortio -n $NAMESPACE --timeout=120s
else
    echo -e "${GREEN}Fortio is already deployed.${NC}"
fi

FORTIO_POD=$(kubectl get pods -n $NAMESPACE -l app=fortio -o 'jsonpath={.items[0].metadata.name}')


echo -e "\n${BLUE}>>> 3. Triggering 'CircuitBreakerTripped' Alert${NC}"
echo "This test applies a strict rule and floods the service to cause 503 errors."

kubectl apply -f kubernetes/base/resilience-test/strict-review-cb.yml
echo "Waiting 5s for rule propagation..."
sleep 5

echo -e "${YELLOW}Running 45-second load test (High Load)...${NC}"
kubectl exec "$FORTIO_POD" -n $NAMESPACE -c fortio -- fortio load -c 2 -qps 0 -t 45s http://review-service:80/review?clubId=1 > /dev/null 2>&1

echo -e "${GREEN}Load test complete. Check MailHog for 'CircuitBreakerTripped' email.${NC}"

kubectl apply -f kubernetes/base/istio/istio-circuit-breaker.yml > /dev/null 2>&1


echo -e "\n${BLUE}>>> 4. Triggering 'ServiceDown' Alert${NC}"
echo "This test scales 'notification-service' to 0 replicas."
echo "The alert rule requires the service to be down for > 1 minute."

echo -e "${YELLOW}Scaling down notification-service...${NC}"
kubectl scale deployment notification-service --replicas=0 -n $NAMESPACE

echo -e "${YELLOW}Waiting 75 seconds for alert condition (Rule: for 1m)...${NC}"
for i in {75..1}; do
    echo -ne "Time remaining: $i seconds\r"
    sleep 1
done
echo -e "\n${GREEN}Time up. Check MailHog for 'ServiceDown' email.${NC}"

echo -e "${YELLOW}Restoring notification-service...${NC}"
kubectl scale deployment notification-service --replicas=1 -n $NAMESPACE
kubectl wait --for=condition=ready pod -l app=notification-service -n $NAMESPACE --timeout=60s > /dev/null 2>&1

echo -e "\n${GREEN}>>> Alarm Tests Completed.${NC}"
echo -e "Access MailHog at: http://localhost:8025 (requires port-forward)"