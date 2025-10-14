#!/bin/bash

# ========================================
# Maternity Backend - Complete Server Setup
# ========================================
# Run this script on your server (129.211.167.49)
# Usage: bash server-setup.sh

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Maternity Backend - Server Setup${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Step 0: Check and install Git
echo -e "${GREEN}Step 0: Checking Git installation...${NC}"
if ! command -v git &> /dev/null; then
    echo -e "${YELLOW}Git not found. Installing Git...${NC}"
    yum install -y git
    echo -e "${GREEN}✓ Git installed successfully${NC}"
else
    echo -e "${GREEN}✓ Git already installed: $(git --version)${NC}"
fi

# Step 1: Check and install Docker
echo -e "${GREEN}Step 1: Checking Docker installation...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Docker not found. Installing Docker for TencentOS/CentOS...${NC}"

    # Remove old versions
    yum remove -y docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine 2>/dev/null || true

    # Install required packages
    yum install -y yum-utils device-mapper-persistent-data lvm2

    # Add Aliyun Docker repository
    yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

    # Install Docker
    yum install -y docker-ce docker-ce-cli containerd.io

    # Configure Docker registry mirrors for China
    echo -e "${YELLOW}Configuring Docker registry mirrors for China...${NC}"
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://mirror.ccs.tencentyun.com",
    "https://registry.docker-cn.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
DOCKER_EOF

    # Start and enable Docker
    systemctl start docker
    systemctl enable docker

    echo -e "${GREEN}✓ Docker installed successfully with registry mirrors${NC}"
else
    echo -e "${GREEN}✓ Docker already installed: $(docker --version)${NC}"

    # Check if registry mirrors are configured
    if [ ! -f /etc/docker/daemon.json ] || ! grep -q "registry-mirrors" /etc/docker/daemon.json 2>/dev/null; then
        echo -e "${YELLOW}Configuring Docker registry mirrors for China...${NC}"
        mkdir -p /etc/docker
        cat > /etc/docker/daemon.json << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://mirror.ccs.tencentyun.com",
    "https://registry.docker-cn.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
DOCKER_EOF
        systemctl restart docker
        echo -e "${GREEN}✓ Docker registry mirrors configured${NC}"
    else
        echo -e "${GREEN}✓ Docker registry mirrors already configured${NC}"
    fi
fi

# Step 2: Check and install Docker Compose
echo ""
echo -e "${GREEN}Step 2: Checking Docker Compose installation...${NC}"
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${YELLOW}Docker Compose not found. Installing...${NC}"

    # Try installing docker-compose-plugin via yum (recommended method)
    echo -e "${YELLOW}Trying to install docker-compose-plugin...${NC}"
    yum install -y docker-compose-plugin 2>/dev/null || true

    # Check if docker compose plugin works
    if docker compose version &> /dev/null; then
        echo -e "${GREEN}✓ Docker Compose plugin installed successfully${NC}"
        # Create docker-compose alias
        echo 'alias docker-compose="docker compose"' >> ~/.bashrc
        ln -sf /usr/libexec/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose 2>/dev/null || true
    else
        # Fallback: try downloading binary
        echo -e "${YELLOW}Trying to download Docker Compose binary...${NC}"
        COMPOSE_VERSION="v2.24.0"

        # Try DaoCloud mirror (usually more reliable in China)
        curl -L "https://get.daocloud.io/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose 2>/dev/null || \
        curl -kL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose 2>/dev/null || true

        if [ -f /usr/local/bin/docker-compose ] && [ -s /usr/local/bin/docker-compose ]; then
            chmod +x /usr/local/bin/docker-compose
            echo -e "${GREEN}✓ Docker Compose binary installed successfully${NC}"
        else
            echo -e "${RED}Failed to install Docker Compose. Will try to use 'docker compose' command instead.${NC}"
        fi
    fi
else
    if command -v docker-compose &> /dev/null; then
        echo -e "${GREEN}✓ Docker Compose already installed: $(docker-compose --version)${NC}"
    else
        echo -e "${GREEN}✓ Docker Compose plugin already installed: $(docker compose version)${NC}"
        # Create alias for convenience
        ln -sf /usr/libexec/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose 2>/dev/null || true
    fi
fi

# Step 3: Clone or update repository
echo ""
echo -e "${GREEN}Step 3: Setting up repository...${NC}"
cd /opt

if [ -d "maternity-backend" ]; then
    echo -e "${YELLOW}Repository exists. Updating...${NC}"
    cd maternity-backend
    git fetch --all
    git checkout feat/deployment-setup
    git pull origin feat/deployment-setup
    echo -e "${GREEN}✓ Repository updated${NC}"
else
    echo -e "${YELLOW}Cloning repository...${NC}"
    git clone https://github.com/369855707/Matera.git maternity-backend
    cd maternity-backend
    git checkout feat/deployment-setup
    echo -e "${GREEN}✓ Repository cloned${NC}"
fi

# Step 4: Setup environment configuration
echo ""
echo -e "${GREEN}Step 4: Configuring environment...${NC}"

if [ -f .env ]; then
    echo -e "${YELLOW}Warning: .env file already exists${NC}"
    echo -e "${YELLOW}Do you want to recreate it? (y/N)${NC}"
    read -r RECREATE
    if [[ ! "$RECREATE" =~ ^[Yy]$ ]]; then
        echo "Keeping existing .env file"
        SKIP_ENV=true
    fi
fi

if [ "$SKIP_ENV" != "true" ]; then
    echo -e "${YELLOW}Generating secure credentials...${NC}"

    # Generate secure random passwords
    JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    DB_PASSWORD="MaternityDB$(openssl rand -hex 8)"
    ADMIN_PASSWORD="Admin$(openssl rand -hex 6)!"

    # Create .env file
    cat > .env << EOF
# ========================================
# Maternity Backend Production Configuration
# Generated: $(date)
# ========================================

# Database Password
DB_PASSWORD=$DB_PASSWORD

# JWT Configuration
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=86400000

# Admin Account
ADMIN_USERNAME=admin
ADMIN_PASSWORD=$ADMIN_PASSWORD

# WeChat Mini Program (Optional)
WECHAT_APPID=
WECHAT_SECRET=

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://129.211.167.49,http://129.211.167.49:3000
EOF

    echo -e "${GREEN}✓ Environment configuration created${NC}"
    echo ""
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${BLUE}IMPORTANT: Save These Credentials!${NC}"
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${YELLOW}Admin Username: admin${NC}"
    echo -e "${YELLOW}Admin Password: $ADMIN_PASSWORD${NC}"
    echo -e "${BLUE}=========================================${NC}"
    echo ""
    echo -e "${RED}Credentials are also saved in: /opt/maternity-backend/.env${NC}"
    echo ""
    echo -e "Press ${YELLOW}Enter${NC} to continue with deployment..."
    read
fi

# Step 5: Open firewall port
echo ""
echo -e "${GREEN}Step 5: Configuring firewall...${NC}"
if command -v ufw &> /dev/null; then
    if ufw status | grep -q "Status: active"; then
        echo -e "${YELLOW}Opening port 8080...${NC}"
        ufw allow 8080/tcp
        echo -e "${GREEN}✓ Firewall configured${NC}"
    else
        echo -e "${YELLOW}UFW is not active, skipping...${NC}"
    fi
else
    echo -e "${YELLOW}UFW not found, skipping firewall configuration${NC}"
fi

# Step 6: Deploy application
echo ""
echo -e "${GREEN}Step 6: Deploying application...${NC}"
chmod +x deploy.sh
./deploy.sh

# Final summary
echo ""
echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}✓ Setup Complete!${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "${GREEN}Your application is now running:${NC}"
echo -e "  • API URL: ${YELLOW}http://129.211.167.49:8080${NC}"
echo -e "  • Health Check: ${YELLOW}http://129.211.167.49:8080/actuator/health${NC}"
echo -e "  • API Docs: ${YELLOW}http://129.211.167.49:8080/swagger-ui.html${NC}"
echo ""
echo -e "${GREEN}Admin Credentials:${NC}"
echo -e "  • Location: ${YELLOW}/opt/maternity-backend/.env${NC}"
echo -e "  • View: ${YELLOW}cat /opt/maternity-backend/.env | grep ADMIN${NC}"
echo ""
echo -e "${GREEN}Management Commands:${NC}"
echo -e "  • View logs: ${YELLOW}cd /opt/maternity-backend && docker-compose logs -f${NC}"
echo -e "  • Restart: ${YELLOW}cd /opt/maternity-backend && docker-compose restart${NC}"
echo -e "  • Stop: ${YELLOW}cd /opt/maternity-backend && docker-compose down${NC}"
echo -e "  • Update: ${YELLOW}cd /opt/maternity-backend && git pull && ./deploy.sh${NC}"
echo ""
echo -e "${BLUE}=========================================${NC}"
