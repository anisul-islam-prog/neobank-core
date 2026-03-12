package com.neobank.transfers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a transfer is successfully completed.
 * Used for asynchronous side effects like notifications, analytics, etc.
 */
public record TransferCompletedEvent(
        UUID transferId,
        UUID fromId,
        UUID toId,
        BigDecimal amount,
        String occurredAt
) {
    public TransferCompletedEvent {
        if (transferId == null) {
            throw new IllegalArgumentException("transferId must not be null");
        }
        if (fromId == null) {
            throw new IllegalArgumentException("fromId must not be null");
        }
        if (toId == null) {
            throw new IllegalArgumentException("toId must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }

    public static TransferCompletedEvent of(UUID transferId, UUID fromId, UUID toId, BigDecimal amount) {
        return new TransferCompletedEvent(transferId, fromId, toId, amount, Instant.now().toString());
    }
}
