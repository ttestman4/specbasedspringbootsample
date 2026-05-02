# Implementation Plan: Payment API Platform

**Branch**: `001-add-payment-api-platform` | **Date**: 2026-05-02 | **Spec**: [specs/001-payment-api-platform/spec.md](specs/001-payment-api-platform/spec.md)
**Input**: Feature specification from `/specs/001-payment-api-platform/spec.md`

## Summary

Build a banking-grade Payments API platform using Spring Boot, Gradle, Apache Kafka, and MongoDB Community Edition. The platform will expose secure REST `/v1` endpoints to internal systems and external financial applications, provide consent-aware access management, enforce idempotency, and deliver payment lifecycle state changes through a Kafka-based event mesh.

## Technical Context

**Language/Version**: Java 17+  
**Primary Dependencies**: Spring Boot 3.x, Spring WebFlux, Spring Cloud Gateway, Spring Kafka, Spring Data MongoDB, Resilience4j, Micrometer, OpenTelemetry, Spring Security OAuth2/OIDC, JJWT/JOSE  
**Storage**: MongoDB sharded replica set with majority write concern and transactional sessions; Kafka cluster with Avro/JSON schema registry  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers, embedded Kafka, contract tests for API and Kafka event schemas  
**Target Platform**: Linux containers, Kubernetes 1.28+  
**Project Type**: Microservices payments platform  
**Performance Goals**: 1,000,000+ transactions per hour, P95 < 300 ms, 99.99% availability  
**Constraints**: Must use Spring Boot, Kafka, Gradle, and open-source NoSQL; preserve financial transaction consistency; support mTLS, OAuth2/OIDC, JWS, schema-managed Kafka events, and idempotent service operations  
**Scale/Scope**: External TPPs and internal systems, payment initiation (single/bulk), payment status tracking, account validation, webhook notifications, event-driven payment lifecycle, and regulatory auditability  

## Constitution Check

- Service design is stateless at runtime; durable state is isolated in MongoDB and Kafka.
- Architecture supports Kubernetes-native auto-scaling and operational telemetry.
- Strong consistency is explicitly addressed with MongoDB transactions, majority write concern, and an outbox pattern.
- The plan aligns with constitution principles for scalability, resilience, security, and observability.

## Project Structure

### Documentation (this feature)

```text
specs/001-payment-api-platform/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── api-contracts.md
└── tasks.md
```

### Proposed Source Layout (repository root)

```text
services/
├── api-gateway/
├── payment-api/
├── payment-processor/
├── beneficiary-service/
├── account-validation-service/
└── notification-service/

infrastructure/
├── kubernetes/
│   ├── base/
│   └── overlays/
├── kafka/
├── mongodb/
└── certs/
```

**Structure Decision**: Separate services ensure bounded contexts for payment handling, beneficiary management, account validation, and notifications. All services are Spring Boot modules with Kubernetes-ready deployment artifacts.

## Service Decomposition

- `api-gateway`: ingress gateway for mTLS, OAuth2/OIDC token validation, route enforcement, rate limiting, and request signing verification.
- `payment-api`: external REST interface for payment initiation, bulk payment submission, payment status queries, and account validation calls.
- `payment-processor`: Kafka-driven service responsible for payment lifecycle transitions, fraud/AML/core banking orchestration, and status event production.
- `beneficiary-service`: CRUD and validation for payment beneficiaries with Open Banking-aligned schema mapping.
- `account-validation-service`: synchronous account verification service with caching and core banking integration.
- `notification-service`: webhook dispatcher that consumes payment status events and delivers notifications to external subscribers.

## API Layer Design

- All external APIs use JSON and are versioned under `/v1/`.
- `api-gateway` enforces security, idempotency headers, request/response signing, and metrics capture.
- Internal APIs use OAuth2 client credentials and mTLS.
- `payment-api` implements request validation, consent checks, idempotency record creation, and transactional persistence.
- `payment-processor` handles workflow state changes asynchronously, preserving eventual event ordering per payment.

### Primary External Endpoints

- `POST /v1/payments` – create a single payment request.  
- `POST /v1/payments/bulk` – create multiple payment requests in one operation.  
- `GET /v1/payments/{paymentId}` – retrieve payment status and details.  
- `GET /v1/payments` – search payments by status, date range, or consent ID.  
- `POST /v1/accounts/validate` – validate account details before payment initiation.  
- `POST /v1/beneficiaries` – add beneficiary record.  
- `GET /v1/beneficiaries/{beneficiaryId}` – retrieve beneficiary.  
- `PUT /v1/beneficiaries/{beneficiaryId}` – update beneficiary.  
- `DELETE /v1/beneficiaries/{beneficiaryId}` – delete beneficiary.

### Idempotency and Request Semantics

- `Idempotency-Key` is required for all payment write operations.
- `payment-api` writes the request and idempotency metadata in a MongoDB transaction.
- Duplicate requests return the stored response and status without reprocessing.
- Bulk requests are normalized into individual payment records for downstream processing.

### Security Model

- External TPPs use OAuth2 Authorization Code Flow with explicit consent scopes.
- Internal systems use OAuth2 Client Credentials Flow.
- Scopes: `payments:write`, `payments:read`, `beneficiaries:write`, `beneficiaries:read`, `accounts:validate`.
- mTLS is enforced at the gateway and for internal service-to-service calls.
- Request/response integrity is supported through JWS signatures where required.
- Tokens include consent metadata and transaction-level audit context.

## Kafka Design

### Topics

