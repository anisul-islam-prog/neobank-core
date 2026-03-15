package com.neobank.auth.web;

import com.neobank.auth.*;
import com.neobank.auth.api.AuthApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication operations.
 * Provides public endpoints for user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public authentication endpoints")
public class AuthController {

    private final AuthApi authApi;

    public AuthController(AuthApi authApi) {
        this.authApi = authApi;
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Public registration. User status set to PENDING with ROLE_GUEST.")
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
    @Operation(summary = "Login", description = """
            Authenticate user and receive JWT token.
            
            **Important:**
            - PENDING users: Returns 403 Forbidden (awaiting approval)
            - SUSPENDED users: Returns 403 Forbidden (account suspended)
            - Active users with mustChangePassword=true: Response includes flag to force password reset
            """)
    public ResponseEntity<AuthenticationResult> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResult result = authApi.authenticate(request);
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        // Return 403 Forbidden for PENDING/SUSPENDED users
        if (result.message().contains("pending") || result.message().contains("suspended")) {
            return ResponseEntity.status(403).body(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    /**
     * Onboard a new staff user.
     * Only accessible by SYSTEM_ADMIN, MANAGER, or RELATIONSHIP_OFFICER.
     */
    @PostMapping("/onboard")
    @Tag(name = "Administrative Operations", description = "Admin-only endpoints")
    @Operation(summary = "Onboard staff user", description = """
            Create a new staff user with specific role.
            
            **Access Control:**
            - SYSTEM_ADMIN: Can onboard any role
            - MANAGER: Can onboard TELLER, CUSTOMER_RETAIL, CUSTOMER_BUSINESS
            - RELATIONSHIP_OFFICER: Can onboard TELLER, CUSTOMER_RETAIL, CUSTOMER_BUSINESS
            
            **Note:** Staff users are created with mustChangePassword=true to force password reset on first login.
            """)
    public ResponseEntity<RegistrationResult> onboardStaff(
            @RequestBody StaffOnboardingRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        UserRole creatorRole = getUserRole(userDetails);
        RegistrationResult result = authApi.onboardStaff(request, creatorRole);
        
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        if (result.message().contains("already exists") || result.message().contains("already registered")) {
            return ResponseEntity.status(409).body(result);
        }
        if (result.message().contains("privileges")) {
            return ResponseEntity.status(403).body(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Get current authenticated user info.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Returns the authenticated user's details")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities()
        ));
    }

    /**
     * Extract user role from UserDetails.
     */
    private UserRole getUserRole(UserDetails userDetails) {
        if (userDetails == null) {
            return UserRole.ROLE_GUEST;
        }
        
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> {
                    String roleName = auth.getAuthority();
                    if (roleName.startsWith("ROLE_")) {
                        roleName = roleName.substring(5);
                    }
                    try {
                        return UserRole.valueOf(roleName);
                    } catch (IllegalArgumentException e) {
                        return UserRole.ROLE_GUEST;
                    }
                })
                .orElse(UserRole.ROLE_GUEST);
    }
}
