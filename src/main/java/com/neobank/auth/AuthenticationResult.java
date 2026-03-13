package com.neobank.auth;

import java.util.UUID;

/**
 * Result of user authentication.
 *
 * @param userId the authenticated user's ID
 * @param success whether authentication was successful
 * @param message a descriptive message
 * @param token the JWT token (null if failed)
 * @param expiresIn token expiration time in seconds
 */
public record AuthenticationResult(
        UUID userId,
        boolean success,
        String message,
        String token,
        Long expiresIn
) {
    public static AuthenticationResult success(UUID userId, String token, long expiresIn) {
        return new AuthenticationResult(userId, true, "Authentication successful", token, expiresIn);
    }

    public static AuthenticationResult failure(String reason) {
        return new AuthenticationResult(null, false, reason, null, null);
    }
}
