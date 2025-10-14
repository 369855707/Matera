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
        stage('Checkout') {
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                git branch: "${params.BRANCH}",
                    url: 'https://github.com/369855707/Matera.git'
            }
        }

        stage('Deploy to Server') {
            steps {
                echo "Deploying to ${params.SERVER_HOST}..."
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            set -e
                            echo "========================================="
                            echo "Starting deployment on server..."
                            echo "========================================="

                            # Navigate to project directory
                            cd ${DEPLOY_PATH}

                            # Pull latest code
                            echo "Pulling latest code from ${params.BRANCH}..."
                            git fetch --all
                            git checkout ${params.BRANCH}
                            git pull origin ${params.BRANCH}

                            # Stop existing containers
                            echo "Stopping existing containers..."
                            docker-compose down || true

                            # Build new image
                            echo "Building Docker image..."
                            docker-compose build --no-cache

                            # Start containers
                            echo "Starting containers..."
                            docker-compose up -d

                            # Wait for application to start
                            echo "Waiting for application to start..."
                            sleep 15

                            echo "Deployment completed!"
                        '
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                echo 'Performing health check...'
                sshagent(credentials: ['tencent-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no root@${params.SERVER_HOST} '
                            # Check container status
                            echo "Container status:"
                            docker-compose -f ${DEPLOY_PATH}/${DOCKER_COMPOSE_FILE} ps

                            # Check application health
                            echo "Checking application health..."
                            for i in {1..12}; do
                                if curl -f http://localhost:8080/actuator/health -s > /dev/null; then
                                    echo "✓ Application is healthy!"
                                    exit 0
                                fi
                                echo "Waiting for application... (\$i/12)"
                                sleep 5
                            done

                            echo "✗ Health check failed!"
                            docker-compose -f ${DEPLOY_PATH}/${DOCKER_COMPOSE_FILE} logs --tail=50
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
