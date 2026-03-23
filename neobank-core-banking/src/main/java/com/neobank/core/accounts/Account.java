package com.neobank.core.accounts;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain record for Account - immutable representation.
 */
public record Account(
        UUID id,
        String ownerName,
        BigDecimal balance
) {
    public Account {
        if (id == null) {
            throw new IllegalArgumentException("Account id must not be null");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Account ownerName must not be blank");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Account balance must not be null");
        }
    }
}
