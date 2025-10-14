# Jenkins CI/CD Setup Guide

Complete guide to set up Jenkins on your Mac for deploying to Tencent Cloud server.

## Architecture

```
Local Mac (Jenkins) → GitHub → SSH → Tencent Server (Docker)
```

## Prerequisites

- macOS with Homebrew installed
- SSH access to Tencent Cloud server (129.211.167.49)
- GitHub repository access

---

## Part 1: Install Jenkins

### Step 1: Run Installation Script

```bash
chmod +x jenkins-setup.sh
./jenkins-setup.sh
```

This will:
- Install Jenkins LTS via Homebrew
- Start Jenkins service
- Display setup instructions

### Step 2: Initial Jenkins Setup

1. Wait 1-2 minutes for Jenkins to start
2. Open http://localhost:8080 in your browser
3. Get the initial admin password:
   ```bash
   cat ~/.jenkins/secrets/initialAdminPassword
   ```
4. Copy and paste the password into Jenkins
5. Click "Install suggested plugins"
6. Create your first admin user
7. Keep the Jenkins URL as http://localhost:8080

### Step 3: Install Required Plugins

Navigate to: **Manage Jenkins → Plugins → Available plugins**

Search and install:
- ✅ Git plugin (usually pre-installed)
- ✅ Pipeline plugin (usually pre-installed)
- ✅ SSH Agent Plugin
- ✅ Publish Over SSH (optional, for alternative deployment)

Click "Install" and restart Jenkins when done.

---

## Part 2: Configure SSH Access

### Step 1: Generate and Deploy SSH Keys

```bash
chmod +x setup-ssh-keys.sh
./setup-ssh-keys.sh
```

This will:
1. Generate SSH key pair at `~/.ssh/jenkins_tencent_rsa`
2. Copy public key to Tencent server
3. Test SSH connection

**Note:** You'll need your server password during this step.

### Step 2: Add SSH Credentials to Jenkins

1. Open Jenkins: http://localhost:8080
2. Navigate to: **Manage Jenkins → Credentials → System → Global credentials → Add Credentials**
3. Fill in the form:
   - **Kind:** SSH Username with private key
   - **ID:** `tencent-server-ssh` (important! must match Jenkinsfile)
   - **Description:** Tencent Cloud Server SSH
   - **Username:** `root`
   - **Private Key:** Select "Enter directly"
   - Click "Add" and paste the content of:
     ```bash
     cat ~/.ssh/jenkins_tencent_rsa
     ```
4. Click "Create"

### Step 3: Verify SSH Credential

Test the connection manually:
```bash
ssh -i ~/.ssh/jenkins_tencent_rsa root@129.211.167.49
```

If successful, you should be logged into your server.

---

## Part 3: Create Jenkins Pipeline Job

### Step 1: Create New Pipeline Job

1. Go to Jenkins home: http://localhost:8080
2. Click "New Item"
3. Enter name: `maternity-backend-deploy`
4. Select "Pipeline"
5. Click "OK"

### Step 2: Configure Pipeline

In the job configuration page:

#### General Section:
- ✅ Check "This project is parameterized"
- Add parameters (these are already defined in Jenkinsfile, but you can override):
  - Choice Parameter:
    - Name: `BRANCH`
    - Choices: `main`, `feat/deployment-setup`, `develop`
  - String Parameter:
    - Name: `SERVER_HOST`
    - Default Value: `129.211.167.49`

#### Pipeline Section:
- **Definition:** Pipeline script from SCM
- **SCM:** Git
- **Repository URL:** `https://github.com/369855707/Matera.git`
- **Credentials:** (leave blank if public repo)
- **Branch Specifier:** `*/${BRANCH}` or just `*/feat/deployment-setup`
- **Script Path:** `Jenkinsfile`

#### Build Triggers (Optional):
Choose one or more:
- ☐ **Build periodically:** `H/30 * * * *` (every 30 minutes)
- ☐ **Poll SCM:** `H/5 * * * *` (check GitHub every 5 minutes)
- ☑ **Build manually** (recommended for start)

Click "Save"

---

## Part 4: Run Your First Deployment

### Step 1: Trigger Build

1. Go to your pipeline job: http://localhost:8080/job/maternity-backend-deploy/
2. Click "Build with Parameters"
3. Select options:
   - **BRANCH:** `feat/deployment-setup` (or your preferred branch)
   - **SERVER_HOST:** `129.211.167.49`
4. Click "Build"

### Step 2: Monitor Progress

