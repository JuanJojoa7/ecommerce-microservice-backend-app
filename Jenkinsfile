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
        // Set your Docker Hub namespace (username or org). Update this to your account.
        DOCKERHUB_NAMESPACE = 'your-dockerhub-username'
        // Jenkins Credentials ID for Docker Hub (Username with Password / Token)
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub'
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
                        // Build ALL services that will be containerized so their JARs exist under each target/
                        def services = [
                            'api-gateway',
                            'cloud-config',
                            'proxy-client',
                            'favourite-service',
                            'order-service',
                            'payment-service',
                            'product-service',
                            'service-discovery',
                            'shipping-service',
                            'user-service'
                        ]
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

        stage('Build and Push Images') {
            steps {
                container('docker') {
                    script {
                        withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                            sh '''
                                set -euxo pipefail
                                # Login to Docker Hub
                                echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin

                                # Tag to use (short git sha)
                                TAG="${GIT_COMMIT:0:7}"

                                build_push() {
                                  local svc="$1";
                                  local path="$1";
                                  local img="docker.io/${DOCKERHUB_USER}/ecom-${svc}:${TAG}";
                                  echo "Building ${img} from ${path}"
                                  docker build -t "${img}" -f "${path}/Dockerfile" "${path}"
                                  docker push "${img}"
                                }

                                # Core and services
                                build_push api-gateway
                                build_push cloud-config
                                build_push service-discovery
                                build_push proxy-client
                                build_push user-service
                                build_push product-service
                                build_push order-service
                                build_push payment-service
                                build_push shipping-service
                                build_push favourite-service

                                # Persist variables for later stages
                                echo DOCKERHUB_USER=${DOCKERHUB_USER} > .ci-env
                                echo TAG=${TAG} >> .ci-env
                            '''
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                container('kubectl') {
                    sh '''
                        set -euxo pipefail
                        # Load image variables from previous stage
                        if [ -f .ci-env ]; then source .ci-env; else echo "Missing .ci-env with image info"; fi
                        kubectl version --client
                        # Prepare namespace
                        kubectl create namespace ecommerce-dev --dry-run=client -o yaml | kubectl apply -f -

                        # Install kompose locally (download static binary)
                        KVER="v1.32.0"
                        curl -sSL -o /usr/local/bin/kompose "https://github.com/kubernetes/kompose/releases/download/${KVER}/kompose-linux-amd64"
                        chmod +x /usr/local/bin/kompose
                        /usr/local/bin/kompose version

                        # Generate manifests from docker compose files (combine core + services to satisfy depends_on)
                        rm -rf k8s/generated || true
                        mkdir -p k8s/generated/all
                        /usr/local/bin/kompose convert -f core.yml -f compose.yml -o k8s/generated/all --namespace ecommerce-dev
                        # Remove auto-generated namespace file to avoid conflicts (we already created namespace)
                        rm -f k8s/generated/all/*namespace.yaml || true

                        # Apply manifests
                        kubectl apply -f k8s/generated/all

                                                # Kompose maps Compose host ports into pod hostPort, which blocks rollouts on a single node.
                                                # Remove hostPort from all deployments to avoid scheduling/rollout conflicts.
                                                for d in api-gateway-container cloud-config-container service-discovery-container proxy-client-container user-service-container product-service-container order-service-container payment-service-container shipping-service-container favourite-service-container; do
                                                    kubectl -n ecommerce-dev patch deployment "$d" --type='json' -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/ports/0/hostPort"}]' || true
                                                done

                        # Force-set images on live deployments to the ones we just built and pushed (avoid sed/apply drift)
                        ns=ecommerce-dev
                                                kubectl -n "$ns" set image deployment/api-gateway-container api-gateway-container="docker.io/${DOCKERHUB_USER}/ecom-api-gateway:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/cloud-config-container cloud-config-container="docker.io/${DOCKERHUB_USER}/ecom-cloud-config:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/service-discovery-container service-discovery-container="docker.io/${DOCKERHUB_USER}/ecom-service-discovery:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/proxy-client-container proxy-client-container="docker.io/${DOCKERHUB_USER}/ecom-proxy-client:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/user-service-container user-service-container="docker.io/${DOCKERHUB_USER}/ecom-user-service:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/product-service-container product-service-container="docker.io/${DOCKERHUB_USER}/ecom-product-service:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/order-service-container order-service-container="docker.io/${DOCKERHUB_USER}/ecom-order-service:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/payment-service-container payment-service-container="docker.io/${DOCKERHUB_USER}/ecom-payment-service:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/shipping-service-container shipping-service-container="docker.io/${DOCKERHUB_USER}/ecom-shipping-service:${TAG}" || true
                                                kubectl -n "$ns" set image deployment/favourite-service-container favourite-service-container="docker.io/${DOCKERHUB_USER}/ecom-favourite-service:${TAG}" || true
                        # zipkin stays public image

                        # Wait for rollouts to complete
                        for d in api-gateway-container cloud-config-container service-discovery-container proxy-client-container user-service-container product-service-container order-service-container payment-service-container shipping-service-container favourite-service-container; do
                          kubectl -n "$ns" rollout status deployment/$d --timeout=300s || true
                        done
                    '''
                }
            }
        }

        stage('K8s Smoke Tests') {
            steps {
                container('kubectl') {
                    sh '''
                        set -euxo pipefail
                        NS=ecommerce-dev
                        # Wait for pods to be Ready
                        kubectl wait --for=condition=Ready pods --all -n "$NS" --timeout=600s || true

                                                # Helper: connectivity check — succeed if TCP connect & HTTP exchange works (status code ignored)
                                                kcheck() {
                                                    local url="$1"; local name="$2";
                                                    echo "Checking ${name}: ${url}"
                                                    # Use a unique pod name to avoid name collisions on retries
                                                    kubectl -n "$NS" run curl-$(date +%s%N) --rm -i --restart=Never --image=curlimages/curl:8.10.1 -- \
                                                        sh -lc "curl -s -o /dev/null '${url}'" \
                                                        && echo "OK ${name}" || (echo "FAIL ${name}" && exit 1)
                                                }

                        # Core health
                        kcheck http://cloud-config-container:9296/actuator/health cloud-config
                        kcheck http://service-discovery-container:8761/actuator/health eureka
                        kcheck http://zipkin-container:9411/health zipkin

                        # Services connectivity (use root path to avoid actuator dependency)
                        kcheck http://user-service-container:8700/ user-service
                        kcheck http://product-service-container:8500/ product-service
                        kcheck http://order-service-container:8300/ order-service
                        kcheck http://payment-service-container:8400/ payment-service
                        kcheck http://shipping-service-container:8600/ shipping-service
                        kcheck http://favourite-service-container:8800/ favourite-service
                    '''
                }
            }
        }

        stage('Wait for Services') {
            steps {
                container('kubectl') {
                    sh '''
                        echo "Waiting for deployments to be ready..."
                        kubectl wait --for=condition=available --timeout=300s deployment --all -n ecommerce-dev || true
                        
                        echo "Listing all services in ecommerce-dev namespace:"
                        kubectl get svc -n ecommerce-dev
                        
                        echo "Listing all pods in ecommerce-dev namespace:"
                        kubectl get pods -n ecommerce-dev
                        
                        echo "Waiting additional 30 seconds for services to stabilize..."
                        sleep 30
                    '''
                }
            }
        }

        stage('Run E2E Tests') {
            steps {
                container('node') {
                    sh 'apt-get update && apt-get install -y libgtk2.0-0 libgtk-3-0 libgbm-dev libnotify-dev libgconf-2-4 libnss3-dev libxss1 libasound2-dev libxtst6 xauth xvfb curl'
                    dir('e2e-tests') {
                        sh 'npm install'
                        sh '''
                            # Test connectivity to API Gateway before running tests
                            echo "Testing connectivity to API Gateway..."
                            max_attempts=30
                            attempt=1
                            
                            while [ $attempt -le $max_attempts ]; do
                                echo "Attempt $attempt of $max_attempts..."
                                if curl -f http://api-gateway-container.ecommerce-dev.svc.cluster.local:8080/actuator/health; then
                                    echo "API Gateway is ready!"
                                    break
                                fi
                                
                                if [ $attempt -eq $max_attempts ]; then
                                    echo "API Gateway did not become ready in time"
                                    exit 1
                                fi
                                
                                echo "Waiting 10 seconds before retry..."
                                sleep 10
                                attempt=$((attempt + 1))
                            done
                            
                            # Run Cypress tests
                            export CYPRESS_BASE_URL=http://api-gateway-container.ecommerce-dev.svc.cluster.local:8080
                            export CYPRESS_EUREKA_URL=http://service-discovery-container.ecommerce-dev.svc.cluster.local:8761
                            xvfb-run -a npx cypress run --config baseUrl=http://api-gateway-container.ecommerce-dev.svc.cluster.local:8080
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            container('docker') {
                sh 'docker system prune -f'
            }
        }
    }
}