/**
 * Core transfers module for fund transfer operations.
 * Provides transfer execution and emits MoneyTransferredEvent on completion.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"accounts :: account-api"}
)
package com.neobank.core.transfers;
