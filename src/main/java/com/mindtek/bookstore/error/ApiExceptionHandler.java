package com.mindtek.bookstore.error;

import com.mindtek.bookstore.security.UnauthorizedException;
import com.mindtek.bookstore.web.RequestIdAccessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            build(
                HttpStatus.NOT_FOUND.value(),
                ApiError.CODE_NOT_FOUND,
                ex.getMessage(),
                List.of(),
                req));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> unauthorized(UnauthorizedException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            build(
                HttpStatus.UNAUTHORIZED.value(),
                ApiError.CODE_UNAUTHORIZED,
                ex.getMessage(),
                List.of(),
                req));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            build(
                HttpStatus.FORBIDDEN.value(),
                ApiError.CODE_FORBIDDEN,
                "Insufficient permissions",
                List.of(),
                req));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<FieldErrorDto> fields =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());
    return ResponseEntity.badRequest()
        .body(
            build(
                HttpStatus.BAD_REQUEST.value(),
                ApiError.CODE_VALIDATION,
                "Validation failed",
                fields,
                req));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> constraints(
      ConstraintViolationException ex, HttpServletRequest req) {
    List<FieldErrorDto> fields =
        ex.getConstraintViolations().stream()
            .map(
                v ->
                    new FieldErrorDto(
                        v.getPropertyPath().toString(), v.getInvalidValue(), v.getMessage()))
            .collect(Collectors.toList());
    return ResponseEntity.badRequest()
        .body(
            build(
                HttpStatus.BAD_REQUEST.value(),
                ApiError.CODE_VALIDATION,
                "Validation failed",
                fields,
                req));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> typeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String msg = "Invalid value for parameter '%s'".formatted(ex.getName());
    return ResponseEntity.badRequest()
        .body(
            build(HttpStatus.BAD_REQUEST.value(), ApiError.CODE_BAD_REQUEST, msg, List.of(), req));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> notReadable(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(
            build(
                HttpStatus.BAD_REQUEST.value(),
                ApiError.CODE_BAD_REQUEST,
                "Malformed JSON body",
                List.of(),
                req));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> illegalArgument(
      IllegalArgumentException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(
            build(
                HttpStatus.BAD_REQUEST.value(),
                ApiError.CODE_BAD_REQUEST,
                ex.getMessage(),
                List.of(),
                req));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> conflict(
      DataIntegrityViolationException ex, HttpServletRequest req) {
    String message = "Data constraint violation";
    String lower =
        ex.getMostSpecificCause().getMessage() != null
            ? ex.getMostSpecificCause().getMessage().toLowerCase()
            : "";
    if (lower.contains("isbn") || lower.contains("books_isbn_unique")) {
      message = "Duplicate ISBN";
    }
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(build(HttpStatus.CONFLICT.value(), ApiError.CODE_CONFLICT, message, List.of(), req));
  }

  private FieldErrorDto mapFieldError(FieldError fe) {
    return new FieldErrorDto(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage());
  }

  private static ApiError build(
      int status, String code, String message, List<FieldErrorDto> fields, HttpServletRequest req) {
    return new ApiError(
        Instant.now().toString(),
        status,
        code,
        message,
        req.getRequestURI(),
        fields,
        RequestIdAccessor.current(req));
  }
}
