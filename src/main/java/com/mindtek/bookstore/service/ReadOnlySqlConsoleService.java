package com.mindtek.bookstore.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReadOnlySqlConsoleService {

  private static final int MAX_ROWS = 500;
  private static final int QUERY_TIMEOUT_SEC = 15;

  private static final Pattern ALLOWED_PREFIX =
      Pattern.compile(
          "^\\s*(SELECT|WITH|EXPLAIN|SHOW|DESCRIBE|DESC)\\b",
          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private final JdbcTemplate jdbcTemplate;

  public ReadOnlySqlConsoleService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public SqlConsoleResult execute(String rawSql) {
    String sql = validateAndNormalize(rawSql);
    try {
      return jdbcTemplate.execute(
          (ConnectionCallback<SqlConsoleResult>) connection -> runQuery(connection, sql));
    } catch (DataAccessException ex) {
      Throwable cause = ex.getMostSpecificCause();
      String msg =
          cause instanceof SQLException ? ((SQLException) cause).getMessage() : ex.getMessage();
      throw new IllegalArgumentException(msg != null ? msg : ex.getMessage(), ex);
    }
  }

  private static SqlConsoleResult runQuery(Connection connection, String sql) throws SQLException {
    try (Statement st = connection.createStatement()) {
      st.setMaxRows(MAX_ROWS + 1);
      st.setQueryTimeout(QUERY_TIMEOUT_SEC);
      boolean hasRs = st.execute(sql);
      if (!hasRs) {
        return new SqlConsoleResult(List.of(), List.of(), false, 0, null);
      }
      try (ResultSet rs = st.getResultSet()) {
        ResultSetMetaData md = rs.getMetaData();
        int colCount = md.getColumnCount();
        List<String> columns = new ArrayList<>(colCount);
        for (int i = 1; i <= colCount; i++) {
          columns.add(md.getColumnLabel(i));
        }
        List<List<Object>> rows = new ArrayList<>();
        int count = 0;
        while (rs.next()) {
          count++;
          if (count > MAX_ROWS) {
            return new SqlConsoleResult(columns, rows, true, rows.size(), null);
          }
          List<Object> values = new ArrayList<>(colCount);
          for (int i = 1; i <= colCount; i++) {
            values.add(rs.getObject(i));
          }
          rows.add(values);
        }
        return new SqlConsoleResult(columns, rows, false, rows.size(), null);
      }
    }
  }

  static String validateAndNormalize(String rawSql) {
    if (rawSql == null || rawSql.isBlank()) {
      throw new IllegalArgumentException("Enter a SQL query.");
    }
    String sql = rawSql.trim();
    if (sql.endsWith(";")) {
      sql = sql.substring(0, sql.length() - 1).trim();
    }
    if (containsSemicolonOutsideQuotes(sql)) {
      throw new IllegalArgumentException(
          "Only one statement is allowed. Remove extra semicolons or split into separate runs.");
    }
    String probe = sql.toUpperCase(Locale.ROOT);
    for (String forbidden :
        List.of(
            " INSERT ",
            " UPDATE ",
            " DELETE ",
            " DROP ",
            " ALTER ",
            " CREATE ",
            " TRUNCATE ",
            " GRANT ",
            " REVOKE ",
            " MERGE ",
            " CALL ",
            " EXEC ",
            " EXECUTE ")) {
      if (probe.contains(forbidden)) {
        throw new IllegalArgumentException(
            "Only read-only queries are allowed (e.g. SELECT, EXPLAIN, SHOW).");
      }
    }
    if (!ALLOWED_PREFIX.matcher(sql).find()) {
      throw new IllegalArgumentException(
          "Query must start with SELECT, WITH, EXPLAIN, SHOW, DESCRIBE, or DESC (table).");
    }
    return sql;
  }

  /**
   * Detect ';' that could start a second statement (ignores semicolons inside single-quoted
   * literals).
   */
  static boolean containsSemicolonOutsideQuotes(String sql) {
    boolean inSingle = false;
    for (int i = 0; i < sql.length(); i++) {
      char c = sql.charAt(i);
      if (c == '\'') {
        if (inSingle && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
          i++;
          continue;
        }
        inSingle = !inSingle;
      } else if (c == ';' && !inSingle) {
        return true;
      }
    }
    return false;
  }

  public record SqlConsoleResult(
      List<String> columns,
      List<List<Object>> rows,
      boolean truncated,
      int rowCount,
      String errorMessage) {

    public static SqlConsoleResult error(String message) {
      return new SqlConsoleResult(List.of(), List.of(), false, 0, message);
    }
  }
}
