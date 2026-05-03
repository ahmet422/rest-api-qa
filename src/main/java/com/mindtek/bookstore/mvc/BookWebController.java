package com.mindtek.bookstore.mvc;

import com.mindtek.bookstore.api.dto.BookCreateRequest;
import com.mindtek.bookstore.api.dto.BookUpdateRequest;
import com.mindtek.bookstore.catalog.BookCatalogOptions;
import com.mindtek.bookstore.mvc.dto.BookForm;
import com.mindtek.bookstore.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
@RequestMapping("/books")
public class BookWebController {

    private final BookService bookService;

    public BookWebController(BookService bookService) {
        this.bookService = bookService;
    }

    @ModelAttribute("catalogCategories")
    public List<String> catalogCategories() {
        return BookCatalogOptions.CATEGORIES;
    }

    @ModelAttribute("catalogLanguages")
    public List<String> catalogLanguages() {
        return BookCatalogOptions.LANGUAGES;
    }

    @GetMapping
    public String list(
            Model model,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String q) {
        model.addAttribute("page", bookService.findPage(page, size, sort, category, language, q));
        model.addAttribute("category", category);
        model.addAttribute("language", language);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort != null ? sort : "createdAt,desc");
        return "books/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("editMode", false);
        return "books/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getById(id));
        return "books/detail";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(
            @Valid @ModelAttribute("bookForm") BookForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", false);
            return "books/form";
        }
        bookService.create(toCreate(form));
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        var b = bookService.getById(id);
        BookForm form = new BookForm();
        form.setTitle(b.title());
        form.setAuthor(b.author());
        form.setIsbn(b.isbn());
        form.setPrice(b.price());
        form.setStock(b.stock());
        form.setCategory(b.category());
        form.setLanguage(b.language());
        model.addAttribute("bookForm", form);
        model.addAttribute("editId", id);
        model.addAttribute("editMode", true);
        return "books/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("bookForm") BookForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editId", id);
            model.addAttribute("editMode", true);
            return "books/form";
        }
        bookService.replace(id, toUpdate(form));
        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        bookService.delete(id);
        return "redirect:/books";
    }

    private static BookCreateRequest toCreate(BookForm f) {
        return new BookCreateRequest(
                f.getTitle(),
                f.getAuthor(),
                blankToNull(f.getIsbn()),
                f.getPrice(),
                f.getStock(),
                f.getCategory(),
                f.getLanguage());
    }

    private static BookUpdateRequest toUpdate(BookForm f) {
        return new BookUpdateRequest(
                f.getTitle(),
                f.getAuthor(),
                blankToNull(f.getIsbn()),
                f.getPrice(),
                f.getStock(),
                f.getCategory(),
                f.getLanguage());
    }

    private static String blankToNull(String isbn) {
        return isbn == null || isbn.isBlank() ? null : isbn.trim();
    }
}
