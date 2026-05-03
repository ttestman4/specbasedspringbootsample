package com.payments.shared.context;

import java.util.Optional;
import java.util.UUID;

public final class CorrelationIdContext {
    public static final String HEADER_NAME = "X-Correlation-Id";

    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();

    private CorrelationIdContext() {
    }

    public static void set(String id) {
        correlationId.set(id);
    }

    public static Optional<String> get() {
        return Optional.ofNullable(correlationId.get());
    }

    public static String getOrCreate() {
        return get().orElseGet(() -> {
            String generated = UUID.randomUUID().toString();
            set(generated);
            return generated;
        });
    }

    public static void clear() {
        correlationId.remove();
    }
}
