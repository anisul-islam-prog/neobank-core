package com.neobank.core.transfers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a transfer is successfully completed.
 * Used for asynchronous side effects like notifications, analytics, etc.
 */
public record MoneyTransferredEvent(
        UUID transferId,
        UUID senderId,
        UUID receiverId,
        BigDecimal amount,
        String currency,
        String occurredAt
) {
    public MoneyTransferredEvent {
        if (transferId == null) {
            throw new IllegalArgumentException("transferId must not be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId must not be null");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("receiverId must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("currency must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }

    public static MoneyTransferredEvent of(UUID transferId, UUID senderId, UUID receiverId, BigDecimal amount, String currency) {
        return new MoneyTransferredEvent(transferId, senderId, receiverId, amount, currency, Instant.now().toString());
    }
}
