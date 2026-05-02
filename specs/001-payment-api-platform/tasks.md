# Tasks: Payment API Platform

**Input**: Design documents from `/specs/001-payment-api-platform/`  
**Branch**: `001-add-payment-api-platform`  
**Prerequisites**: plan.md, spec.md, data-model.md, quickstart.md, contracts/api-contracts.md

**Organization**: Tasks are organized by phase and user story to enable independent implementation and parallel execution. Each task is small, focused, and produces a verifiable outcome.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Parallelizable task (different files, no blocking dependencies)
- **[Story]**: User story label (US1, US2, US3)
- File paths are exact and implementation-ready

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize project structure, Gradle build, and foundational tooling

**Estimated Duration**: 2-3 hours  
**Checkpoint**: Gradle build runs, all dependencies resolved, project structure validated

- [ ] T001 Create Gradle multi-project root with settings.gradle in repository root
- [ ] T002 [P] Create `services/` directory structure: `api-gateway/`, `payment-api/`, `payment-processor/`, `beneficiary-service/`, `account-validation-service/`, `notification-service/`
- [ ] T003 [P] Create `infrastructure/` directories: `kubernetes/base/`, `kubernetes/overlays/`, `kafka/`, `mongodb/`, `certs/`
- [ ] T004 [P] Create shared `build-common.gradle` with Spring Boot 3.x, Java 17, and dependency management
- [ ] T005 Create root `build.gradle` with multi-project task definitions and test aggregation
- [ ] T006 [P] Add Gradle dependency versions in `gradle/libs.versions.toml` for Spring Boot, Spring Cloud, Kafka, MongoDB, Resilience4j, Micrometer, OpenTelemetry
- [ ] T007 Configure `.gitignore` and `.dockerignore` for Java/Gradle/Docker artifacts
- [ ] T008 [P] Create `Dockerfile.base` template for Spring Boot containerization across services

**Dependencies**: None  
**Parallel Opportunities**: T002-T008 (all directory and configuration tasks)

---

## Phase 2: Foundational Infrastructure

**Purpose**: Core platform services and security that ALL user stories depend on

**Estimated Duration**: 8-10 hours  
**Checkpoint**: API Gateway running, MongoDB transaction support verified, Kafka topics created, OAuth2/OIDC configured, base Spring Boot app runs locally

### 2A: Shared Libraries & Common Components

- [ ] T009 Create `shared/` module with common domain exceptions, DTOs, and response wrappers in `shared/src/main/java/com/payments/shared/`
- [ ] T010 [P] Implement base request/response models for Payment, Beneficiary, Account in `shared/src/main/java/com/payments/shared/model/`
- [ ] T011 [P] Create OpenAPI schema validators in `shared/src/main/java/com/payments/shared/validation/`
- [ ] T012 [P] Implement idempotency response cache interface in `shared/src/main/java/com/payments/shared/idempotency/`
- [ ] T013 Implement correlation ID propagation utility in `shared/src/main/java/com/payments/shared/context/`

### 2B: MongoDB Configuration & Persistence

- [ ] T014 Create MongoDB Spring Data configuration in `services/payment-api/src/main/java/com/payments/config/MongoConfig.java` with replica set, transactions, and majority write concern
- [ ] T015 [P] Create Spring Data repositories base interface in `services/payment-api/src/main/java/com/payments/repository/BaseRepository.java`
- [ ] T016 [P] Implement Payment repository with `findByIdempotencyKey`, `findByPaymentId`, `updateStatus` in `services/payment-api/src/main/java/com/payments/repository/PaymentRepository.java`
- [ ] T017 [P] Implement IdempotencyKey repository in `services/payment-api/src/main/java/com/payments/repository/IdempotencyKeyRepository.java`
- [ ] T018 [P] Implement Outbox repository in `services/payment-api/src/main/java/com/payments/repository/OutboxRepository.java`
- [ ] T019 Create MongoDB Testcontainers setup in `services/payment-api/src/test/java/com/payments/testutil/MongoTestcontainer.java`

### 2C: Kafka Configuration & Schema Registry

- [ ] T020 Create Kafka Spring configuration in `services/payment-api/src/main/java/com/payments/config/KafkaProducerConfig.java` with Avro serialization and schema registry
- [ ] T021 [P] Configure schema registry client in `services/payment-api/src/main/java/com/payments/config/SchemaRegistryConfig.java` (Apicurio)
- [ ] T022 [P] Create Kafka consumer configuration in `services/payment-processor/src/main/java/com/payments/config/KafkaConsumerConfig.java`
- [ ] T023 [P] Define Kafka topics in `infrastructure/kafka/topics.yaml` (payments.initiated.v1, payments.status.v1, payments.audit.v1, payments.notifications.v1)
- [ ] T024 Create topic provisioning script in `infrastructure/kafka/create-topics.sh`
- [ ] T025 Create embedded Kafka test configuration in `services/payment-api/src/test/java/com/payments/testutil/EmbeddedKafkaTest.java`

### 2D: Security & OAuth2/OIDC

