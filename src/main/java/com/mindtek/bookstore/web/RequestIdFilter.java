package com.mindtek.bookstore.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Integer.MIN_VALUE)
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String id = request.getHeader(RequestIdConstants.HEADER_NAME);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        request.setAttribute(RequestIdConstants.REQUEST_ATTRIBUTE, id);
        response.setHeader(RequestIdConstants.HEADER_NAME, id);
        MDC.put("requestId", id);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}
