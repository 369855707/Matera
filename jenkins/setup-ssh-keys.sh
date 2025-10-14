#!/bin/bash

# ========================================
# SSH Key Setup for Jenkins → Tencent Server
# ========================================

set -e

SERVER_HOST="129.211.167.49"
SERVER_USER="root"
SSH_KEY_PATH="$HOME/.ssh/jenkins_tencent_rsa"

echo "========================================="
echo "SSH Key Setup for Jenkins"
echo "========================================="
echo ""
echo "This script will:"
echo "1. Generate SSH key pair (if not exists)"
echo "2. Copy public key to Tencent server"
echo "3. Test SSH connection"
echo ""

# Check if key already exists
if [ -f "$SSH_KEY_PATH" ]; then
    echo "SSH key already exists at: $SSH_KEY_PATH"
    read -p "Do you want to use the existing key? (y/n): " USE_EXISTING
    if [[ ! "$USE_EXISTING" =~ ^[Yy]$ ]]; then
        echo "Please manually remove the old key and run this script again."
        exit 1
    fi
else
    echo "Step 1: Generating new SSH key..."
    ssh-keygen -t rsa -b 4096 -f "$SSH_KEY_PATH" -N "" -C "jenkins@maternity-backend"
    echo "✓ SSH key generated at: $SSH_KEY_PATH"
fi

echo ""
echo "Step 2: Copying public key to server..."
echo "You will be prompted for the server password."
echo ""

# Copy public key to server
if ssh-copy-id -i "${SSH_KEY_PATH}.pub" "${SERVER_USER}@${SERVER_HOST}"; then
    echo "✓ Public key copied to server"
else
    echo "✗ Failed to copy public key"
    echo ""
    echo "Manual steps:"
    echo "1. Copy the public key:"
    cat "${SSH_KEY_PATH}.pub"
    echo ""
    echo "2. SSH into your server:"
    echo "   ssh ${SERVER_USER}@${SERVER_HOST}"
    echo ""
    echo "3. Add the key to authorized_keys:"
    echo "   mkdir -p ~/.ssh"
    echo "   echo 'PASTE_PUBLIC_KEY_HERE' >> ~/.ssh/authorized_keys"
    echo "   chmod 600 ~/.ssh/authorized_keys"
    echo "   chmod 700 ~/.ssh"
    exit 1
fi

echo ""
echo "Step 3: Testing SSH connection..."
if ssh -i "$SSH_KEY_PATH" -o StrictHostKeyChecking=no "${SERVER_USER}@${SERVER_HOST}" 'echo "Connection successful!"'; then
    echo "✓ SSH connection test passed!"
else
    echo "✗ SSH connection test failed"
    exit 1
fi

echo ""
echo "========================================="
echo "SSH Setup Complete!"
echo "========================================="
echo ""
echo "Private key location: $SSH_KEY_PATH"
echo "Public key location: ${SSH_KEY_PATH}.pub"
echo ""
echo "Next steps for Jenkins:"
echo "1. Open Jenkins: http://localhost:8080"
echo "2. Go to: Manage Jenkins → Credentials"
echo "3. Add new credential:"
echo "   - Kind: SSH Username with private key"
echo "   - ID: tencent-server-ssh"
echo "   - Username: root"
echo "   - Private Key: Enter directly"
echo "   - Copy and paste the content of: $SSH_KEY_PATH"
echo ""
echo "To view the private key:"
echo "  cat $SSH_KEY_PATH"
echo ""
