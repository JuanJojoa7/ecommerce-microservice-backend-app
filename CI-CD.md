# Taller 2: Pruebas y lanzamiento

Este documento resume cómo se cumplen los ítems obligatorios de la rúbrica usando este repo.

- Servicios elegidos (se comunican vía Eureka y API Gateway): api-gateway, user-service, product-service, order-service, payment-service, shipping-service. Se incluye favourite-service para flujos.
- Dev: construcción y pruebas unitarias por servicio con Maven (`mvnw clean package`).
- Stage: despliegue en Kubernetes (namespaces `ecommerce-stage`) y ejecución de pruebas E2E y performance sobre la app desplegada.
- Master: despliegue a `ecommerce-prod` + generación automática de Release Notes.

## Cómo correr el proyecto (según README)
- Build: `./mvnw clean package`
- Levantar todo: `docker-compose -f compose.yml up`

## Jenkins
- Jenkinsfiles por servicio: `api-gateway/Jenkinsfile`, `user-service/Jenkinsfile`, `product-service/Jenkinsfile`, `order-service/Jenkinsfile`, `payment-service/Jenkinsfile`, `shipping-service/Jenkinsfile`.
- Plantilla: `.jenkins/Jenkinsfile.shared`.
- Parámetros por rama: dev (build+unit), stage (deploy k8s + e2e + locust), master (deploy prod + release notes).

## Kubernetes (mínimo requerido)
- Namespaces: `k8s/dev/namespace.yaml`, `k8s/stage/namespace.yaml`, `k8s/prod/namespace.yaml`.
- Agregue los manifests por servicio según su cluster (Service/Deployment). No se inventan valores fuera del repo.

## Pruebas
- Unitarias (>=5): agregadas en `product-service`, `payment-service`, `user-service`, `shipping-service`, etc.
- Integración (>=5): ejemplo en `order-service` con `@SpringBootTest`. Añada más siguiendo el patrón.
- E2E (>=5): módulo `e2e-tests` con `GatewayFlowsE2E.java` cubre 5 rutas reales a través de API Gateway.
- Rendimiento: `performance/locust/locustfile.py` usando `GATEWAY_BASE_URL`.

## Release Notes
- Script: `scripts/generate-release-notes.sh` usando git log.

## Variables
- `GATEWAY_BASE_URL`: URL del API Gateway (por defecto `http://localhost:8080`).

> Nota: Para detalles de puertos y rutas consulte siempre `README.md` y los `application.yml` de cada microservicio.
