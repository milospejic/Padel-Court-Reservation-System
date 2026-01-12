#!/bin/bash

# ==================================================================================
# Padel Court Reservation System - Shutdown & Cleanup Script
# ==================================================================================

NAMESPACE="padel-dev"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' 

echo -e "${BLUE}>>> Initiating Shutdown Sequence...${NC}"

# 1. Kill Minikube Tunnel
if pgrep -f "minikube tunnel" > /dev/null; then
    echo -e "${BLUE}Stopping Minikube tunnel...${NC}"
    pkill -f "minikube tunnel"
else
    echo -e "${BLUE}No background tunnel found (if you have one open in a separate terminal, close it now).${NC}"
fi

# 2. Delete Application Resources
echo -e "${BLUE}Deleting Kubernetes resources (Deployments, Services, ConfigMaps)...${NC}"
kubectl delete -k kubernetes/overlays/dev --ignore-not-found=true

# 3. Delete Namespace
echo -e "${BLUE}Deleting Namespace: ${NAMESPACE}...${NC}"
kubectl delete namespace $NAMESPACE --ignore-not-found=true

# 4. Stop Minikube
echo -e "${BLUE}Stopping Minikube Cluster...${NC}"
minikube stop

echo -e "${GREEN}>>> Application stopped successfully.${NC}"

# 5. Optional: Full Delete
echo -e "\n${RED}!!! DESTRUCTIVE ACTION !!!${NC}"
read -p "Do you want to completely DELETE the Minikube cluster and all data? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}Deleting Minikube cluster...${NC}"
    minikube delete
    echo -e "${GREEN}Cluster deleted.${NC}"
else
    echo -e "${BLUE}Cluster stopped but kept intact. Data in PersistentVolumes is preserved.${NC}"
fi

echo -e "${GREEN}Done.${NC}"