- [ ] T026 Create Spring Security OAuth2/OIDC configuration in `services/api-gateway/src/main/java/com/payments/security/OAuth2Config.java` with authorization server setup
- [ ] T027 [P] Implement JWT token validator in `services/api-gateway/src/main/java/com/payments/security/JwtTokenValidator.java`
- [ ] T028 [P] Create consent scope mapper in `services/api-gateway/src/main/java/com/payments/security/ConsentScopeMapper.java` mapping token scopes to payment operations
- [ ] T029 [P] Implement mTLS certificate loading in `services/api-gateway/src/main/java/com/payments/security/MtlsConfiguration.java`
- [ ] T030 Create Spring Security filter chain in `services/api-gateway/src/main/java/com/payments/security/SecurityFilterChain.java` for token validation and mTLS enforcement

### 2E: API Gateway Setup

- [ ] T031 Create Spring Cloud Gateway configuration in `services/api-gateway/src/main/java/com/payments/gateway/GatewayConfig.java` with route definitions for backend services
- [ ] T032 [P] Implement request/response logging filter in `services/api-gateway/src/main/java/com/payments/gateway/LoggingFilter.java`
- [ ] T033 [P] Implement rate limiting filter in `services/api-gateway/src/main/java/com/payments/gateway/RateLimitingFilter.java` with per-client throttling
- [ ] T034 [P] Implement header normalization filter in `services/api-gateway/src/main/java/com/payments/gateway/HeaderNormalizationFilter.java` (Idempotency-Key, correlation ID)
- [ ] T035 Implement request signing validator in `services/api-gateway/src/main/java/com/payments/gateway/RequestSigningValidator.java` for JWS validation
- [ ] T036 Create API gateway application class in `services/api-gateway/src/main/java/com/payments/PaymentApiGatewayApplication.java` with Spring Boot main
- [ ] T037 Create `services/api-gateway/build.gradle` with Spring Cloud Gateway and Spring Security dependencies
- [ ] T038 Create `services/api-gateway/application.yml` with gateway routes, OAuth2 config, mTLS settings

### 2F: Error Handling & Resilience Framework

- [ ] T039 [P] Create global exception handler in `services/payment-api/src/main/java/com/payments/exception/GlobalExceptionHandler.java` returning standardized error responses
- [ ] T040 [P] Implement Resilience4j circuit breaker factory in `services/payment-api/src/main/java/com/payments/resilience/CircuitBreakerFactory.java`
- [ ] T041 [P] Create retry configuration in `services/payment-api/src/main/java/com/payments/resilience/RetryConfig.java` with exponential backoff
- [ ] T042 [P] Create bulkhead configuration in `services/payment-api/src/main/java/com/payments/resilience/BulkheadConfig.java` for thread pool isolation

### 2G: Observability

- [ ] T043 Configure Micrometer in `services/payment-api/src/main/java/com/payments/observability/MicrometerConfig.java` with Prometheus metrics
- [ ] T044 [P] Create custom metrics in `services/payment-api/src/main/java/com/payments/observability/PaymentMetrics.java` (TPS, latency, error rates)
- [ ] T045 [P] Configure OpenTelemetry tracing in `services/payment-api/src/main/java/com/payments/observability/OpenTelemetryConfig.java`
- [ ] T046 [P] Implement structured logging with correlation IDs in `services/payment-api/src/main/java/com/payments/logging/CorrelationIdLogger.java`
- [ ] T047 Create `/actuator/health` and `/actuator/metrics` endpoints in base Spring Boot app

### 2H: Testing Infrastructure

- [ ] T048 [P] Create JUnit 5 base test class in `services/payment-api/src/test/java/com/payments/BaseIT.java` with Spring Boot Test setup
- [ ] T049 [P] Create Pact contract test base in `services/payment-api/src/test/java/com/payments/contract/ContractTestBase.java`
- [ ] T050 Create test fixtures in `services/payment-api/src/test/java/com/payments/fixtures/PaymentFixtures.java` for test data generation

**Dependencies**: All Phase 2 tasks complete before moving to user stories  
**Parallel Opportunities**: T002-T008, T010-T013, T015-T018, T020-T025, T027-T035, T039-T050

---

## Phase 3: User Story 1 - Initiate Payment (Priority: P1) 🎯 MVP

**Goal**: Enable payment initiation via REST API with idempotency, consent enforcement, and transactional persistence

**Independent Test**: Create a payment and verify it appears with "Pending" status and can be queried by ID

**Estimated Duration**: 6-8 hours

### 3A: Domain Models for Payment Initiation

- [ ] T051 [P] Create Payment entity in `services/payment-api/src/main/java/com/payments/entity/Payment.java` with fields: paymentId, debtorAccountId, creditorAccountId, amount, currency, status, consentId, idempotencyKey, initiatedAt, updatedAt
- [ ] T052 [P] Create IdempotencyKey entity in `services/payment-api/src/main/java/com/payments/entity/IdempotencyKey.java` with upsert semantics and response cache
- [ ] T053 [P] Create Outbox entity in `services/payment-api/src/main/java/com/payments/entity/Outbox.java` for transactional event publishing
- [ ] T054 [P] Create PaymentInitiationRequest DTO in `services/payment-api/src/main/java/com/payments/dto/PaymentInitiationRequest.java`
- [ ] T055 [P] Create PaymentInitiationResponse DTO in `services/payment-api/src/main/java/com/payments/dto/PaymentInitiationResponse.java`

### 3B: Payment Initiation Service Logic

