package com.neobank.onboarding.web;

import com.neobank.auth.UserRole;
import com.neobank.onboarding.ApprovalResult;
import com.neobank.onboarding.UserStatus;
import com.neobank.onboarding.internal.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @PathVariable UUID id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID approverId = extractUserId(authentication);
        UserRole approverRole = extractUserRole(authentication);

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
            @RequestParam UserStatus status) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID updaterId = extractUserId(authentication);
        UserRole updaterRole = extractUserRole(authentication);

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
    public ResponseEntity<Map<String, Object>> getMyStatus() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        UUID userId = extractUserId(authentication);
        return onboardingService.getUserProfile(userId)
                .map(profile -> {
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("userId", userId.toString());
                    response.put("email", profile.getEmail());
                    response.put("status", profile.getStatus().toString());
                    response.put("kycVerified", profile.isKycVerified());
                    response.put("mustChangePassword", profile.isMustChangePassword());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return UUID.randomUUID();
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(username.getBytes());
        }
    }

    private UserRole extractUserRole(Authentication authentication) {
        if (authentication == null) return UserRole.CUSTOMER_RETAIL;
        return authentication.getAuthorities().stream()
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
