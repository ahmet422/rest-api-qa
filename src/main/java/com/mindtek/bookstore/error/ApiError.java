package com.mindtek.bookstore.error;

import java.util.List;

public record ApiError(
        String timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldErrorDto> fieldErrors,
        String requestId) {

    public static final String CODE_VALIDATION = "VALIDATION_ERROR";
    public static final String CODE_NOT_FOUND = "NOT_FOUND";
    public static final String CODE_CONFLICT = "CONFLICT";
    public static final String CODE_BAD_REQUEST = "BAD_REQUEST";
    public static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String CODE_FORBIDDEN = "FORBIDDEN";
}
