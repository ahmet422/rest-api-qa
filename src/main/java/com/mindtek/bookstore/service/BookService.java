package com.mindtek.bookstore.service;

import com.mindtek.bookstore.api.dto.BookCreateRequest;
import com.mindtek.bookstore.api.dto.BookResponse;
import com.mindtek.bookstore.api.dto.BookUpdateRequest;
import com.mindtek.bookstore.catalog.BookCatalogOptions;
import com.mindtek.bookstore.domain.Book;
import com.mindtek.bookstore.error.ResourceNotFoundException;
import com.mindtek.bookstore.repo.BookRepository;
import com.mindtek.bookstore.repo.BookSpecifications;
import java.util.Arrays;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "createdAt",
                    "updatedAt",
                    "title",
                    "price",
                    "stock",
                    "category",
                    "author",
                    "language");

    private final BookRepository bookRepository;

    @Value("${app.books.page-default-size:20}")
    private int defaultPageSize;

    @Value("${app.books.page-max-size:100}")
    private int maxPageSize;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Page<BookResponse> findPage(
            int page, int size, String sortParam, String category, String language, String q) {
        int effectiveSize = size <= 0 ? defaultPageSize : Math.min(size, maxPageSize);
        Sort sort = parseSort(sortParam);
        Pageable pageable = PageRequest.of(page, effectiveSize, sort);
        Specification<Book> spec = BookSpecifications.catalogFilter(category, language, q);
        return bookRepository.findAll(spec, pageable).map(BookResponse::from);
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] pair = sortParam.split(",", 2);
        String field = pair[0].trim();
        Sort.Direction dir =
                pair.length > 1 && pair[1].trim().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    "Invalid sort field '%s'. Allowed: %s"
                            .formatted(field, Arrays.toString(ALLOWED_SORT_FIELDS.toArray())));
        }
        return Sort.by(dir, field);
    }

    public BookResponse getById(Long id) {
        return bookRepository.findById(id).map(BookResponse::from).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public BookResponse create(BookCreateRequest req) {
        validateCatalogLabels(req.category(), req.language());
        Book b = new Book();
        applyCreate(b, req);
        return BookResponse.from(bookRepository.save(b));
    }

    @Transactional
    public BookResponse replace(Long id, BookUpdateRequest req) {
        validateCatalogLabels(req.category(), req.language());
        Book b = bookRepository.findById(id).orElseThrow(() -> notFound(id));
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(blankToNull(req.isbn()));
        b.setPrice(req.price());
        b.setStock(req.stock());
        b.setCategory(req.category());
        b.setLanguage(req.language());
        return BookResponse.from(bookRepository.save(b));
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw notFound(id);
        }
        bookRepository.deleteById(id);
    }

    private static void validateCatalogLabels(String category, String language) {
        if (!BookCatalogOptions.isAllowedCategory(category)) {
            throw new IllegalArgumentException(
                    "Invalid category. Allowed: " + BookCatalogOptions.CATEGORIES);
        }
        if (!BookCatalogOptions.isAllowedLanguage(language)) {
            throw new IllegalArgumentException(
                    "Invalid language. Allowed: " + BookCatalogOptions.LANGUAGES);
        }
    }

    private static void applyCreate(Book b, BookCreateRequest req) {
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(blankToNull(req.isbn()));
        b.setPrice(req.price());
        b.setStock(req.stock());
        b.setCategory(req.category());
        b.setLanguage(req.language());
    }

    private static String blankToNull(String isbn) {
        return isbn == null || isbn.isBlank() ? null : isbn.trim();
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Book not found with id " + id);
    }
}
