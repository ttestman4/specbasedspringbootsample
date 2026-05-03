package com.payments.shared.model;

import java.util.Collections;
import java.util.List;

public class ApiResponse<T> {
    private final T data;
    private final String status;
    private final String message;
    private final List<String> errors;

    public ApiResponse(T data, String status, String message, List<String> errors) {
        this.data = data;
        this.status = status;
        this.message = message;
        this.errors = errors == null ? Collections.emptyList() : errors;
    }

    public T getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, "SUCCESS", null, Collections.emptyList());
    }

    public static <T> ApiResponse<T> failure(String status, String message, List<String> errors) {
        return new ApiResponse<>(null, status, message, errors);
    }
}
