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
                ./${svc}
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
      steps {
        sh """
          kubectl apply -f k8s/infra/namespace.yaml
          kubectl -n ${NAMESPACE} apply -f k8s/infra/
        """
      }
    }
    stage('Deploy Services') {
      steps {
        sh """
          kubectl -n ${NAMESPACE} apply -f k8s/services/
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
