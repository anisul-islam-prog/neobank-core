package com.neobank.onboarding.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new user registration is requested.
 * Listened by auth module to create technical credentials.
 *
 * @param userId the new user's identifier
 * @param username the username
 * @param email the email address
 * @param occurredAt when the event occurred
 */
public record UserAccountRequestedEvent(
        UUID userId,
        String username,
        String email,
        String occurredAt
) {
    public UserAccountRequestedEvent {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }

    public static UserAccountRequestedEvent of(UUID userId, String username, String email) {
        return new UserAccountRequestedEvent(userId, username, email, Instant.now().toString());
    }
}
