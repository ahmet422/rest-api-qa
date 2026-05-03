package com.mindtek.bookstore.repo;

import com.mindtek.bookstore.domain.Book;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecifications {

  private BookSpecifications() {}

  public static Specification<Book> catalogFilter(String category, String language, String q) {
    return (root, query, cb) -> {
      List<Predicate> parts = new ArrayList<>();
      if (category != null && !category.isBlank()) {
        parts.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
      }
      if (language != null && !language.isBlank()) {
        parts.add(cb.equal(root.get("language"), language.trim()));
      }
      if (q != null && !q.isBlank()) {
        parts.add(cb.like(cb.lower(root.get("title")), "%" + q.trim().toLowerCase() + "%"));
      }
      if (parts.isEmpty()) {
        return cb.conjunction();
      }
      return cb.and(parts.toArray(Predicate[]::new));
    };
  }
}