- `payments.initiated.v1` – payment initiation events.  
- `payments.status.v1` – status transition events.  
- `payments.notifications.v1` – notification and webhook events.  
- `payments.audit.v1` – audit and reconciliation events.  
- `account.validations.v1` – account validation results.

### Partitioning

- Use 24 partitions initially; scale partitions by load, with a minimum replication factor of 3.
- Use `paymentId` as a routing key to preserve per-payment ordering.
- Use `recipientId` or `subscriberId` for notification topic affinity.

### Schema Management

- Use Apicurio or Confluent OSS schema registry with Avro/JSON schema enforcement.
- Events include mandatory fields: `eventType`, `eventId`, `paymentId`, `occurredAt`, `correlationId`, `version`.
- Enforce backward-compatible producer schema evolution.

### Event Flow

1. `payment-api` writes the payment request, idempotency record, and outbox event in one MongoDB transaction.
2. `outbox-dispatcher` publishes `PaymentInitiated` to `payments.initiated.v1`.
3. `payment-processor` consumes `PaymentInitiated`, runs fraud/AML/core banking adapters, updates payment status, and emits `PaymentStatusChanged`.
4. `notification-service` consumes `PaymentStatusChanged` and dispatches webhook notifications.
5. `payments.audit.v1` captures immutable audit records for regulatory reporting.

### Transaction Integrity Strategy

- Adopt a Transactional Outbox pattern to avoid distributed two-phase commit.
- Persist payment state and outbox events together in MongoDB transactions.
- Use event-driven saga semantics for cross-system orchestration and recovery.
- For payment state transitions, use conditional updates and state machines to prevent stale or duplicate transitions.

### Resilience and Backpressure

- Kafka consumers use pause/resume semantics when downstream persistence or external integrations are saturated.
- `payment-processor` and `notification-service` implement retries, circuit breakers, and bulkhead isolation.
- Use dead-letter topics for failed business events after retry exhaustion.

## NoSQL Database Design

### Chosen Data Store

- MongoDB Community Edition with sharding and replica sets.
- Strong consistency is enforced through `majority` write concern and MongoDB transactions for critical write paths.

### Collections

- `payments` – active payment request and current state.  
- `payment_events` – immutable transition history.  
- `beneficiaries` – beneficiary master data.  
- `account_validations` – account verification results.  
- `idempotency_keys` – idempotency mapping and response cache.  
- `outbox` – transactional event publishing queue.

### Partitioning and Indexing

- Shard `payments` by `paymentShardKey` derived from `paymentId` to distribute write load evenly.
- Index `paymentId`, `consentId`, `status`, `updatedAt`, and `debtorAccountId`.
- TTL index on `idempotency_keys.createdAt` and `account_validations.updatedAt`.

### Consistency Strategy

- Use MongoDB transactions for the payment initiation path and payment status transitions.
- Use `findOneAndUpdate` with `precondition` on payment `status` to enforce valid state progression.
- Read and write operations for payment state use `readConcern: majority` and `writeConcern: majority`.
- Derivative read models may be eventually consistent, but the primary `payments` collection remains authoritative.

## Deployment Architecture

- Build using Gradle and produce Docker images for each service.
- Deploy services to Kubernetes with dedicated Deployments and Services.
- Use `HorizontalPodAutoscaler` on `payment-api`, `payment-processor`, and `notification-service`.
- Deploy Kafka as a clustered service with 3+ brokers and replication factor 3.
- Deploy MongoDB as a sharded cluster with 3 config servers and 3 replica set members per shard.
- Use `api-gateway` as the ingress point with mTLS termination and routing to backend services.
- Use ConfigMaps and Secrets for runtime configuration and credentials.

## Performance and Scaling Strategy

- Target steady-state throughput of 278 TPS (1M/hour) and 300 ms P95 by scaling Kafka consumers, MongoDB shards, and API pods.
- Use reactive non-blocking I/O in `payment-api` for high request concurrency.
- Cache consent and account validation results to reduce external load.
- Use batch publish for bulk payments and partition-aware Kafka producers.

## Failure Handling and Resilience Patterns

- Use Resilience4j circuit breakers around external integrations: core banking, fraud/AML, identity, and webhook endpoints.
- Configure retries with exponential backoff and safe retry windows for idempotent operations.
- Send failed business events to dead-letter topics after retry exhaustion.
- Use liveness/readiness probes and graceful termination for Kubernetes pod lifecycle.
- Propagate correlation IDs through logs, Kafka headers, and outgoing webhook payloads.

## Implementation-Ready Tasks

1. Create Gradle multi-project setup for `services/*` modules.
2. Implement `api-gateway` with OAuth2, mTLS, request signing, and rate limiting.
3. Implement `payment-api` with MongoDB transactions, idempotency, consent enforcement, and outbox event writes.
4. Implement `payment-processor` with Kafka consumer, fraud/AML/core banking orchestration, and status event emission.
5. Implement `beneficiary-service` and `account-validation-service` as service-oriented Spring Boot modules.
6. Implement `notification-service` to consume status events and deliver webhooks.
7. Add centralized observability: Micrometer metrics, OpenTelemetry tracing, centralized logs, and health endpoints.
8. Create Kubernetes manifests or Helm charts for core services, Kafka, and MongoDB.

## Next Artifact Targets

- `research.md`: document supporting design decisions for MongoDB, Kafka schema management, and transaction integrity.
- `data-model.md`: define domain entities, collection schemas, and partitioning strategy.
- `quickstart.md`: provide build, local run, and deploy commands.
- `contracts/api-contracts.md`: capture the API contract and schema obligations for both external clients and internal services.
