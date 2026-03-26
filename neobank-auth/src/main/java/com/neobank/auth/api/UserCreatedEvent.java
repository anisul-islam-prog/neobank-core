package com.neobank.auth.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new user is registered.
 * Listened by accounts module to create default savings account.
 *
 * @param userId the new user's identifier
 * @param username the username
 * @param branchId the user's assigned branch identifier
 * @param occurredAt when the event occurred
 */
public record UserCreatedEvent(
        UUID userId,
        String username,
        UUID branchId,
        String occurredAt
) {
    public UserCreatedEvent {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
    }

    public static UserCreatedEvent of(UUID userId, String username, UUID branchId) {
        return new UserCreatedEvent(userId, username, branchId, Instant.now().toString());
    }
}
