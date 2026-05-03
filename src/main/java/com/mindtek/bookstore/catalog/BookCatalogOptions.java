package com.mindtek.bookstore.catalog;

import java.util.List;

public final class BookCatalogOptions {

  /** Values match seeded categories and API validation. */
  public static final List<String> CATEGORIES =
      List.of(
          "Business",
          "Computer Science",
          "Fiction",
          "History",
          "Operations",
          "Psychology",
          "Software");

  public static final List<String> LANGUAGES =
      List.of(
          "English",
          "Spanish",
          "French",
          "German",
          "Italian",
          "Portuguese",
          "Japanese",
          "Mandarin",
          "Other");

  private BookCatalogOptions() {}

  public static boolean isAllowedCategory(String value) {
    return value != null && CATEGORIES.contains(value);
  }

  public static boolean isAllowedLanguage(String value) {
    return value != null && LANGUAGES.contains(value);
  }
}
