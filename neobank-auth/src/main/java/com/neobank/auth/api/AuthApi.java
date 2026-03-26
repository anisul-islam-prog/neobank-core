package com.neobank.auth.api;

import com.neobank.auth.*;

import java.util.UUID;

/**
 * Public API interface for the auth module.
 * Defines the contract for user authentication, registration, and approval workflow.
 * 
 * This is a named interface for Spring Modulith - other modules can depend on
 * "auth :: auth-api" to access authentication functionality without coupling
 * to internal implementation details.
 */
@org.springframework.modulith.NamedInterface("auth-api")
public interface AuthApi {

    /**
     * Register a new user.
     * Public registration sets status to PENDING and role to ROLE_GUEST.
     *
     * @param request the registration request
     * @return the registration result with user ID
     */
    RegistrationResult register(RegistrationRequest request);

    /**
     * Authenticate a user and return JWT token.
     * Rejects PENDING or SUSPENDED users with 403 Forbidden.
     *
     * @param request the authentication request
     * @return the authentication result with token and status flags
     */
    AuthenticationResult authenticate(AuthenticationRequest request);

    /**
     * Get user ID from JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    UUID getUserIdFromToken(String token);

    /**
     * Onboard a new staff user with specific role.
     * Only accessible by SYSTEM_ADMIN, MANAGER, or RELATIONSHIP_OFFICER.
     * Sets mustChangePassword = true to force password reset on first login.
     *
     * @param request the staff onboarding request
     * @param createdBy the role of the user creating the account
     * @return the registration result with user ID
     */
    RegistrationResult onboardStaff(StaffOnboardingRequest request, UserRole createdBy);

    /**
     * Approve a pending user (promote from ROLE_GUEST to CUSTOMER_RETAIL).
     * Only accessible by MANAGER or RELATIONSHIP_OFFICER.
     *
     * @param userId the user ID to approve
     * @param approvedBy the role of the approving user
     * @return the approval result
     */
    ApprovalResult approveUser(UUID userId, UserRole approvedBy);

    /**
     * Approve a staff user (activate MANAGER, TELLER, or RO account).
     * Only accessible by SYSTEM_ADMIN.
     *
     * @param userId the user ID to approve
     * @param approvedBy the role of the approving user (must be SYSTEM_ADMIN)
     * @return the approval result
     */
    ApprovalResult approveStaff(UUID userId, UserRole approvedBy);

    /**
     * Update user status (e.g., suspend/activate).
     * Only accessible by SYSTEM_ADMIN or MANAGER.
     *
     * @param userId the user ID
     * @param newStatus the new status to set
     * @param updatedBy the role of the updating user
     * @return the approval result
     */
    ApprovalResult updateUserStatus(UUID userId, UserStatus newStatus, UserRole updatedBy);
}
