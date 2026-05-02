# Quickstart: Payment API Platform

## Prerequisites

- Java 17 JDK
- Gradle 8.x
- Docker and Docker Compose
- Kubernetes cluster or local Kubernetes (kind/minikube)

## Local Development

1. `cd /home/chetan/repo/specbasedspringbootsample`
2. Build the Gradle multi-project:
   ```bash
   ./gradlew clean build
   ```
3. Start local Kafka and MongoDB for development:
   ```bash
   docker-compose -f infrastructure/docker-compose-dev.yml up -d
   ```
4. Run `api-gateway`:
   ```bash
   ./gradlew :services:api-gateway:bootRun
   ```
5. Run `payment-api`:
   ```bash
   ./gradlew :services:payment-api:bootRun
   ```
6. Run `payment-processor`:
   ```bash
   ./gradlew :services:payment-processor:bootRun
   ```

## Sample Request

```bash
curl -X POST https://localhost:8443/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: <uuid>" \
  -H "Content-Type: application/json" \
  -d '{
    "debtorAccountId": "debtor-123",
    "creditorAccountId": "creditor-456",
    "amount": 12500,
    "currency": "USD",
    "remittanceInformation": "Invoice 12345"
  }'
```

## Kubernetes Deployment

1. Package Docker images:
   ```bash
   ./gradlew :services:api-gateway:bootJar :services:payment-api:bootJar :services:payment-processor:bootJar
   docker build -t payments-api-gateway ./services/api-gateway
   docker build -t payments-api ./services/payment-api
   docker build -t payment-processor ./services/payment-processor
   ```
2. Deploy infrastructure manifests:
   ```bash
   kubectl apply -f infrastructure/kubernetes/base/
   ```
3. Verify pods and services:
   ```bash
   kubectl get pods
   kubectl get svc
   ```

## Operational Checks

- Confirm Kafka topics are created and healthy.
- Confirm MongoDB replica set is healthy.
- Confirm the gateway is accepting mTLS clients.
- Validate metrics endpoints on `/actuator/prometheus`.
- Validate tracing spans are exported to OpenTelemetry collector.
