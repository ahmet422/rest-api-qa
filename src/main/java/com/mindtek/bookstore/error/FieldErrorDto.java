package com.mindtek.bookstore.error;

public record FieldErrorDto(String field, Object rejectedValue, String message) {}
