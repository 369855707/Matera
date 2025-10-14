#!/bin/bash

# ========================================
# Maternity Backend Deployment Script
# ========================================

set -e  # Exit on error

echo "========================================="
echo "Maternity Backend Deployment"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found!${NC}"
    echo "Creating .env from template..."
    cp .env.production .env
    echo -e "${RED}Please edit .env file with your actual values before continuing!${NC}"
    exit 1
fi

echo -e "${GREEN}Step 1: Stopping existing containers...${NC}"
docker-compose down || true

echo -e "${GREEN}Step 2: Pulling latest code...${NC}"
git pull origin main || echo "Git pull skipped (not in git repo or no changes)"

echo -e "${GREEN}Step 3: Building Docker image...${NC}"
docker-compose build --no-cache

echo -e "${GREEN}Step 4: Starting containers...${NC}"
docker-compose up -d

echo -e "${GREEN}Step 5: Waiting for application to start...${NC}"
sleep 10

echo -e "${GREEN}Step 6: Checking container status...${NC}"
docker-compose ps

echo -e "${GREEN}Step 7: Checking application health...${NC}"
for i in {1..12}; do
    if curl -f http://localhost:8080/actuator/health -s > /dev/null; then
        echo -e "${GREEN}✓ Application is healthy!${NC}"
        break
    fi
    if [ $i -eq 12 ]; then
        echo -e "${RED}✗ Health check failed after 60 seconds${NC}"
        echo "Showing logs:"
        docker-compose logs --tail=50 backend
        exit 1
    fi
    echo "Waiting for application to be ready... ($i/12)"
    sleep 5
done

echo ""
echo "========================================="
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo "========================================="
echo ""
echo "Application URLs:"
echo "  - API: http://129.211.167.49:8080"
echo "  - Health: http://129.211.167.49:8080/actuator/health"
echo "  - Swagger UI: http://129.211.167.49:8080/swagger-ui.html"
echo ""
echo "Useful commands:"
echo "  - View logs: docker-compose logs -f backend"
echo "  - Restart: docker-compose restart backend"
echo "  - Stop: docker-compose down"
echo "  - View status: docker-compose ps"
echo ""
