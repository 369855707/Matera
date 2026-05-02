pipeline {
    agent any

    parameters {
        string(
            name: 'BRANCH',
            defaultValue: 'main',
            description: 'Branch name to deploy (e.g., main, develop, feature/xxx)'
        )
        string(
            name: 'SERVER_HOST',
            defaultValue: '129.211.167.49',
            description: 'Tencent Cloud server IP'
        )
    }

    environment {
        PROJECT_NAME = 'maternity-backend'
        DEPLOY_PATH = '/opt/maternity-backend'
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        GIT_REPO = 'https://github.com/369855707/Matera.git'
    }

    stages {
        stage('Deploy to Server') {
            steps {
                echo "Deploying ${params.BRANCH} to ${params.SERVER_HOST}..."
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            set -e

                            echo "========================================="
                            echo "Stage 1: Pull Latest Code"
                            echo "========================================="

                            # Create directory if not exists
                            mkdir -p ${DEPLOY_PATH}
                            cd ${DEPLOY_PATH}

                            # Clone or pull repository
                            if [ -d .git ]; then
                                echo "Repository exists, pulling latest changes..."

                                # Clean up any local changes or untracked files that might block pull
                                echo "Cleaning working directory..."
                                git reset --hard HEAD
                                git clean -fd

                                git fetch origin
                                git checkout ${params.BRANCH}
                                git reset --hard origin/${params.BRANCH}

                                echo "✓ Successfully updated to latest code"
                            else
                                echo "Cloning repository..."
                                git clone -b ${params.BRANCH} ${GIT_REPO} .
                            fi

                            # Verify source code
                            if [ -f Dockerfile ]; then
                                echo "✓ Source code verified: Dockerfile found"
                            else
                                echo "✗ Verification failed: Dockerfile not found"
                                exit 1
                            fi

                            echo ""
                            echo "========================================="
                            echo "Stage 2: Build Docker Image"
                            echo "========================================="

                            # Build Docker image
                            docker build -t maternity-backend:${BUILD_NUMBER} .
                            docker tag maternity-backend:${BUILD_NUMBER} maternity-backend:latest

                            # Verify image was created
                            echo ""
                            echo "Verifying built image..."
                            docker images | grep maternity-backend

                            if docker images | grep -q "maternity-backend.*${BUILD_NUMBER}"; then
                                echo "✓ Docker image built successfully!"
                            else
                                echo "✗ Docker image build failed!"
                                exit 1
                            fi

                            echo ""
                            echo "========================================="
                            echo "Stage 3: Stop Old Containers"
                            echo "========================================="

                            # Check if containers are running
                            if docker-compose ps | grep -q "Up"; then
                                echo "Found running containers, stopping them..."
                                docker-compose down
                            else
                                echo "No running containers found"
                                docker-compose down || true
                            fi

                            # Verify containers are stopped
                            if docker-compose ps | grep -q "Up"; then
                                echo "✗ Failed to stop containers!"
                                docker-compose ps
                                exit 1
                            else
                                echo "✓ Old containers stopped successfully"
                            fi

                            echo ""
                            echo "========================================="
                            echo "Stage 4: Start New Containers"
                            echo "========================================="

                            # Update docker-compose.yml to use the new image
                            sed -i "s|maternity-backend:.*|maternity-backend:${BUILD_NUMBER}|g" docker-compose.yml
                            echo "Configuration updated to use maternity-backend:${BUILD_NUMBER}"

                            # Start containers
                            docker-compose up -d

                            echo ""
                            echo "Waiting for application to initialize..."
                            sleep 15

                            # Verify containers are running
                            echo ""
                            echo "Verifying container status..."
                            docker-compose ps

                            if docker-compose ps | grep -q "Up"; then
                                echo "✓ New containers started successfully"
                            else
                                echo "✗ Containers failed to start!"
                                docker-compose logs --tail=50
                                exit 1
                            fi

                            echo ""
                            echo "========================================="
                            echo "Stage 5: Health Check"
                            echo "========================================="

                            # Check container status
                            echo "Container status:"
                            docker-compose ps

                            # Check running image version
                            echo ""
                            echo "Running image version:"
                            docker inspect --format="{{.Config.Image}}" \$(docker-compose ps -q maternity-backend) || echo "Container not found"

                            # Perform health check
                            echo ""
                            echo "Checking application health..."
                            for i in {1..12}; do
                                if curl -f http://localhost:8080/actuator/health -s > /dev/null; then
                                    echo "✓ Application is healthy!"

                                    # Get additional info
                                    echo ""
                                    echo "Application Info:"
                                    curl -s http://localhost:8080/actuator/health | grep -o "\\\"status\\\":\\\"[^\\\"]*\\\""

                                    exit 0
                                fi
                                echo "Waiting for application... (\$i/12)"
                                sleep 5
                            done

                            echo "✗ Health check failed!"
                            docker-compose logs --tail=50
                            exit 1
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo '========================================='
            echo 'Deployment Successful! 🎉'
            echo '========================================='
            echo "API URL: http://${params.SERVER_HOST}:8080"
            echo "Health: http://${params.SERVER_HOST}:8080/actuator/health"
            echo "Swagger: http://${params.SERVER_HOST}:8080/swagger-ui.html"
            echo '========================================='
        }
        failure {
            echo '========================================='
            echo 'Deployment Failed! ❌'
            echo '========================================='
            echo 'Check the logs above for details.'
            echo 'You may need to SSH into the server to investigate:'
            echo "  ssh root@${params.SERVER_HOST}"
            echo "  cd ${DEPLOY_PATH}"
            echo "  docker-compose logs -f"
            echo '========================================='
        }
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}
