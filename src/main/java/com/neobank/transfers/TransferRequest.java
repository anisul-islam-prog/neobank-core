package com.neobank.transfers;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID fromId, UUID toId, BigDecimal amount) {
}