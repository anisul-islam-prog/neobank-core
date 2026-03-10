package com.neobank.transfers;

public sealed interface TransactionResult {
    record Success(String message) implements TransactionResult {}
    record Failure(String errorMessage) implements TransactionResult {}
}