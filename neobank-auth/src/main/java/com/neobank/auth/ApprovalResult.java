package com.neobank.auth;

import java.util.UUID;

/**
 * Result of a user/staff approval operation.
 *
 * @param userId the user ID (null if failed)
 * @param success whether the approval was successful
 * @param message a descriptive message
 */
public record ApprovalResult(
        UUID userId,
        boolean success,
        String message
) {
    public static ApprovalResult success(UUID userId, String message) {
        return new ApprovalResult(userId, true, message);
    }

    public static ApprovalResult failure(String reason) {
        return new ApprovalResult(null, false, reason);
    }
}