- [ ] T056 Create PaymentInitiationService in `services/payment-api/src/main/java/com/payments/service/PaymentInitiationService.java` with methods: initiatePayment, initiateBulkPayments
- [ ] T057 [P] Implement idempotency check in PaymentInitiationService: `checkIdempotency(String idempotencyKey)` returning cached response or null
- [ ] T058 Implement MongoDB transaction wrapper in PaymentInitiationService for atomic payment + idempotency + outbox insert
- [ ] T059 Implement payment state validation in PaymentInitiationService (status progression, required fields)
- [ ] T060 Create BulkPaymentProcessor in `services/payment-api/src/main/java/com/payments/service/BulkPaymentProcessor.java` to normalize and persist multiple payments

### 3C: Payment Initiation API Endpoints

- [ ] T061 Create PaymentController in `services/payment-api/src/main/java/com/payments/controller/PaymentController.java` with base request/response mapping
- [ ] T062 Implement `POST /v1/payments` endpoint in PaymentController with idempotency validation, consent check, and transactional call to PaymentInitiationService
- [ ] T063 [P] Implement `POST /v1/payments/bulk` endpoint in PaymentController for bulk payment submission
- [ ] T064 [P] Add request validation annotations on PaymentInitiationRequest (amount > 0, currency ISO-4217, account IDs required)
- [ ] T065 [P] Add error response handling in PaymentController for insufficient funds, invalid account, consent failure, idempotency conflicts

### 3D: Outbox Event Publishing

- [ ] T066 Create OutboxPublisher in `services/payment-api/src/main/java/com/payments/event/OutboxPublisher.java` with Kafka producer
- [ ] T067 Create PaymentInitiatedEvent POJO in `services/payment-api/src/main/java/com/payments/event/PaymentInitiatedEvent.java` with schema fields
- [ ] T068 Implement Kafka producer in OutboxPublisher to publish PaymentInitiatedEvent to `payments.initiated.v1` topic
- [ ] T069 Implement outbox dispatcher scheduled task in `services/payment-api/src/main/java/com/payments/task/OutboxDispatcher.java` to publish pending outbox entries

### 3E: Account Validation Service Setup

- [ ] T070 Create AccountValidationService stub in `services/account-validation-service/src/main/java/com/payments/service/AccountValidationService.java` with `validateAccount(String iban, String currency)` method
- [ ] T071 Create `services/account-validation-service/build.gradle` with Spring Boot dependencies
- [ ] T072 Create AccountValidationController in `services/account-validation-service/src/main/java/com/payments/controller/AccountValidationController.java` with `POST /v1/accounts/validate` endpoint
- [ ] T073 Implement account validation caching in AccountValidationService with TTL (4 hours)

### 3F: Integration Tests for User Story 1

- [ ] T074 [P] Create integration test PaymentInitiationIT in `services/payment-api/src/test/java/com/payments/integration/PaymentInitiationIT.java`
- [ ] T075 [P] Add test case: initiate single payment and verify Pending status
- [ ] T076 [P] Add test case: initiate duplicate payment with same Idempotency-Key and verify cached response
- [ ] T077 [P] Add test case: initiate bulk payment and verify all payments created individually
- [ ] T078 [P] Add test case: reject payment with missing required fields

### 3G: Contract Tests for Payment Endpoints

- [ ] T079 [P] Create Pact contract test for `POST /v1/payments` in `services/payment-api/src/test/java/com/payments/contract/PaymentInitiationContractTest.java`
- [ ] T080 [P] Create Pact contract test for `POST /v1/payments/bulk`

**Dependencies**: Phase 2 (foundational) must be complete  
**Parallel Opportunities**: T051-T055, T062-T065, T074-T080  
**Acceptance Criteria**:
- Payment is persisted with Pending status
- Idempotency enforced (duplicate requests return same response)
- Outbox event created and Kafka topic published
- Account validation called and cached
- All tests pass locally

**Checkpoint**: User Story 1 fully functional and independently testable. Can demo single/bulk payment submission.

---

## Phase 4: User Story 2 - Track Payment Status (Priority: P2)

**Goal**: Enable clients to query payment status and track transaction lifecycle

**Independent Test**: Create a payment, then query its status and verify updates over time

**Estimated Duration**: 4-6 hours

### 4A: Domain Models for Status Tracking

- [ ] T081 [P] Create PaymentStatusResponse DTO in `services/payment-api/src/main/java/com/payments/dto/PaymentStatusResponse.java` with status, reason, links
- [ ] T082 [P] Create PaymentListResponse DTO in `services/payment-api/src/main/java/com/payments/dto/PaymentListResponse.java` for paginated results

### 4B: Payment Status Query Service

- [ ] T083 Create PaymentStatusService in `services/payment-api/src/main/java/com/payments/service/PaymentStatusService.java` with methods: `getPaymentStatus(String paymentId)`, `listPayments(PaymentFilter filter)`
- [ ] T084 [P] Implement pagination in PaymentStatusService: `listPayments(String consentId, PaymentStatus status, LocalDateTime fromDate, int page, int size)`
- [ ] T085 [P] Implement caching in PaymentStatusService for frequently queried payments (5-min TTL)

### 4C: Payment Status API Endpoints

