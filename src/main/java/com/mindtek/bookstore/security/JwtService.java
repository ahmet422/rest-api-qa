package com.mindtek.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 256 bits (32 bytes) for HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String createToken(String username, String roleName) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("role", roleName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Parsed parse(String token) {
        Claims claims =
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        String username = claims.getSubject();
        String role = claims.get("role", String.class);
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new Parsed(username, authorities, claims.getExpiration().toInstant());
    }

    public record Parsed(String username, Collection<? extends GrantedAuthority> authorities, Instant expiresAt) {}
}
