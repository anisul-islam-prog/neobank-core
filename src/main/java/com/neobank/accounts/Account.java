package com.neobank.accounts;

import java.math.BigDecimal;
import java.util.UUID;

public record Account(UUID id, String ownerName, BigDecimal balance) {
    public Account {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name must not be blank");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance must not be negative");
        }
    }
}
