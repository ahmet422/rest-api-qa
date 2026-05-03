package com.mindtek.bookstore.mvc;

import com.mindtek.bookstore.service.ReadOnlySqlConsoleService;
import com.mindtek.bookstore.service.ReadOnlySqlConsoleService.SqlConsoleResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reference/database")
@PreAuthorize("hasRole('ADMIN')")
public class ReferenceDatabaseController {

    private final ReadOnlySqlConsoleService sqlConsoleService;

    public ReferenceDatabaseController(ReadOnlySqlConsoleService sqlConsoleService) {
        this.sqlConsoleService = sqlConsoleService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("sqlDraft", "SELECT * FROM books LIMIT 20;");
        return "reference/database";
    }

    @PostMapping("/query")
    public String runQuery(@RequestParam(value = "sql", required = false) String sql, Model model) {
        model.addAttribute("sqlDraft", sql != null ? sql : "");
        if (sql == null || sql.isBlank()) {
            model.addAttribute("queryResult", SqlConsoleResult.error("Enter a SQL query."));
            return "reference/database";
        }
        try {
            SqlConsoleResult result = sqlConsoleService.execute(sql);
            model.addAttribute("queryResult", result);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("queryResult", SqlConsoleResult.error(ex.getMessage()));
        }
        return "reference/database";
    }
}
