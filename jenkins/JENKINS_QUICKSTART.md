# Jenkins CI/CD - Quick Start Guide

Get your Jenkins pipeline running in 10 minutes!

## Prerequisites
- macOS with Homebrew
- SSH access to server (129.211.167.49)

---

## Step 1: Install Jenkins (2 minutes)

```bash
chmod +x jenkins-setup.sh
./jenkins-setup.sh
```

Wait for Jenkins to start, then:
1. Open http://localhost:8080
2. Get password: `cat ~/.jenkins/secrets/initialAdminPassword`
3. Paste password → Install suggested plugins
4. Create admin user → Save and Continue

---

## Step 2: Setup SSH Keys (2 minutes)

```bash
chmod +x setup-ssh-keys.sh
./setup-ssh-keys.sh
```

Enter your server password when prompted.

---

## Step 3: Add SSH Credentials to Jenkins (2 minutes)

1. Jenkins → **Manage Jenkins** → **Credentials**
2. Click **System** → **Global credentials** → **Add Credentials**
3. Fill in:
   - Kind: **SSH Username with private key**
   - ID: `tencent-server-ssh`
   - Username: `root`
   - Private Key: **Enter directly**
   - Paste content from: `cat ~/.ssh/jenkins_tencent_rsa`
4. Click **Create**

---

## Step 4: Install Required Plugins (2 minutes)

1. Jenkins → **Manage Jenkins** → **Plugins** → **Available plugins**
2. Search and install:
   - SSH Agent Plugin
3. Restart Jenkins

---

## Step 5: Create Pipeline Job (2 minutes)

1. Jenkins home → **New Item**
2. Name: `maternity-backend-deploy`
3. Type: **Pipeline** → OK
4. Configure:
   - **Pipeline → Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** `https://github.com/369855707/Matera.git`
   - **Branch:** `*/feat/deployment-setup`
   - **Script Path:** `Jenkinsfile`
5. Click **Save**

---

## Step 6: Run Deployment! (2 minutes)

1. Go to job: http://localhost:8080/job/maternity-backend-deploy/
2. Click **Build Now**
3. Watch build progress in **Console Output**

Wait for:
- ✅ Checkout
- ✅ Deploy to Server
- ✅ Health Check

**Success!** Your app is deployed at:
- http://129.211.167.49:8080

---

## Troubleshooting

### SSH Connection Failed?
```bash
# Test SSH manually
ssh -i ~/.ssh/jenkins_tencent_rsa root@129.211.167.49
```

### Docker Build Timeout?
Server mirrors not configured. SSH to server:
```bash
ssh root@129.211.167.49
cd /opt/maternity-backend
./server-setup.sh  # Configure Docker mirrors
```

### Need detailed guide?
Read [JENKINS_SETUP.md](./JENKINS_SETUP.md) for complete documentation.

---

## What's Next?

- Add GitHub webhook for auto-deploy on push
- Set up different branches (dev/staging/prod)
- Add automated tests
- Configure notifications

Happy deploying! 🚀
