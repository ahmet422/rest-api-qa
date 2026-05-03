package com.mindtek.bookstore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtek.bookstore.error.ApiError;
import com.mindtek.bookstore.web.RequestIdAccessor;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if ("POST".equalsIgnoreCase(request.getMethod())
        && "/api/login".equals(request.getServletPath())) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(7).trim();
    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      JwtService.Parsed parsed = jwtService.parse(token);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(parsed.username(), null, parsed.authorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException | IllegalArgumentException ex) {
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      ApiError body =
          new ApiError(
              Instant.now().toString(),
              401,
              ApiError.CODE_UNAUTHORIZED,
              "Invalid or expired token",
              request.getRequestURI(),
              List.of(),
              RequestIdAccessor.current(request));
      objectMapper.writeValue(response.getOutputStream(), body);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
