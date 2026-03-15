/**
 * Loans module for loan origination and management.
 * Supports ScopedValue-based risk profile context during loan processing.
 *
 * Public APIs:
 * - {@link com.neobank.loans.LoanApi} - Loan application and management
 * - {@link com.neobank.loans.CreditScoreApi} - Credit score checking
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"accounts :: account-api", "auth"}
)
package com.neobank.loans;
