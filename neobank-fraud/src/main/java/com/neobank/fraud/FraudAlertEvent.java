package com.neobank.fraud;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a transfer is flagged as potentially fraudulent.
 * Used for asynchronous side effects like notifications, manual review, etc.
 */
public record FraudAlertEvent(
        UUID alertId,
        UUID transferId,
        UUID fromAccountId,
        UUID toAccountId,
        String alertType,
        String reason,
        int riskScore,
        String occurredAt
) {
    public FraudAlertEvent {
        if (alertId == null) {
            throw new IllegalArgumentException("alertId must not be null");
        }
        if (transferId == null) {
            throw new IllegalArgumentException("transferId must not be null");
        }
        if (fromAccountId == null) {
            throw new IllegalArgumentException("fromAccountId must not be null");
        }
        if (toAccountId == null) {
            throw new IllegalArgumentException("toAccountId must not be null");
        }
        if (alertType == null || alertType.isBlank()) {
            throw new IllegalArgumentException("alertType must not be blank");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (riskScore < 0 || riskScore > 100) {
            throw new IllegalArgumentException("riskScore must be between 0 and 100");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }

    public static FraudAlertEvent of(
            UUID alertId,
            UUID transferId,
            UUID fromAccountId,
            UUID toAccountId,
            String alertType,
            String reason,
            int riskScore
    ) {
        return new FraudAlertEvent(
                alertId,
                transferId,
                fromAccountId,
                toAccountId,
                alertType,
                reason,
                riskScore,
                Instant.now().toString()
        );
    }
}
