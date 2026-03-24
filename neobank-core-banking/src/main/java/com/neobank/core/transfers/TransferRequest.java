package com.neobank.core.transfers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request record for initiating a fund transfer.
 */
public record TransferRequest(
        UUID fromId,
        UUID toId,
        BigDecimal amount,
        String currency,
        UUID initiatorId,
        String initiatorRole,
        String reason
) {
    public TransferRequest {
        if (fromId == null) {
            throw new IllegalArgumentException("fromId must not be null");
        }
        if (toId == null) {
            throw new IllegalArgumentException("toId must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (currency == null) {
            currency = "USD";
        }
    }

    public TransferRequest(UUID fromId, UUID toId, BigDecimal amount) {
        this(fromId, toId, amount, "USD", null, null, null);
    }

    public TransferRequest(UUID fromId, UUID toId, BigDecimal amount, String currency) {
        this(fromId, toId, amount, currency, null, null, null);
    }
}