- [ ] T086 Add `GET /v1/payments/{paymentId}` endpoint to PaymentController using PaymentStatusService
- [ ] T087 Add `GET /v1/payments` endpoint to PaymentController for filtered list queries with query parameters (status, dateFrom, dateTo, consentId)
- [ ] T088 [P] Add request validation for query parameters (valid status enum, date range validation)
- [ ] T089 [P] Add HATEOAS links to responses (payment detail, related payments by same debtor)

### 4D: Status Event Consumer (Payment Processor)

- [ ] T090 Create PaymentProcessor service in `services/payment-processor/src/main/java/com/payments/service/PaymentProcessor.java` as Kafka consumer for `payments.initiated.v1`
- [ ] T091 Implement PaymentProcessorListener in `services/payment-processor/src/main/java/com/payments/listener/PaymentProcessorListener.java` consuming PaymentInitiated events
- [ ] T092 Implement status transition logic in PaymentProcessor: Pending → Processing → Completed/Failed
- [ ] T093 [P] Create PaymentStatusChangedEvent POJO in `services/payment-processor/src/main/java/com/payments/event/PaymentStatusChangedEvent.java`
- [ ] T094 Implement Kafka producer in PaymentProcessor to publish PaymentStatusChanged to `payments.status.v1`

### 4E: External Integration Adapters (Stubs)

- [ ] T095 [P] Create CoreBankingAdapter stub in `services/payment-processor/src/main/java/com/payments/adapter/CoreBankingAdapter.java` with `executeTransfer()` method
- [ ] T096 [P] Create FraudCheckAdapter stub in `services/payment-processor/src/main/java/com/payments/adapter/FraudCheckAdapter.java` with `checkFraudRisk()` method
- [ ] T097 [P] Create AmlCheckAdapter stub in `services/payment-processor/src/main/java/com/payments/adapter/AmlCheckAdapter.java` with `checkAmlCompliance()` method
- [ ] T098 Implement adapter orchestration in PaymentProcessor with circuit breakers and retries

### 4F: Integration Tests for User Story 2

- [ ] T099 [P] Create PaymentStatusIT in `services/payment-api/src/test/java/com/payments/integration/PaymentStatusIT.java`
- [ ] T100 [P] Add test case: query single payment status and verify fields
- [ ] T101 [P] Add test case: list payments with filters (status, date range)
- [ ] T102 [P] Add test case: verify status transitions as payment processor updates state
- [ ] T103 [P] Add test case: query non-existent payment returns 404

### 4G: Contract Tests for Status Endpoints

- [ ] T104 [P] Create Pact contract test for `GET /v1/payments/{paymentId}` in `services/payment-api/src/test/java/com/payments/contract/PaymentStatusContractTest.java`
- [ ] T105 [P] Create Pact contract test for `GET /v1/payments` list endpoint

**Dependencies**: Phase 2 (foundational) + Phase 3 (User Story 1) complete  
**Parallel Opportunities**: T081-T082, T084-T089, T095-T097, T099-T105  
**Acceptance Criteria**:
- Payment status queries return correct current state
- Status transitions are visible within 1 second of Kafka event consumption
- List endpoint supports filtering and pagination
- All tests pass

**Checkpoint**: User Stories 1 and 2 working together. Clients can initiate payments and track status.

---

## Phase 5: User Story 3 - Manage Beneficiaries (Priority: P3)

**Goal**: Provide CRUD API for beneficiary master data used in payment initiation

**Independent Test**: Create, read, update, delete a beneficiary and verify changes persist

**Estimated Duration**: 4-5 hours

### 5A: Domain Models for Beneficiaries

- [ ] T106 [P] Create Beneficiary entity in `services/beneficiary-service/src/main/java/com/payments/entity/Beneficiary.java` with fields: beneficiaryId, name, account (IBAN, BIC, currency), address
- [ ] T107 [P] Create BeneficiaryRequest DTO in `services/beneficiary-service/src/main/java/com/payments/dto/BeneficiaryRequest.java`
- [ ] T108 [P] Create BeneficiaryResponse DTO in `services/beneficiary-service/src/main/java/com/payments/dto/BeneficiaryResponse.java`

### 5B: Beneficiary Service Logic

- [ ] T109 Create BeneficiaryService in `services/beneficiary-service/src/main/java/com/payments/service/BeneficiaryService.java` with methods: `createBeneficiary`, `getBeneficiary`, `updateBeneficiary`, `deleteBeneficiary`, `listBeneficiaries`
- [ ] T110 [P] Implement IBAN/BIC validation in BeneficiaryService
- [ ] T111 [P] Implement duplicate detection (same IBAN/currency) in BeneficiaryService

### 5C: Beneficiary API Endpoints

- [ ] T112 Create BeneficiaryController in `services/beneficiary-service/src/main/java/com/payments/controller/BeneficiaryController.java`
- [ ] T113 Implement `POST /v1/beneficiaries` endpoint
- [ ] T114 [P] Implement `GET /v1/beneficiaries/{beneficiaryId}` endpoint
- [ ] T115 [P] Implement `PUT /v1/beneficiaries/{beneficiaryId}` endpoint
- [ ] T116 [P] Implement `DELETE /v1/beneficiaries/{beneficiaryId}` endpoint
- [ ] T117 [P] Implement `GET /v1/beneficiaries` endpoint for list with filtering by name/account
- [ ] T118 [P] Add request validation for IBAN format and required fields

### 5D: Beneficiary Service Application Setup

