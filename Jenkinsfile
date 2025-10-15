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
    }

    stages {
        stage('1. Checkout') {
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                retry(3) {
                    timeout(time: 10, unit: 'MINUTES') {
                        git branch: "${params.BRANCH}",
                            url: 'https://github.com/369855707/Matera.git'
                    }
                }
                echo "✓ Checkout completed successfully"
            }
        }

        stage('2. Transfer Source Code to Server') {
            steps {
                echo "Transferring source code to ${params.SERVER_HOST}..."
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        set -e
                        echo "========================================="
                        echo "Preparing deployment directory..."
                        echo "========================================="

                        # Create deployment directory if it doesn't exist
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} "
                            mkdir -p ${DEPLOY_PATH}
                            echo '✓ Deployment directory ready'
                        "

                        echo "========================================="
                        echo "Transferring source code..."
                        echo "========================================="

                        # Use rsync to transfer source code efficiently
                        rsync -avz --delete \
                            --exclude '.git' \
                            --exclude 'node_modules' \
                            --exclude 'target' \
                            --exclude '.gradle' \
                            --exclude 'build' \
                            -e "ssh -o StrictHostKeyChecking=no" \
                            ./ root@${params.SERVER_HOST}:${DEPLOY_PATH}/

                        echo "✓ Source code transferred successfully"

                        # Verify transfer
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} "
                            echo 'Verifying transferred files...'
                            ls -la ${DEPLOY_PATH}/ | head -20
                            if [ -f ${DEPLOY_PATH}/Dockerfile ]; then
                                echo '✓ Verification passed: Dockerfile found'
                            else
                                echo '✗ Verification failed: Dockerfile not found'
                                exit 1
                            fi
                        "
                    """
                }
            }
        }

        stage('3. Build Docker Image on Server') {
            steps {
                echo "Building Docker image on ${params.SERVER_HOST}..."
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            set -e
                            echo "========================================="
                            echo "Building Docker image on server..."
                            echo "========================================="

                            cd ${DEPLOY_PATH}

                            # Build Docker image with tag
                            docker build -t maternity-backend:${BUILD_NUMBER} .

                            # Also tag as latest
                            docker tag maternity-backend:${BUILD_NUMBER} maternity-backend:latest

                            echo ""
                            echo "Verifying built image..."
                            docker images | grep maternity-backend

                            # Verify image was created successfully
                            if docker images | grep -q "maternity-backend.*${BUILD_NUMBER}"; then
                                echo "✓ Docker image built successfully!"
                            else
                                echo "✗ Docker image build failed!"
                                exit 1
                            fi
                        '
                    """
                }
            }
        }

        stage('4. Stop Old Container') {
            steps {
                echo "Stopping old container on ${params.SERVER_HOST}..."
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            set -e
                            echo "========================================="
                            echo "Stopping existing containers..."
                            echo "========================================="

                            cd ${DEPLOY_PATH}

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
                        '
                    """
                }
            }
        }

        stage('5. Start New Container') {
            steps {
                echo "Starting new container on ${params.SERVER_HOST}..."
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            set -e
                            echo "========================================="
                            echo "Updating docker-compose configuration..."
                            echo "========================================="

                            cd ${DEPLOY_PATH}

                            # Update docker-compose.yml to use the new image
                            sed -i "s|maternity-backend:.*|maternity-backend:${BUILD_NUMBER}|g" docker-compose.yml

                            echo "Configuration updated to use maternity-backend:${BUILD_NUMBER}"

                            echo "========================================="
                            echo "Starting new containers..."
                            echo "========================================="

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
                        '
                    """
                }
            }
        }

        stage('6. Verify Instance Version') {
            steps {
                echo 'Verifying application version and health...'
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            set -e
                            echo "========================================="
                            echo "Verifying deployment..."
                            echo "========================================="

                            cd ${DEPLOY_PATH}

                            # Check container status
                            echo ""
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
