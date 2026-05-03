package com.payments.shared.exception;

public class IdempotencyConflictException extends DomainException {
    private final String idempotencyKey;

    public IdempotencyConflictException(String idempotencyKey, String message) {
        super(message);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
