package com.neobank.auth.web;

import com.neobank.auth.*;
import com.neobank.auth.api.AuthApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for user approval workflow operations.
 * Administrative endpoints for managing user status and approvals.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Management", description = "Administrative operations for user approval and status management")
@SecurityRequirement(name = "bearerAuth")
public class ApprovalApi {

    private final AuthApi authApi;

    public ApprovalApi(AuthApi authApi) {
        this.authApi = authApi;
    }

    /**
     * Approve a pending user (promote from ROLE_GUEST to CUSTOMER_RETAIL).
     * Only accessible by MANAGER or RELATIONSHIP_OFFICER.
     */
    @PutMapping("/users/{id}/approve")
    @Tag(name = "Administrative Operations", description = "Admin-only endpoints")
    @Operation(
            summary = "Approve a pending user",
            description = """
                    Promote a pending user from ROLE_GUEST to CUSTOMER_RETAIL.
                    
                    **Access Control:**
                    - MANAGER: Can approve
                    - RELATIONSHIP_OFFICER: Can approve
                    - SYSTEM_ADMIN: Can approve
                    
                    The user will be able to login and access banking features after approval.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User approved successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResult.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApprovalResult> approveUser(
            @Parameter(description = "User ID to approve") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        UserRole approverRole = getUserRole(userDetails);
        ApprovalResult result = authApi.approveUser(id, approverRole);
        
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Approve a staff user (activate MANAGER, TELLER, or RO account).
     * Only accessible by SYSTEM_ADMIN.
     */
    @PutMapping("/staff/{id}/approve")
    @Tag(name = "Administrative Operations", description = "Admin-only endpoints")
    @Operation(
            summary = "Approve a staff user",
            description = """
                    Activate a pending staff user account (MANAGER, TELLER, RO, or AUDITOR).
                    
                    **Access Control:**
                    - SYSTEM_ADMIN: Can approve
                    - Other roles: Forbidden
                    
                    Staff users can login and access their role-specific features after approval.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Staff user approved successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResult.class))),
            @ApiResponse(responseCode = "403", description = "Only SYSTEM_ADMIN can approve staff"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApprovalResult> approveStaff(
            @Parameter(description = "Staff user ID to approve") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        UserRole approverRole = getUserRole(userDetails);
        ApprovalResult result = authApi.approveStaff(id, approverRole);
        
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Update user status (suspend/activate).
     */
    @PatchMapping("/users/{id}/status")
    @Tag(name = "Administrative Operations", description = "Admin-only endpoints")
    @Operation(
            summary = "Update user status",
            description = """
                    Change a user's status (ACTIVE, SUSPENDED, PENDING).
                    
                    **Access Control:**
                    - SYSTEM_ADMIN: Can update any status
                    - MANAGER: Can update status
                    
                    Use SUSPENDED to temporarily block user access.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResult.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApprovalResult> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "New status (ACTIVE, SUSPENDED, PENDING)") 
            @RequestParam UserStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        UserRole updaterRole = getUserRole(userDetails);
        ApprovalResult result = authApi.updateUserStatus(id, status, updaterRole);
        
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Extract user role from UserDetails.
     * In production, this parses the role from JWT claims.
     */
    private UserRole getUserRole(UserDetails userDetails) {
        if (userDetails == null) {
            return UserRole.ROLE_GUEST;
        }
        
        // Extract role from authorities (format: "ROLE_XYZ")
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
