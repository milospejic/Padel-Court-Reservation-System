#!/bin/bash

# ==================================================================================
# Padel Court Reservation System - Minikube Startup Script
# ==================================================================================

: "${DOCKER_USERNAME:?Environment variable DOCKER_USERNAME is not set}"


NAMESPACE="padel-dev"
MEMORY="7000" 
CPUS="4"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}>>> Starting Deployment Sequence...${NC}"

if minikube status | grep -q "Running"; then
    echo -e "${GREEN}Minikube is already running.${NC}"
else
    echo -e "${BLUE}Starting Minikube (CPUs: ${CPUS}, Memory: ${MEMORY}MB)...${NC}"
    minikube start --cpus $CPUS --memory $MEMORY --driver=docker
fi

echo -e "${BLUE}Pointing shell to Minikube's Docker daemon...${NC}"
eval $(minikube docker-env)

echo -e "${BLUE}Checking Istio installation...${NC}"
if ! istioctl version > /dev/null 2>&1; then
    echo -e "${RED}istioctl is not installed. Please install Istio first.${NC}"
    exit 1
fi

if ! kubectl get namespace istio-system > /dev/null 2>&1; then
    echo -e "${BLUE}Installing Istio (demo profile)...${NC}"
    istioctl install --set profile=demo -y
else
    echo -e "${GREEN}Istio is already installed.${NC}"
fi

echo -e "${BLUE}Installing Istio Addons (Prometheus, Grafana, Kiali, Zipkin)...${NC}"
ISTIO_ADDON_URL="https://raw.githubusercontent.com/istio/istio/release-1.20/samples/addons"
kubectl apply -f "${ISTIO_ADDON_URL}/prometheus.yaml"
kubectl apply -f "${ISTIO_ADDON_URL}/grafana.yaml"
kubectl apply -f "${ISTIO_ADDON_URL}/kiali.yaml"
kubectl apply -f "${ISTIO_ADDON_URL}/extras/zipkin.yaml"

echo -e "${BLUE}Applying Monitoring Stack (Alertmanager & Rules) Declaratively...${NC}"
kubectl apply -k kubernetes/platform/monitoring

kubectl -n istio-system rollout restart deployment prometheus


echo -e "${BLUE}Setting up namespace: ${NAMESPACE}...${NC}"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
kubectl label namespace $NAMESPACE istio-injection=enabled --overwrite
kubectl create secret generic jwt-secret --from-literal=JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437 


echo -e "${BLUE}Patching Deployments to use local images (IfNotPresent)...${NC}"
for file in kubernetes/base/components/deployments/*.yml; do
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' 's/imagePullPolicy: Always/imagePullPolicy: IfNotPresent/g' "$file"
    else
        sed -i 's/imagePullPolicy: Always/imagePullPolicy: IfNotPresent/g' "$file"
    fi
done
echo -e "${GREEN}Deployments patched.${NC}"

echo -e "${BLUE}Building Shared Libraries...${NC}"
cd util
chmod +x mvnw
./mvnw clean install -DskipTests
cd ..

echo -e "${BLUE}Building Shared API Library...${NC}"
cd api
chmod +x mvnw
./mvnw clean install -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}API build failed! Stopping script.${NC}"
    exit 1
fi
cd ..

services=(
    "user-service"
    "club-service"
    "reservation-service"
    "review-service"
    "notification-service"
    "club-composite-service"
)

echo -e "${BLUE}Building Services and Docker Images...${NC}"
for service in "${services[@]}"; do
    echo -e "${BLUE}Processing ${service}...${NC}"
    
    cd $service
    chmod +x mvnw
    ./mvnw clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo -e "${RED}Maven build failed for ${service}! Stopping script.${NC}"
        exit 1
    fi
    

    docker build -t "${DOCKER_USERNAME}/${service}:latest" .
    if [ $? -ne 0 ]; then
        echo -e "${RED}Docker build failed for ${service}! Stopping script.${NC}"
        exit 1
    fi
    
    cd ..
done

#echo -e "${BLUE}Building Custom Fluentd Image...${NC}"
#eval $(minikube docker-env)
#docker build -t "hands-on/fluentd:v1" kubernetes/base/efk/

echo -e "${GREEN}All images built successfully.${NC}"

echo -e "${BLUE}Creating Kubernetes Secrets...${NC}"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
SECRET_VALUE="${JWT_SECRET:-5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437}"
kubectl create secret generic jwt-secret \
    --namespace $NAMESPACE \
    --from-literal=JWT_SECRET="$SECRET_VALUE" \
    --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}Secret 'jwt-secret' created in namespace '$NAMESPACE'.${NC}"




#echo -e "${BLUE}Creating Logging Namespace...${NC}"
#kubectl create namespace logging --dry-run=client -o yaml | kubectl apply -f -

echo -e "${BLUE}Applying Istio System Policies...${NC}"

sed "s|\${JWT_SECRET}|$SECRET_VALUE|g" kubernetes/base/istio/istio-authentication.yml | kubectl apply -f -

kubectl apply -f kubernetes/base/istio/istio-authorization.yml
kubectl apply -f kubernetes/base/istio/istio-envoy-filter.yml

echo -e "${BLUE}Applying Kubernetes Manifests (Apps)...${NC}"
kubectl apply -k kubernetes/overlays/dev

#echo -e "${BLUE}Deploying EFK Components...${NC}"
#kubectl apply -k kubernetes/platform/efk

#echo -e "${BLUE}Waiting for Elasticsearch to be ready...${NC}"
#kubectl wait --namespace logging \
 # --for=condition=Ready pod \
  #--selector=app=elasticsearch \
  #--timeout=300s



#echo -e "${BLUE}Waiting for Fluentd DaemonSet...${NC}"
#kubectl wait --namespace kube-system \
 # --for=condition=Ready pod \
  #-l app=fluentd \
  #--timeout=120s

echo -e "\n${GREEN}======================================================${NC}"
echo -e "${GREEN}   DEPLOYMENT SUCCESSFUL!   ${NC}"
echo -e "${GREEN}======================================================${NC}"
echo -e "IMPORTANT: Run the tunnel in a separate terminal now:${NC}"
echo -e "\n   ${RED}minikube tunnel${NC}\n"


