#!/bin/bash

# ========================================
# Docker Installation for China (CentOS/Tencent Cloud)
# ========================================

set -e

echo "Installing Docker on CentOS/Tencent Cloud..."

# Remove old versions
yum remove -y docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine

# Install required packages
yum install -y yum-utils device-mapper-persistent-data lvm2

# Add Aliyun Docker repository
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# Install Docker
yum install -y docker-ce docker-ce-cli containerd.io

# Start Docker
systemctl start docker
systemctl enable docker

# Verify installation
docker --version

echo "Docker installed successfully!"

# Install Docker Compose
echo "Installing Docker Compose..."

# Download from mirror
COMPOSE_VERSION="v2.24.0"
curl -L "https://mirror.ghproxy.com/https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose || \
curl -L "https://get.daocloud.io/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

chmod +x /usr/local/bin/docker-compose

# Verify installation
docker-compose --version

echo "Docker Compose installed successfully!"
echo ""
echo "You can now run: ./server-setup.sh"
