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
 * @param mustChangePassword flag indicating user must reset password
 * @param status the user's current status
 */
public record AuthenticationResult(
        UUID userId,
        boolean success,
        String message,
        String token,
        Long expiresIn,
        boolean mustChangePassword,
        UserStatus status
) {
    public static AuthenticationResult success(UUID userId, String token, long expiresIn, boolean mustChangePassword, UserStatus status) {
        return new AuthenticationResult(userId, true, "Authentication successful", token, expiresIn, mustChangePassword, status);
    }

    public static AuthenticationResult failure(String reason) {
        return new AuthenticationResult(null, false, reason, null, null, false, null);
    }

    public static AuthenticationResult forbidden(String reason) {
        return new AuthenticationResult(null, false, reason, null, null, false, null);
    }
}
