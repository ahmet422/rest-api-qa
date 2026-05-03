package com.mindtek.bookstore.api;

import com.mindtek.bookstore.api.dto.LoginRequest;
import com.mindtek.bookstore.api.dto.LoginResponse;
import com.mindtek.bookstore.security.JwtService;
import com.mindtek.bookstore.security.UnauthorizedException;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@SecurityRequirements
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(), request.password()));
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String role =
                    principal.getAuthorities().stream()
                            .findFirst()
                            .map(a -> a.getAuthority().replace("ROLE_", ""))
                            .orElse("USER");
            String token = jwtService.createToken(principal.getUsername(), role);
            return new LoginResponse(token, "Bearer", expirationMs / 1000);
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException();
        }
    }
}