- [ ] T119 Create BeneficiaryServiceApplication in `services/beneficiary-service/src/main/java/com/payments/BeneficiaryServiceApplication.java`
- [ ] T120 Create `services/beneficiary-service/build.gradle` with Spring Boot and MongoDB dependencies
- [ ] T121 Create `services/beneficiary-service/application.yml` with MongoDB and Kafka config

### 5E: Integration Tests for User Story 3

- [ ] T122 [P] Create BeneficiaryServiceIT in `services/beneficiary-service/src/test/java/com/payments/integration/BeneficiaryServiceIT.java`
- [ ] T123 [P] Add test case: create beneficiary and retrieve
- [ ] T124 [P] Add test case: update beneficiary and verify changes
- [ ] T125 [P] Add test case: delete beneficiary
- [ ] T126 [P] Add test case: list beneficiaries with filtering
- [ ] T127 [P] Add test case: reject duplicate IBAN

### 5F: Contract Tests for Beneficiary Endpoints

- [ ] T128 [P] Create Pact contract test for `POST /v1/beneficiaries`
- [ ] T129 [P] Create Pact contract test for `GET /v1/beneficiaries/{beneficiaryId}`
- [ ] T130 [P] Create Pact contract test for `PUT /v1/beneficiaries/{beneficiaryId}`
- [ ] T131 [P] Create Pact contract test for `DELETE /v1/beneficiaries/{beneficiaryId}`

**Dependencies**: Phase 2 (foundational) complete; can be worked in parallel with Phase 4  
**Parallel Opportunities**: T106-T108, T110-T118, T122-T131  
**Acceptance Criteria**:
- All CRUD operations work
- Validation prevents invalid IBAN/BIC
- Duplicate detection works
- All tests pass

**Checkpoint**: User Stories 1, 2, 3 all independently functional. Core MVP complete.

---

## Phase 6: Payment Processor Enhancement & Event Integration

**Goal**: Complete the payment lifecycle orchestration, status transitions, and external adapter integration

**Estimated Duration**: 6-8 hours

### 6A: Payment State Machine

- [ ] T132 Create PaymentStateMachine in `services/payment-processor/src/main/java/com/payments/state/PaymentStateMachine.java` defining valid status transitions
- [ ] T133 [P] Implement conditional state transitions: Pending → Processing (on fraud/AML pass), Processing → Completed (on transfer success)
- [ ] T134 [P] Implement failure paths: Pending/Processing → Failed with status reason

### 6B: External Integration Logic

- [ ] T135 Create PaymentOrchestrator in `services/payment-processor/src/main/java/com/payments/orchestration/PaymentOrchestrator.java` coordinating fraud, AML, core banking
- [ ] T136 Implement fraud check call in PaymentOrchestrator with circuit breaker and retry
- [ ] T137 Implement AML check call in PaymentOrchestrator
- [ ] T138 Implement core banking transfer call in PaymentOrchestrator with idempotency on transfer ID
- [ ] T139 [P] Create compensation logic for failed transfers (rollback, logging)

### 6C: Kafka Consumer Error Handling

- [ ] T140 Create dead-letter topic handler in `services/payment-processor/src/main/java/com/payments/error/DeadLetterHandler.java` for failed business events
- [ ] T141 Implement retry logic with exponential backoff in PaymentProcessorListener
- [ ] T142 Implement poison pill handling for unparseable events

### 6D: Processor Service Application Setup

- [ ] T143 Create PaymentProcessorApplication in `services/payment-processor/src/main/java/com/payments/PaymentProcessorApplication.java`
- [ ] T144 Create `services/payment-processor/build.gradle` with Spring Boot, Kafka, Resilience4j dependencies
- [ ] T145 Create `services/payment-processor/application.yml` with Kafka consumer config and external adapter endpoints

### 6E: Processor Integration Tests

- [ ] T146 [P] Create PaymentProcessorIT in `services/payment-processor/src/test/java/com/payments/integration/PaymentProcessorIT.java`
- [ ] T147 [P] Add test case: consume PaymentInitiated, transition to Processing, publish PaymentStatusChanged
- [ ] T148 [P] Add test case: handle fraud check failure and mark payment Failed
- [ ] T149 [P] Add test case: handle core banking timeout and retry

**Dependencies**: Phases 2-4 complete  
**Parallel Opportunities**: T132-T134, T136-T139, T146-T149

---

## Phase 7: Notification Service & Webhooks

**Goal**: Enable external subscribers to receive payment status updates via webhooks

**Estimated Duration**: 4-5 hours

### 7A: Domain Models for Notifications

- [ ] T150 [P] Create WebhookSubscription entity in `services/notification-service/src/main/java/com/payments/entity/WebhookSubscription.java` with fields: subscriberId, webhookUrl, events, status
- [ ] T151 [P] Create WebhookDeliveryLog entity in `services/notification-service/src/main/java/com/payments/entity/WebhookDeliveryLog.java` for audit trail

### 7B: Webhook Dispatcher

- [ ] T152 Create WebhookDispatcher in `services/notification-service/src/main/java/com/payments/service/WebhookDispatcher.java` consuming `payments.notifications.v1` Kafka topic
- [ ] T153 Implement webhook delivery in WebhookDispatcher with retry (up to 5 attempts, exponential backoff)
- [ ] T154 [P] Implement webhook signature (JWS) in WebhookDispatcher for integrity
- [ ] T155 [P] Implement timeout/circuit breaker for webhook endpoints
- [ ] T156 [P] Implement webhook delivery logging in WebhookDispatcher

