# API Contracts: Payment API Platform

## External Contract Principles

- All APIs are JSON-based and versioned under `/v1/`.
- Security is enforced using OAuth2 access tokens and mTLS.
- Idempotent writes require `Idempotency-Key`.
- APIs must accept and return Open Banking-aligned fields where applicable.

## Core Endpoints

### POST /v1/payments

Request:
- `Authorization: Bearer <token>`
- `Idempotency-Key`
- `Content-Type: application/json`

Body schema:
- `debtorAccountId` (string)
- `creditorAccountId` (string)
- `amount` (integer)
- `currency` (string)
- `paymentMethod` (string)
- `remittanceInformation` (string)
- `consentId` (string)

Response (201):
- `paymentId` (string)
- `status` (string)
- `createdAt` (date-time)
- `links` (object)

### POST /v1/payments/bulk

Request body: array of payment initiation objects.

Response (201):
- `batchId` (string)
- `payments` (array of `{paymentId, status}`)

### GET /v1/payments/{paymentId}

Response (200):
- `paymentId`
- `status`
- `amount`
- `currency`
- `debtorAccountId`
- `creditorAccountId`
- `statusReason`
- `initiatedAt`
- `updatedAt`

### GET /v1/payments

Query parameters:
- `status` (enum)
- `dateFrom` (ISO 8601)
- `dateTo` (ISO 8601)
- `consentId` (string)
- `page` (int, default 1)
- `pageSize` (int, default 20)

Response (200):
- `payments` (array)
- `pagination` (object)

### POST /v1/accounts/validate

Request body:
- `accountNumber` (string)
- `sortCode` / `routingNumber` (string)
- `currency` (string)

Response (200):
- `accountId` (string)
- `validationStatus` (string)
- `validationDetails` (object)

## Beneficiary API Contract

- `POST /v1/beneficiaries` creates beneficiary (201)
- `GET /v1/beneficiaries/{beneficiaryId}` reads beneficiary (200)
- `PUT /v1/beneficiaries/{beneficiaryId}` updates beneficiary (200)
- `DELETE /v1/beneficiaries/{beneficiaryId}` removes beneficiary (204)
- `GET /v1/beneficiaries` lists beneficiaries with filtering (200)

## Event Contracts

### PaymentInitiated

- `eventType`: `PaymentInitiated`
- `paymentId`
- `amount`
- `currency`
- `debtorAccountId`
- `creditorAccountId`
- `consentId`
- `initiatedAt`
- `correlationId`

### PaymentStatusChanged

- `eventType`: `PaymentStatusChanged`
- `paymentId`
- `oldStatus`
- `newStatus`
- `statusReason`
- `occurredAt`
- `correlationId`

## Schema Obligations

- Events must be registered with schema registry.
- Request/response schemas validated at service boundary.
- All webhook payloads must include `correlationId` and `eventType`.
