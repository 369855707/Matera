# Deployment Guide - Maternity Backend

This guide explains how to deploy the backend to your server (129.211.167.49).

## Prerequisites

On your server, ensure you have:
- Docker (20.10+)
- Docker Compose (2.0+)
- Git
- Port 8080 available

## Installation

### 1. Install Docker & Docker Compose (if not already installed)

```bash
# Update system
sudo apt-get update

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to docker group (to run without sudo)
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version

# Log out and back in for group changes to take effect
```

## Deployment Steps

### 2. Clone Repository on Server

```bash
# SSH into your server
ssh root@129.211.167.49

# Clone the repository
cd /opt
git clone <your-repo-url> maternity-backend
cd maternity-backend
```

### 3. Configure Environment

```bash
# Copy the environment template
cp .env.production .env

# Edit the environment file with your actual values
nano .env
```

**Important: Update these values in `.env`:**

1. **DB_PASSWORD**: Use a strong database password
2. **JWT_SECRET**: Generate a secure random key:
   ```bash
   openssl rand -base64 64
   ```
3. **ADMIN_PASSWORD**: Change the default admin password
4. **CORS_ALLOWED_ORIGINS**: Add your frontend URL

Example `.env`:
```bash
DB_PASSWORD=MySecurePassword123!
JWT_SECRET=kX9mZ2pL5vR8wN4qT7yH1jF3dK6aS0bV9cE2xW5nM8gP1lQ4uJ7iO0tY3rA6zB9
JWT_EXPIRATION=86400000
ADMIN_USERNAME=admin
ADMIN_PASSWORD=MyAdminPass456!
WECHAT_APPID=
WECHAT_SECRET=
CORS_ALLOWED_ORIGINS=http://129.211.167.49:3000,http://129.211.167.49
```

### 4. Deploy

```bash
# Make deployment script executable
chmod +x deploy.sh

# Run deployment
./deploy.sh
```

The script will:
1. Stop existing containers
2. Pull latest code
3. Build Docker image
4. Start containers
5. Verify health status

### 5. Verify Deployment

Once deployed, check:

```bash
# Check container status
docker-compose ps

# View logs
docker-compose logs -f backend

# Test health endpoint
curl http://localhost:8080/actuator/health

# Test from external
curl http://129.211.167.49:8080/actuator/health
```

## Post-Deployment

### Access the Application

- **API Base URL**: http://129.211.167.49:8080
- **Health Check**: http://129.211.167.49:8080/actuator/health
- **API Documentation**: http://129.211.167.49:8080/swagger-ui.html
- **H2 Console** (dev only): http://129.211.167.49:8080/h2-console

### Test Admin Login

```bash
# Login as admin
curl -X POST http://129.211.167.49:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your-admin-password"}'
```

You should receive a JWT token in response.

## Management Commands

### View Logs

```bash
# Follow logs
docker-compose logs -f backend

# View last 100 lines
docker-compose logs --tail=100 backend
```

### Restart Application

```bash
# Restart container
docker-compose restart backend

# Or rebuild and restart
./deploy.sh
```

### Stop Application

```bash
docker-compose down
```

### Update to Latest Code

```bash
# Pull latest code and redeploy
git pull origin main
./deploy.sh
```

### Backup Database

```bash
# Backup H2 database
docker cp maternity-backend:/data/h2 ./backup-$(date +%Y%m%d)
```

### View Resource Usage

```bash
# Container stats
docker stats maternity-backend

# Disk usage
docker system df
```

## Firewall Configuration

Make sure port 8080 is open:

```bash
# UFW (Ubuntu)
sudo ufw allow 8080/tcp
sudo ufw status

# Or iptables
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
```

## Troubleshooting

### Container Won't Start

```bash
# Check logs
docker-compose logs backend

# Check if port is already in use
sudo lsof -i :8080

# Remove old containers and volumes
docker-compose down -v
./deploy.sh
```

### Out of Memory

```bash
# Adjust Java memory in docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m ...

# Then redeploy
./deploy.sh
```

### Database Issues

```bash
# Reset database (WARNING: deletes all data)
docker-compose down -v
./deploy.sh
```

### Health Check Fails

```bash
# Check if application is running
curl http://localhost:8080/actuator/health

# Check logs for errors
docker-compose logs --tail=100 backend

# Increase health check timeout in docker-compose.yml
```

## Production Best Practices

1. **Use HTTPS**: Set up nginx reverse proxy with SSL certificate
2. **Regular Backups**: Schedule daily database backups
3. **Monitoring**: Set up monitoring with Prometheus/Grafana
4. **Log Rotation**: Configure log rotation to prevent disk full
5. **Update Regularly**: Keep Docker images and dependencies updated
6. **Secure Secrets**: Never commit `.env` file to git

## Nginx Reverse Proxy (Optional)

For production, use nginx with SSL:

```nginx
server {
    listen 80;
    server_name 129.211.167.49;

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Support

For issues or questions:
- Check logs: `docker-compose logs -f backend`
- Review this guide
- Check application status: `docker-compose ps`
