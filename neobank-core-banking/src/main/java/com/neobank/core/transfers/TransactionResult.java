package com.neobank.core.transfers;

import java.util.UUID;

/**
 * Result record for a transfer transaction.
 */
public sealed interface TransactionResult {
    boolean isSuccess();
    String message();

    record Success(String message) implements TransactionResult {
        public Success {
            if (message == null) {
                throw new IllegalArgumentException("message must not be null");
            }
        }

        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    record Failure(String message) implements TransactionResult {
        public Failure {
            if (message == null) {
                throw new IllegalArgumentException("message must not be null");
            }
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}