### 7C: Webhook Management API

- [ ] T157 Create WebhookController in `services/notification-service/src/main/java/com/payments/controller/WebhookController.java`
- [ ] T158 Implement `POST /v1/webhook-subscriptions` to register webhook
- [ ] T159 [P] Implement `GET /v1/webhook-subscriptions` to list subscriptions
- [ ] T160 [P] Implement `DELETE /v1/webhook-subscriptions/{subscriptionId}` to unsubscribe

### 7D: Notification Service Application Setup

- [ ] T161 Create NotificationServiceApplication in `services/notification-service/src/main/java/com/payments/NotificationServiceApplication.java`
- [ ] T162 Create `services/notification-service/build.gradle`
- [ ] T163 Create `services/notification-service/application.yml` with Kafka consumer config

### 7E: Integration Tests

- [ ] T164 [P] Create WebhookDispatcherIT in `services/notification-service/src/test/java/com/payments/integration/WebhookDispatcherIT.java`
- [ ] T165 [P] Add test case: dispatch webhook on payment status change
- [ ] T166 [P] Add test case: retry failed webhook delivery

**Dependencies**: Phases 2-6 complete  
**Parallel Opportunities**: T150-T151, T154-T155, T158-T166

---

## Phase 8: Observability & Monitoring

**Goal**: Complete metrics, tracing, and centralized logging for production readiness

**Estimated Duration**: 4-6 hours

### 8A: Metrics & Dashboards

- [ ] T167 [P] Add TPS (transactions per second) metric in `shared/src/main/java/com/payments/observability/TransactionMetrics.java`
- [ ] T168 [P] Add latency percentile metrics (P50, P95, P99) in `shared/src/main/java/com/payments/observability/LatencyMetrics.java`
- [ ] T169 [P] Add error rate metrics in `shared/src/main/java/com/payments/observability/ErrorMetrics.java`
- [ ] T170 Create Prometheus scrape config in `infrastructure/observability/prometheus.yml`
- [ ] T171 Create Grafana dashboard template in `infrastructure/observability/grafana-payments-dashboard.json`

### 8B: Distributed Tracing

- [ ] T172 [P] Create OpenTelemetry span processor in `shared/src/main/java/com/payments/observability/TracingConfig.java`
- [ ] T173 [P] Add trace context propagation to Kafka headers in `shared/src/main/java/com/payments/observability/KafkaTracingInterceptor.java`
- [ ] T174 [P] Add trace context propagation to HTTP headers in `shared/src/main/java/com/payments/observability/HttpTracingFilter.java`
- [ ] T175 Create Jaeger configuration in `infrastructure/observability/jaeger-config.yml`

### 8C: Centralized Logging

- [ ] T176 Create Logback configuration in `shared/src/main/resources/logback-spring.xml` with JSON output for structured logging
- [ ] T177 [P] Add MDC (Mapped Diagnostic Context) setup in `shared/src/main/java/com/payments/logging/MdcLoggingFilter.java` for correlation ID
- [ ] T178 Create ELK stack setup in `infrastructure/observability/docker-compose-elk.yml` (Elasticsearch, Logstash, Kibana)

### 8D: Health & Readiness Checks

- [ ] T179 [P] Create liveness probe endpoint in `shared/src/main/java/com/payments/health/LivenessProbe.java`
- [ ] T180 [P] Create readiness probe endpoint in `shared/src/main/java/com/payments/health/ReadinessProbe.java` checking MongoDB, Kafka connectivity

**Dependencies**: Phases 2-7 complete  
**Parallel Opportunities**: T167-T169, T172-T174, T176-T180

---

## Phase 9: Deployment & Infrastructure

**Goal**: Create production-ready Kubernetes manifests and infrastructure code

**Estimated Duration**: 6-8 hours

### 9A: Kubernetes Base Manifests

- [ ] T181 Create api-gateway Deployment manifest in `infrastructure/kubernetes/base/api-gateway-deployment.yaml` with resource limits, probes, envvars
- [ ] T182 [P] Create payment-api Deployment manifest in `infrastructure/kubernetes/base/payment-api-deployment.yaml`
- [ ] T183 [P] Create payment-processor Deployment manifest in `infrastructure/kubernetes/base/payment-processor-deployment.yaml`
- [ ] T184 [P] Create beneficiary-service Deployment manifest in `infrastructure/kubernetes/base/beneficiary-service-deployment.yaml`
- [ ] T185 [P] Create account-validation-service Deployment manifest in `infrastructure/kubernetes/base/account-validation-service-deployment.yaml`
- [ ] T186 [P] Create notification-service Deployment manifest in `infrastructure/kubernetes/base/notification-service-deployment.yaml`

### 9B: Kubernetes Services & Ingress

- [ ] T187 Create Service manifests for all services in `infrastructure/kubernetes/base/services.yaml`
- [ ] T188 Create Ingress manifest in `infrastructure/kubernetes/base/ingress.yaml` with mTLS and rate limiting annotations
- [ ] T189 Create ConfigMap for shared configuration in `infrastructure/kubernetes/base/configmap.yaml`
- [ ] T190 Create Secrets manifest (placeholder) in `infrastructure/kubernetes/base/secrets.yaml` with MongoDB credentials, OAuth2 client secrets

