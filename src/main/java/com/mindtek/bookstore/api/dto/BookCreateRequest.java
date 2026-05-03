package com.mindtek.bookstore.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BookCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @Size(max = 32) String isbn,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(0) int stock,
        @NotBlank @Size(max = 128) String category,
        @NotBlank @Size(max = 64) String language) {}
