/**
 * Loans module for loan origination and management.
 * Supports ScopedValue-based risk profile context during loan processing.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"accounts :: account-api"}
)
package com.neobank.loans;