### 9C: Horizontal Pod Autoscaler & Resource Quotas

- [ ] T191 Create HPA for payment-api in `infrastructure/kubernetes/base/hpa-payment-api.yaml` with CPU/memory thresholds and custom metrics (Kafka lag)
- [ ] T192 [P] Create HPA for payment-processor in `infrastructure/kubernetes/base/hpa-payment-processor.yaml`
- [ ] T193 [P] Create resource quotas in `infrastructure/kubernetes/base/resource-quota.yaml`

### 9D: MongoDB Deployment

- [ ] T194 Create MongoDB Helm values in `infrastructure/mongodb/values.yaml` with sharding, replica sets, majority write concern
- [ ] T195 Create MongoDB backup script in `infrastructure/mongodb/backup.sh`
- [ ] T196 Create MongoDB restore script in `infrastructure/mongodb/restore.sh`

### 9E: Kafka Deployment

- [ ] T197 Create Kafka Helm values in `infrastructure/kafka/values.yaml` with broker replication factor 3, topic auto-creation
- [ ] T198 Create topic provisioning script in `infrastructure/kafka/topics-provision.sh`
- [ ] T199 Create schema registry setup script in `infrastructure/kafka/schema-registry-setup.sh`

### 9F: Certificate & mTLS Setup

- [ ] T200 Create certificate generation script in `infrastructure/certs/generate-certs.sh` for CA, server, client certs
- [ ] T201 Create Kubernetes Secret creation script in `infrastructure/certs/create-tls-secrets.sh`

### 9G: Deployment Overlays

- [ ] T202 Create dev overlay in `infrastructure/kubernetes/overlays/dev/` with dev-specific resource limits and replicas
- [ ] T203 [P] Create staging overlay in `infrastructure/kubernetes/overlays/staging/`
- [ ] T204 [P] Create production overlay in `infrastructure/kubernetes/overlays/prod/` with strict resource limits and high replicas

**Dependencies**: Phases 2-8 complete  
**Parallel Opportunities**: T182-T186, T192-T193, T202-T204

---

## Phase 10: Performance & Load Testing

**Goal**: Validate high-throughput and latency requirements before production

**Estimated Duration**: 4-6 hours

### 10A: Load Testing Scripts

- [ ] T205 Create Locust load test script in `tests/load/locust_payment_api.py` simulating 1000+ concurrent payment initiation requests
- [ ] T206 [P] Create JMeter test plan in `tests/load/payment-api-jmeter.jmx` with payment initiation, status query, beneficiary CRUD
- [ ] T207 [P] Create load test execution script in `tests/load/run-load-tests.sh`

### 10B: Performance Monitoring During Load

- [ ] T208 Create Prometheus queries in `tests/load/prometheus-queries.yaml` for TPS, latency percentiles, error rate during load test
- [ ] T209 Create load test report generator in `tests/load/generate-report.py` outputting TPS, latency, errors

### 10C: Chaos Testing

- [ ] T210 Create chaos test script in `tests/chaos/chaos-tests.sh` simulating MongoDB failover, Kafka broker failure, network delays
- [ ] T211 [P] Create chaos monkey configuration in `infrastructure/chaos/chaos-config.yaml`

**Dependencies**: Phases 1-9 complete  
**Parallel Opportunities**: T206-T207, T210-T211

---

## Phase 11: Documentation & Runbooks

**Goal**: Create operational and developer documentation

**Estimated Duration**: 3-4 hours

### 11A: Developer Guide

- [ ] T212 Create developer setup guide in `docs/DEVELOPER_SETUP.md` with local build, run, debug instructions
- [ ] T213 Create API reference documentation in `docs/API_REFERENCE.md` with endpoint descriptions and examples
- [ ] T214 Create architecture diagram in Mermaid in `docs/ARCHITECTURE.md`

### 11B: Operational Runbooks

- [ ] T215 Create incident response runbook in `docs/INCIDENT_RESPONSE.md` for common failures (high error rate, high latency, Kafka lag)
- [ ] T216 Create scaling runbook in `docs/SCALING.md` for horizontal scaling decisions
- [ ] T217 Create backup/recovery runbook in `docs/BACKUP_RECOVERY.md` for MongoDB and Kafka

### 11C: Security & Compliance

- [ ] T218 Create security guide in `docs/SECURITY.md` with mTLS, OAuth2, consent enforcement details
- [ ] T219 Create PCI-DSS compliance checklist in `docs/COMPLIANCE_CHECKLIST.md`

**Dependencies**: Phases 1-11 complete

---

## Phase 12: Polish & Final Validation

**Goal**: Code cleanup, performance optimization, and end-to-end validation

**Estimated Duration**: 4-6 hours

### 12A: Code Quality

- [ ] T220 [P] Run SonarQube analysis across all services in `scripts/sonarqube-scan.sh`
- [ ] T221 [P] Run dependency vulnerability scan in `scripts/dependency-check.sh`
- [ ] T222 [P] Format code with Gradle spotless plugin in all services
- [ ] T223 [P] Run linting across all services

### 12B: Performance Optimization

