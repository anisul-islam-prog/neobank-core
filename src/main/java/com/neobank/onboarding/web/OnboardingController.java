package com.neobank.onboarding.web;

import com.neobank.auth.UserRole;
import com.neobank.onboarding.ApprovalResult;
import com.neobank.onboarding.UserStatus;
import com.neobank.onboarding.internal.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for onboarding operations.
 * Handles user registration, approval, and status management.
 */
@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "User registration and approval workflow")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Register a new user (public endpoint).
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Public registration. User status set to PENDING awaiting approval.")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationRequest request) {
        OnboardingService.OnboardingResult result = onboardingService.registerUser(
                request.username(), request.email(), request.password());

        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", result.userId().toString(),
                    "message", result.message()
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", result.message()
        ));
    }

    /**
     * Approve a pending user.
     */
    @PutMapping("/users/{id}/approve")
    @Operation(summary = "Approve a pending user", description = "MANAGER, RELATIONSHIP_OFFICER, or SYSTEM_ADMIN only")
    public ResponseEntity<ApprovalResult> approveUser(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        UUID approverId = extractUserId(userDetails);
        UserRole approverRole = extractUserRole(userDetails);

        ApprovalResult result = onboardingService.approveUser(id, approverId, approverRole);

        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Update user status.
     */
    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Update user status", description = "MANAGER or SYSTEM_ADMIN only")
    public ResponseEntity<ApprovalResult> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        UUID updaterId = extractUserId(userDetails);
        UserRole updaterRole = extractUserRole(userDetails);

        ApprovalResult result = onboardingService.updateUserStatus(id, status, updaterRole);

        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Get current user's onboarding status.
     */
    @GetMapping("/me")
    @Operation(summary = "Get my onboarding status", description = "Returns current user's status and KYC info")
    public ResponseEntity<Map<String, Object>> getMyStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        UUID userId = extractUserId(userDetails);
        return onboardingService.getUserProfile(userId)
                .map(profile -> ResponseEntity.ok(Map.of(
                        "userId", userId.toString(),
                        "email", profile.getEmail(),
                        "status", profile.getStatus().toString(),
                        "kycVerified", profile.isKycVerified(),
                        "mustChangePassword", profile.isMustChangePassword()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    private UUID extractUserId(UserDetails userDetails) {
        if (userDetails == null) return UUID.randomUUID();
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(userDetails.getUsername().getBytes());
        }
    }

    private UserRole extractUserRole(UserDetails userDetails) {
        if (userDetails == null) return UserRole.CUSTOMER_RETAIL;
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
                        return UserRole.CUSTOMER_RETAIL;
                    }
                })
                .orElse(UserRole.CUSTOMER_RETAIL);
    }

    /**
     * Registration request DTO.
     */
    public record RegistrationRequest(
            String username,
            String email,
            String password
    ) {}
}
