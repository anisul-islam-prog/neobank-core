package com.neobank.loans;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request to apply for a new loan.
 *
 * @param accountId the borrower's account ID
 * @param principal the loan amount requested
 * @param termMonths the loan term in months
 * @param purpose the loan purpose (e.g., "personal", "auto", "home")
 */
public record LoanApplicationRequest(
        UUID accountId,
        BigDecimal principal,
        int termMonths,
        String purpose
) {
    public LoanApplicationRequest {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("principal must be positive");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("termMonths must be positive");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("purpose must not be blank");
        }
    }
}
