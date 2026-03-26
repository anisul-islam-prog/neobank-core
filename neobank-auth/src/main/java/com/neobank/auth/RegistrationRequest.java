package com.neobank.auth;

/**
 * Request to register a new user.
 *
 * @param username the desired username
 * @param password the password (will be hashed)
 * @param email the user's email address
 */
public record RegistrationRequest(
        String username,
        String password,
        String email
) {
    public RegistrationRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("password must be at least 8 characters");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
