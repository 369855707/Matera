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
            }
        }

        stage('2. Build Docker Image') {
            steps {
                echo "Building Docker image locally in Jenkins..."
                sh """
                    set -e
                    echo "========================================="
                    echo "Building Docker image..."
                    echo "========================================="
                    
                    # Build Docker image with tag
                    docker build -t maternity-backend:${BUILD_NUMBER} .
                    
                    # Also tag as latest
                    docker tag maternity-backend:${BUILD_NUMBER} maternity-backend:latest
                    
                    # Show image info
                    docker images | grep maternity-backend
                    
                    echo "✓ Docker image built successfully!"
                """
            }
        }

        stage('3. Save and Transfer Image') {
            steps {
                echo "Saving and transferring Docker image to ${params.SERVER_HOST}..."
                sh """
                    set -e
                    echo "========================================="
                    echo "Saving Docker image..."
                    echo "========================================="
                    
                    # Save Docker image to tar file
                    docker save maternity-backend:${BUILD_NUMBER} -o maternity-backend-${BUILD_NUMBER}.tar
                    
                    echo "✓ Docker image saved"
                    echo "========================================="
                    echo "Transferring image to server..."
                    echo "========================================="
                    
                    # Transfer tar file to server via SCP
                    scp -o StrictHostKeyChecking=no \
                        maternity-backend-${BUILD_NUMBER}.tar \
                        root@${params.SERVER_HOST}:${DEPLOY_PATH}/
                    
                    echo "✓ Image transferred successfully"
                    
                    # Cleanup local tar file
                    rm maternity-backend-${BUILD_NUMBER}.tar
                """
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
                            
                            # Stop and remove containers
                            docker-compose down || true
                            
                            echo "✓ Old containers stopped"
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
                            echo "Loading Docker image..."
                            echo "========================================="
                            
                            cd ${DEPLOY_PATH}
                            
                            # Load the transferred Docker image
                            docker load -i maternity-backend-${BUILD_NUMBER}.tar
                            
                            echo "✓ Docker image loaded"
                            
                            # Update docker-compose.yml to use the new image
                            # Assuming docker-compose.yml uses 'maternity-backend:latest'
                            sed -i \"s|maternity-backend:.*|maternity-backend:${BUILD_NUMBER}|g\" docker-compose.yml
                            
                            echo "========================================="
                            echo "Starting new containers..."
                            echo "========================================="
                            
                            # Start containers
                            docker-compose up -d
                            
                            echo "✓ New containers started"
                            
                            # Wait for application to start
                            echo "Waiting for application to initialize..."
                            sleep 15
                            
                            # Cleanup tar file
                            rm maternity-backend-${BUILD_NUMBER}.tar
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
                            docker inspect --format=\"{{.Config.Image}}\" \$(docker-compose ps -q maternity-backend) || echo "Container not found"
                            
                            # Perform health check
                            echo ""
                            echo "Checking application health..."
                            for i in {1..12}; do
                                if curl -f http://localhost:8080/actuator/health -s > /dev/null; then
                                    echo "✓ Application is healthy!"
                                    
                                    # Get additional info
                                    echo ""
                                    echo "Application Info:"
                                    curl -s http://localhost:8080/actuator/health | grep -o \"\\\"status\\\":\\\"[^\\\"]*\\\"\"
                                    
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
