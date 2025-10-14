#!/bin/bash

# ========================================
# Jenkins Installation Script for macOS
# ========================================

set -e

echo "========================================="
echo "Jenkins Installation for macOS"
echo "========================================="

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "Error: Homebrew is not installed."
    echo "Please install Homebrew first: https://brew.sh"
    exit 1
fi

echo "Step 1: Installing Jenkins..."
brew install jenkins-lts

echo ""
echo "Step 2: Starting Jenkins service..."
brew services start jenkins-lts

echo ""
echo "========================================="
echo "Jenkins Installation Complete!"
echo "========================================="
echo ""
echo "Jenkins is starting up (this may take a minute)..."
echo ""
echo "Access Jenkins at: http://localhost:8080"
echo ""
echo "Initial admin password location:"
echo "  ~/.jenkins/secrets/initialAdminPassword"
echo ""
echo "To view the password, run:"
echo "  cat ~/.jenkins/secrets/initialAdminPassword"
echo ""
echo "Next steps:"
echo "1. Wait 1-2 minutes for Jenkins to start"
echo "2. Open http://localhost:8080 in your browser"
echo "3. Copy the initial admin password"
echo "4. Follow the setup wizard"
echo "5. Install suggested plugins"
echo ""
echo "Required plugins for this project:"
echo "  - Git plugin"
echo "  - Pipeline plugin"
echo "  - SSH Agent plugin"
echo "  - Publish Over SSH plugin"
echo ""
