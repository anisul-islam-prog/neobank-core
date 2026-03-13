package com.neobank.auth;

import java.util.UUID;

/**
 * Result of a user registration.
 *
 * @param userId the assigned user ID (null if failed)
 * @param success whether the registration was successful
 * @param message a descriptive message
 */
public record RegistrationResult(
        UUID userId,
        boolean success,
        String message
) {
    public static RegistrationResult success(UUID userId) {
        return new RegistrationResult(userId, true, "User registered successfully");
    }

    public static RegistrationResult failure(String reason) {
        return new RegistrationResult(null, false, reason);
    }
}
