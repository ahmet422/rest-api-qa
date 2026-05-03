package com.mindtek.bookstore.api.dto;

import com.mindtek.bookstore.domain.Book;
import java.math.BigDecimal;
import java.time.Instant;

public record BookResponse(
    Long id,
    String title,
    String author,
    String isbn,
    BigDecimal price,
    int stock,
    String category,
    String language,
    Instant createdAt,
    Instant updatedAt) {

  public static BookResponse from(Book b) {
    return new BookResponse(
        b.getId(),
        b.getTitle(),
        b.getAuthor(),
        b.getIsbn(),
        b.getPrice(),
        b.getStock(),
        b.getCategory(),
        b.getLanguage(),
        b.getCreatedAt(),
        b.getUpdatedAt());
  }
}
