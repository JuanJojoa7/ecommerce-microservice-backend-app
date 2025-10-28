pipeline {
  agent any
  environment {
    REGISTRY = 'localhost:5000'
    NAMESPACE = 'ecommerce'
    DOCKER_BUILDKIT = '1'
    JAVA_HOME = '/opt/java/openjdk'
    PATH = "$JAVA_HOME/bin:$PATH" 
  }
  options {
    skipDefaultCheckout(true)
    timestamps()
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Setup kubectl') {
      steps {
        sh '''
          set -e
          mkdir -p "$WORKSPACE/bin"
          if ! "$WORKSPACE/bin/kubectl" version --client >/dev/null 2>&1; then
            echo "Installing kubectl locally in $WORKSPACE/bin"
            curl -sL -o "$WORKSPACE/bin/kubectl" "https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl"
            chmod +x "$WORKSPACE/bin/kubectl"
          fi
          "$WORKSPACE/bin/kubectl" version --client
        '''
      }
    }
    stage('Build & Test') {
      steps {
        sh 'chmod +x mvnw'
        sh './mvnw -B -DskipTests=false test'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
      }
    }
    stage('Package') {
      steps {
        sh './mvnw -B -DskipTests package'
      }
      post {
        success {
          archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, onlyIfSuccessful: true
        }
      }
    }
    stage('Build Images') {
      steps {
        script {
          def services = [
            'cloud-config',
            'service-discovery',
            'api-gateway',
            'proxy-client',
            'user-service',
            'product-service',
            'order-service',
            'payment-service',
            'shipping-service',
            'favourite-service'
          ]
          def sha = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
          services.each { svc ->
            sh """
              docker build \
                -t ${REGISTRY}/ecommerce/${svc}:latest \
                -t ${REGISTRY}/ecommerce/${svc}:${sha} \
                --file ./${svc}/Dockerfile \
                .
            """
          }
          sh 'docker images | head -n 50'
        }
      }
    }
    stage('Push Images') {
      steps {
        script {
          def services = [
            'cloud-config','service-discovery','api-gateway','proxy-client',
            'user-service','product-service','order-service','payment-service','shipping-service','favourite-service'
          ]
          def sha = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
          services.each { svc ->
            sh "docker push ${REGISTRY}/ecommerce/${svc}:latest"
            sh "docker push ${REGISTRY}/ecommerce/${svc}:${sha}"
          }
        }
      }
    }
    stage('Deploy Infra') {
      environment {
        PATH = "${env.WORKSPACE}/bin:${env.PATH}"
      }
      steps {
        sh """
          set -e
          # Resolve kubeconfig: prefer workspace copy, fallback to home
          mkdir -p "$WORKSPACE/.kube"
          if [ -f "$HOME/.kube/config" ] && [ ! -f "$WORKSPACE/.kube/config" ]; then
            cp "$HOME/.kube/config" "$WORKSPACE/.kube/config"
          fi
          export KUBECONFIG="$WORKSPACE/.kube/config"
          unset http_proxy https_proxy no_proxy HTTP_PROXY HTTPS_PROXY NO_PROXY || true

          kubectl version --client
          kubectl config current-context || true

          kubectl apply --validate=false -f k8s/infra/namespace.yaml
          kubectl -n ${NAMESPACE} apply --validate=false -f k8s/infra/
        """
      }
    }
    stage('Deploy Services') {
      environment {
        PATH = "${env.WORKSPACE}/bin:${env.PATH}"
      }
      steps {
        sh """
          set -e
          export KUBECONFIG="$WORKSPACE/.kube/config"
          unset http_proxy https_proxy no_proxy HTTP_PROXY HTTPS_PROXY NO_PROXY || true

          kubectl -n ${NAMESPACE} apply --validate=false -f k8s/services/
        """
      }
    }
  }
  post {
    always {
      cleanWs()
    }
  }
}
