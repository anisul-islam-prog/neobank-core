package com.neobank.auth.web;

import com.neobank.auth.AuthenticationRequest;
import com.neobank.auth.AuthenticationResult;
import com.neobank.auth.RegistrationRequest;
import com.neobank.auth.RegistrationResult;
import com.neobank.auth.api.AuthApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication operations.
 * Provides public endpoints for user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApi authApi;

    public AuthController(AuthApi authApi) {
        this.authApi = authApi;
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResult> register(@RequestBody RegistrationRequest request) {
        RegistrationResult result = authApi.register(request);
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        // Return 409 Conflict for duplicate username/email
        if (result.message().contains("already exists") || result.message().contains("already registered")) {
            return ResponseEntity.status(409).body(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResult> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResult result = authApi.authenticate(request);
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    /**
     * Get current authenticated user info.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // In production, extract user from JWT token
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "username", "user@example.com"
        ));
    }
}
