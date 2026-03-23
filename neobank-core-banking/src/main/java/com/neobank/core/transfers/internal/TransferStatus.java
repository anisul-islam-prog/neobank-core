package com.neobank.core.transfers.internal;

import jakarta.persistence.*;

/**
 * Enum representing the status of a transfer.
 */
public enum TransferStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED
}