- [ ] T224 Optimize Kafka batch sizes and buffer settings in `services/payment-processor/application.yml` for throughput
- [ ] T225 [P] Optimize MongoDB indexes for payment query patterns in `services/payment-api/src/main/java/com/payments/config/MongoIndexConfig.java`
- [ ] T226 [P] Enable connection pooling tuning in Spring Data MongoDB config

### 12C: End-to-End Validation

- [ ] T227 Execute quickstart.md commands in `specs/001-payment-api-platform/quickstart.md` to verify local build and run
- [ ] T228 Run full integration test suite in `scripts/run-integration-tests.sh`
- [ ] T229 Run full contract test suite in `scripts/run-contract-tests.sh`
- [ ] T230 Validate all API contracts match OpenAPI spec in `specs/001-payment-api-platform/contracts/api-contracts.md`

### 12D: Documentation Review

- [ ] T231 [P] Review and update all markdown files for completeness
- [ ] T232 [P] Update README.md with project overview, quick start, and contribution guidelines
- [ ] T233 Verify all links in documentation are valid

**Dependencies**: Phases 1-11 complete  
**Parallel Opportunities**: T220-T226, T231-T232

---

## Dependencies & Execution Order

### Critical Path

1. **Phase 1** (Setup) → **Phase 2** (Foundational) → must complete before any user story
2. **Phase 3** (US1 - Payment Initiation) → can start after Phase 2
3. **Phase 4** (US2 - Status Tracking) → depends on Phase 2 and optionally Phase 3
4. **Phase 5** (US3 - Beneficiaries) → can start after Phase 2 (independent of US1, US2)
5. **Phase 6** (Processor) → should follow Phase 4 to leverage status query capability
6. **Phase 7** (Notifications) → depends on Phase 6 and later user stories
7. **Phase 8** (Observability) → can run parallel to Phases 6-7
8. **Phase 9** (Deployment) → can start after Phase 2 but accelerates with Phase 8
9. **Phase 10** (Load Testing) → depends on Phases 1-9
10. **Phase 11** (Documentation) → can run parallel to all phases but finalizes in Phase 12
11. **Phase 12** (Polish) → final validation step after all phases

### Parallel Opportunities

- **Setup Phase**: All directory/Gradle configuration tasks (T002-T008)
- **Foundational Phase**: Most configuration tasks can run in parallel (T010-T050)
- **User Stories**: US1, US2, US3 can be worked by separate teams after Foundational phase
- **Infrastructure**: Kubernetes manifests, observability, and testing can be parallelized
- **Code Quality**: SonarQube, dependency checks, linting can run on all services simultaneously

---

## Implementation Strategy

### MVP First (Recommended)

1. Complete Phase 1 & 2 (Setup + Foundational)
2. Complete Phase 3 (User Story 1)
3. Stop and validate: Can initiate single/bulk payments with idempotency
4. Demo MVP to stakeholders
5. Continue with remaining phases incrementally

### Incremental Delivery

- **Iteration 1**: Phases 1-3 (payment initiation core)
- **Iteration 2**: Phase 4 (add status tracking)
- **Iteration 3**: Phase 5 (add beneficiary management)
- **Iteration 4**: Phases 6-7 (complete payment processor and notifications)
- **Iteration 5**: Phases 8-9 (production readiness)
- **Iteration 6**: Phases 10-12 (validation and optimization)

### Parallel Team Strategy (6+ developers)

**Team 1 (Infrastructure)**: Phases 1-2  
**Team 2 (Payment API)**: Phase 3 after Phase 2 complete  
**Team 3 (Payment Processor)**: Phase 6 starting when Phase 2 complete  
**Team 4 (Beneficiary Service)**: Phase 5 after Phase 2 complete  
**Team 5 (Notifications)**: Phase 7 after Phase 6 complete  
**Team 6 (DevOps/QA)**: Phases 8-10 in parallel with implementation

---

## Success Checkpoints

| Checkpoint | Phases | Validation |
|-----------|--------|-----------|
| Foundation Ready | 1-2 | All Spring Boot services start, MongoDB connects, Kafka topics created, OAuth2 configured |
| MVP Complete | 1-3 | Payment initiation works end-to-end with idempotency, outbox events published |
| Status Tracking | 1-4 | Payment status queries work, processor consumes and updates state |
| Full Core System | 1-5 | All 3 user stories functional, beneficiary management works |
| Event-Driven | 1-6 | Payment processor fully operational, state transitions working |
| External Notifications | 1-7 | Webhooks dispatching to external subscribers |
| Production Ready | 1-9 | Observability complete, Kubernetes manifests ready |
| Load Validated | 1-10 | 1M TPS, P95 < 300ms verified under load |
| Documented | 1-12 | All docs complete, runbooks tested |

---

## Task Statistics

- **Total Tasks**: 233
- **Setup Phase**: 8 tasks
- **Foundational Phase**: 42 tasks
- **User Story 1**: 29 tasks
- **User Story 2**: 25 tasks
- **User Story 3**: 27 tasks
- **Processor Enhancement**: 14 tasks
- **Notification Service**: 16 tasks
- **Observability**: 14 tasks
- **Deployment**: 24 tasks
- **Performance Testing**: 7 tasks
- **Documentation**: 9 tasks
- **Polish & Validation**: 14 tasks

**Parallelizable Tasks [P]**: ~110 (47% of total)  
**Estimated Total Duration**: 60-80 hours with sequential execution; 20-30 hours with optimal parallelization
