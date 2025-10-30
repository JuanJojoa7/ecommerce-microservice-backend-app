pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  namespace: devops
spec:
  serviceAccountName: jenkins-sa
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-17
    command:
    - cat
    tty: true
  - name: docker
    image: docker:dind
    securityContext:
      privileged: true
    command:
    - dockerd
    tty: true
  - name: kubectl
    image: dtzar/helm-kubectl:3.12
    command:
    - cat
    tty: true
  - name: node
    image: node:20
    command:
    - cat
    tty: true
'''
        }
    }

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/JuanJojoa7/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Build with Maven') {
            steps {
                container('maven') {
                    script {
                        def services = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
                        services.each { service ->
                            dir(service) {
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
            }
        }

        stage('Run Unitary Tests') {
            steps {
                container('maven') {
                    script {
                        def services = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
                        services.each { service ->
                            dir(service) {
                                sh 'mvn test'
                            }
                        }
                    }
                }
            }
        }

        stage ('Run Integration Tests') {
            steps {
                container('maven') {
                    script {
                        def services = ['favourite-service', 'order-service', 'payment-service', 'product-service', 'service-discovery', 'shipping-service', 'user-service']
                        services.each { service ->
                            dir(service) {
                                sh 'mvn verify -Pintegration-tests'
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                container('kubectl') {
                    sh '''
                        kubectl version --client
                        helm version
                        kubectl create namespace ecommerce-dev --dry-run=client -o yaml | kubectl apply -f -
                        helm upgrade --install ecommerce ./helm-charts/ecommerce --namespace ecommerce-dev
                    '''
                }
            }
        }

                stage('Run E2E Tests') {
                        steps {
                                // Run services with Docker Compose inside the docker:dind container and execute Cypress in a container sharing the host network
                                container('docker') {
                                        script {
                                                sh '''
                                                    set -euxo pipefail
                                                    echo "Docker version:" && docker version
                                                    # Prefer docker compose plugin; fallback to docker-compose if needed
                                                    if docker compose version >/dev/null 2>&1; then
                                                        COMPOSE="docker compose"
                                                    elif command -v docker-compose >/dev/null 2>&1; then
                                                        COMPOSE="docker-compose"
                                                    else
                                                        echo "Docker Compose is not available" >&2
                                                        exit 1
                                                    fi

                                                    # Build and start the entire stack (core + services) as one compose project
                                                    $COMPOSE -f core.yml -f compose.yml up -d --build

                                                    # Helper to wait for an HTTP endpoint to be ready via the dind host network
                                                    wait_for() {
                                                        local url="$1"; local name="$2"; local retries="${3:-60}"; local delay="${4:-5}";
                                                        echo "Waiting for ${name} at ${url} ..."
                                                        for i in $(seq 1 "$retries"); do
                                                            if docker run --rm --network=host curlimages/curl:8.10.1 -sSf -o /dev/null "$url"; then
                                                                echo "${name} is UP"
                                                                return 0
                                                            fi
                                                            echo "Attempt $i/${retries} not ready yet; sleeping ${delay}s"
                                                            sleep "$delay"
                                                        done
                                                        echo "Timeout waiting for ${name} at ${url}" >&2
                                                        return 1
                                                    }

                                                    # Wait for core services first
                                                    wait_for http://localhost:9296/actuator/health "cloud-config" 60 5
                                                    wait_for http://localhost:8761/actuator/health "service-discovery (Eureka)" 60 5
                                                    wait_for http://localhost:9411/health "zipkin" 60 5

                                                    # Then wait for a subset of business services
                                                    wait_for http://localhost:8700/actuator/health "user-service" 60 5
                                                    wait_for http://localhost:8500/actuator/health "product-service" 60 5
                                                    wait_for http://localhost:8300/actuator/health "order-service" 60 5
                                                    wait_for http://localhost:8400/actuator/health "payment-service" 60 5
                                                    wait_for http://localhost:8600/actuator/health "shipping-service" 60 5
                                                    wait_for http://localhost:8800/actuator/health "favourite-service" 60 5
                                                    wait_for http://localhost:8080/actuator/health "api-gateway" 60 5 || true

                                                    # Run Cypress using the included image, sharing the dind host network so localhost:* works
                                                    docker run --rm \
                                                        --network=host \
                                                        -v "$PWD/e2e-tests":/e2e \
                                                        -w /e2e \
                                                        -e CYPRESS_VERIFY_TIMEOUT=180000 \
                                                        cypress/included:15.5.0
                                                '''
                                        }
                                }
                        }
                }
    }

    post {
        always {
                        container('docker') {
                                // Tear down stack and cleanup space
                                script {
                                        sh '''
                                            set +e
                                            if docker compose version >/dev/null 2>&1; then
                                                docker compose -f core.yml -f compose.yml down -v
                                            elif command -v docker-compose >/dev/null 2>&1; then
                                                docker-compose -f core.yml -f compose.yml down -v
                                            fi
                                            docker system prune -f
                                        '''
                                }
                        }
        }
    }
}