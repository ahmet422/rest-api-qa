package com.mindtek.bookstore.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestIdAccessor {

    private RequestIdAccessor() {}

    public static String current(HttpServletRequest request) {
        if (request != null) {
            Object v = request.getAttribute(RequestIdConstants.REQUEST_ATTRIBUTE);
            return v != null ? v.toString() : null;
        }
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        Object v = attrs.getRequest().getAttribute(RequestIdConstants.REQUEST_ATTRIBUTE);
        return v != null ? v.toString() : null;
    }
}
