package com.neobank.auth;

/**
 * Request to authenticate a user.
 *
 * @param username the username
 * @param password the password
 */
public record AuthenticationRequest(
        String username,
        String password
) {
    public AuthenticationRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }
    }
}
