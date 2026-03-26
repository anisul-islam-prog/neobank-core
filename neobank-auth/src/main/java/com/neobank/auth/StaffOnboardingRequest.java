package com.neobank.auth;

import java.util.UUID;

/**
 * Request to onboard a new staff user.
 * Used by Admin/RO to create staff accounts with specific roles.
 *
 * @param username the desired username
 * @param password the initial password (user must change on first login)
 * @param email the user's email address
 * @param role the role to assign
 * @param branchId the branch ID (optional, defaults to Head Office)
 */
public record StaffOnboardingRequest(
        String username,
        String password,
        String email,
        UserRole role,
        UUID branchId
) {
    public StaffOnboardingRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("password must be at least 8 characters");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
    }
}
