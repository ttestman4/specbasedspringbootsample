package com.payments.shared.idempotency;

import java.util.Optional;

public interface IdempotencyResponseCache {
    Optional<String> getResponsePayload(String idempotencyKey);

    void storeResponsePayload(String idempotencyKey, String responsePayload);

    void removeResponsePayload(String idempotencyKey);
}
