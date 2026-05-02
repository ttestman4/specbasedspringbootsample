# Data Model: Payments API Platform

## Primary Entities

### Payment

Represents a payment lifecycle record.

Fields:
- `paymentId` (string, UUID)
- `debtorAccountId` (string)
- `creditorAccountId` (string)
- `amount` (integer, minor units)
- `currency` (string, ISO 4217)
- `status` (string, enum: Pending, Processing, Completed, Failed, Cancelled)
- `statusReason` (string)
- `consentId` (string)
- `idempotencyKey` (string)
- `sourceSystem` (string, enum: TPP, Internal)
- `initiatedAt` (date-time)
- `updatedAt` (date-time)
- `paymentType` (string, enum: Single, Bulk)
- `paymentMethod` (string, e.g., `FPS`, `ACH`, `SWIFT`)

### PaymentEvent

Immutable history entry for payment state changes.

Fields:
- `eventId` (string)
- `paymentId` (string)
- `eventType` (string)
- `sourceSystem` (string)
- `detail` (object)
- `createdAt` (date-time)
- `correlationId` (string)

### Beneficiary

Stores beneficiary master data.

Fields:
- `beneficiaryId` (string)
- `name` (string)
- `account` (object)
- `address` (object)
- `createdAt` (date-time)
- `updatedAt` (date-time)

### AccountValidation

Stores verification results.

Fields:
- `accountId` (string)
- `validationStatus` (string, enum: Valid, Invalid, Unknown)
- `validationSource` (string)
- `validatedAt` (date-time)
- `expiresAt` (date-time)

### IdempotencyKey

Tracks request idempotency.

Fields:
- `idempotencyKey` (string)
- `paymentId` (string)
- `requestHash` (string)
- `responsePayload` (object)
- `createdAt` (date-time)
- `expiresAt` (date-time)

## Collection Design

### payments

- Shard key: `paymentId` or synthetic `paymentShardKey` hashed from `paymentId`.
- Indexes: `paymentId`, `consentId`, `status`, `updatedAt`, `debtorAccountId`.
- Write pattern: transactional insert/update for payment state changes.

### payment_events

- Indexes: `paymentId`, `eventType`, `createdAt`.
- Immutable append-only structure.

### beneficiaries

- Indexes: `beneficiaryId`, `account.accountNumber`, `account.iban`, `name`.
- CRUD access pattern with validation on create/update.

### account_validations

- Indexes: `accountId`, `validationStatus`, `expiresAt`.
- TTL index on `expiresAt` to expire stale validation records.

### idempotency_keys

- Indexes: `idempotencyKey`, `paymentId`.
- TTL index on `expiresAt`.

### outbox

- Indexes: `status`, `createdAt`, `eventType`.
- Status values: `PENDING`, `SENT`, `FAILED`.

## Consistency Strategy

- Use MongoDB multi-document transactions for `payments`, `idempotency_keys`, and `outbox` writes.
- Use `majority` read/write concern for financial state operations.
- Enforce conditional state transitions with `findOneAndUpdate` filters.
- Maintain core payment state as the authoritative source; derived read views are asynchronous.
