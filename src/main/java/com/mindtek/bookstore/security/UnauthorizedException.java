package com.mindtek.bookstore.security;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Invalid username or password");
    }
}