Watch the build in real-time:
- Click on the build number (e.g., #1)
- Click "Console Output"
- Watch the deployment progress

Expected stages:
1. ✅ Checkout - Pull code from GitHub
2. ✅ Prepare Deployment - Make scripts executable
3. ✅ Deploy to Server - SSH to server, build Docker image, start containers
4. ✅ Health Check - Verify application is running

### Step 3: Verify Deployment

Once the build succeeds:
1. Check application: http://129.211.167.49:8080/actuator/health
2. View API docs: http://129.211.167.49:8080/swagger-ui.html
3. Test admin login with credentials from `.env` file

---

## Part 5: Troubleshooting

### Build Fails at Checkout Stage

**Issue:** Cannot clone from GitHub

**Solution:**
```bash
# On your server, test git access:
ssh root@129.211.167.49
cd /opt/maternity-backend
git pull origin feat/deployment-setup
```

If GitHub is timing out, you may need to configure git to use a proxy or retry.

### Build Fails at Deploy Stage

**Issue:** SSH connection failed

**Solution:**
1. Verify SSH credential ID matches: `tencent-server-ssh`
2. Test SSH manually:
   ```bash
   ssh -i ~/.ssh/jenkins_tencent_rsa root@129.211.167.49
   ```
3. Check Jenkins credentials configuration

### Build Fails at Docker Build

**Issue:** Docker image pull timeout

**Solution:**
Ensure Docker mirrors are configured on the server:
```bash
ssh root@129.211.167.49
cat /etc/docker/daemon.json
```

Should contain:
```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

If not, run:
```bash
cd /opt/maternity-backend
./server-setup.sh
```

### Health Check Fails

**Issue:** Application doesn't start

**Solution:**
1. SSH into server:
   ```bash
   ssh root@129.211.167.49
   cd /opt/maternity-backend
   ```
2. Check container logs:
   ```bash
   docker-compose logs -f backend
   ```
3. Check container status:
   ```bash
   docker-compose ps
   ```

---

## Part 6: Advanced Configuration

### Option 1: GitHub Webhook (Auto-deploy on push)

1. Go to GitHub repository → Settings → Webhooks
2. Add webhook:
   - **Payload URL:** `http://YOUR_MAC_IP:8080/github-webhook/`
   - **Content type:** `application/json`
   - **Events:** Just the push event
3. In Jenkins job configuration:
   - ✅ Check "GitHub hook trigger for GITScm polling"

**Note:** Your Mac must be accessible from the internet, or use a tool like ngrok.

### Option 2: Scheduled Deployments

In Jenkins job configuration → Build Triggers:
- ✅ Build periodically
- Schedule: `0 2 * * *` (2 AM daily)

### Option 3: Multi-Environment Deployment

Add more choice parameters:
- **ENVIRONMENT:** `dev`, `staging`, `production`
- **SERVER_HOST:** Different IPs for each environment

Update Jenkinsfile to use these parameters.

---

## Part 7: Maintenance

### View Jenkins Logs

```bash
tail -f ~/.jenkins/logs/jenkins.log
```

### Restart Jenkins

```bash
brew services restart jenkins-lts
```

### Stop Jenkins

```bash
brew services stop jenkins-lts
```

### Backup Jenkins Configuration

```bash
# Backup entire Jenkins home
tar -czf jenkins-backup-$(date +%Y%m%d).tar.gz ~/.jenkins

# Backup just job configs
tar -czf jenkins-jobs-$(date +%Y%m%d).tar.gz ~/.jenkins/jobs
```

---

## Quick Reference

### URLs
- **Jenkins:** http://localhost:8080
- **Application:** http://129.211.167.49:8080
- **Health:** http://129.211.167.49:8080/actuator/health
- **Swagger:** http://129.211.167.49:8080/swagger-ui.html

### Commands

```bash
# Start Jenkins
brew services start jenkins-lts

# View Jenkins password
cat ~/.jenkins/secrets/initialAdminPassword

# Test SSH connection
ssh -i ~/.ssh/jenkins_tencent_rsa root@129.211.167.49

# SSH to server
ssh root@129.211.167.49

# View server logs
ssh root@129.211.167.49 'cd /opt/maternity-backend && docker-compose logs -f'

# Restart application manually
ssh root@129.211.167.49 'cd /opt/maternity-backend && docker-compose restart'
```

---

## Security Best Practices

1. ✅ Use SSH keys (not passwords)
2. ✅ Restrict Jenkins access (consider nginx reverse proxy with auth)
3. ✅ Use separate credentials for different environments
4. ✅ Enable Jenkins security (Manage Jenkins → Configure Global Security)
5. ✅ Keep Jenkins and plugins updated
6. ✅ Store sensitive data in Jenkins credentials, not in code
7. ✅ Use `.gitignore` to exclude `.env` files
8. ✅ Regularly backup Jenkins configuration

---

## Next Steps

1. ✅ Set up monitoring (Prometheus + Grafana)
2. ✅ Add automated tests before deployment
3. ✅ Configure rollback mechanism
4. ✅ Set up multiple environments (dev/staging/prod)
5. ✅ Add Slack/Email notifications for build status
6. ✅ Implement blue-green deployment

---

## Support

If you encounter issues:
1. Check Jenkins console output
2. Review server logs: `docker-compose logs -f`
3. Verify SSH connectivity
4. Check GitHub repository access
5. Ensure Docker is running on server

Happy deploying! 🚀
