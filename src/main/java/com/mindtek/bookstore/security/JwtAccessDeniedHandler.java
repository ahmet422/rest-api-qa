package com.mindtek.bookstore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtek.bookstore.error.ApiError;
import com.mindtek.bookstore.web.RequestIdAccessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ApiError body =
        new ApiError(
            Instant.now().toString(),
            403,
            ApiError.CODE_FORBIDDEN,
            "Insufficient permissions",
            request.getRequestURI(),
            List.of(),
            RequestIdAccessor.current(request));
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
