# Research: Payment API Platform Design Decisions

## NoSQL Choice

Decision: MongoDB Community Edition with sharding and replica sets.

Rationale:
- Supports strong consistency through `majority` write concern and transactional sessions.
- Provides rich query capability for payment lookup, beneficiary management, and audit history.
- Integrates well with Spring Data MongoDB and can scale horizontally with sharding.

Alternatives considered:
- Apache Cassandra: excellent throughput, but eventual consistency by default and more complex strong consistency paths.
- ScyllaDB: high-performance but less mature for transactional payment workflows in this environment.

## Transaction Integrity Strategy

Decision: Transactional Outbox + event-driven saga.

Rationale:
- Avoids distributed two-phase commit across Kafka and MongoDB.
- Keeps payment state authoritative in MongoDB while guaranteeing event publication through an outbox collection.
- Supports strong consistency for payment initiation and status transitions.

Alternatives considered:
- Distributed transaction coordinator: too brittle and not aligned with Kafka/MongoDB.
- Pure event sourcing: adds unnecessary complexity for initial payment lifecycle state management.

## Kafka Schema Management

Decision: Open-source schema registry (Apicurio or Confluent OSS) with Avro/JSON Schemas.

Rationale:
- Enforces schema compatibility for producers and consumers.
- Supports backward-compatible versioning as the API evolves.
- Enables strong contract management for external and internal event consumers.

## API Gateway Design

Decision: Use Spring Cloud Gateway or Envoy for ingress.

Rationale:
- Centralizes mTLS enforcement and OpenID Connect token validation.
- Provides a single point for rate limiting, header normalization, and request signing validation.
- Keeps backend services focused on business logic.

## Consistency and Performance

Decision: Strong consistency on the write path, eventual consistency on derived read models.

Rationale:
- Financial transactions require strong state correctness for payment status and idempotency.
- MongoDB transactions ensure atomic updates for payment persistence, idempotency records, and outbox writes.
- Derived views may be asynchronously updated to maintain high-read performance without compromising transactional integrity.

## Observability

Decision: Micrometer + OpenTelemetry, centralized logs with correlation IDs.

Rationale:
- Enables performance monitoring for TPS, latency, failure rates.
- Supports distributed tracing across Kafka and HTTP boundaries.
- Centralized logs are required for banking-grade incident investigation.
