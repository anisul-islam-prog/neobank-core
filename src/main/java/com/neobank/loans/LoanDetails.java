package com.neobank.loans;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detailed information about a loan.
 *
 * @param id the loan identifier
 * @param accountId the borrower's account ID
 * @param principal the original loan amount
 * @param outstandingBalance the remaining balance
 * @param interestRate the annual interest rate (as decimal, e.g., 0.05 for 5%)
 * @param termMonths the loan term in months
 * @param monthlyPayment the calculated monthly payment
 * @param status the current loan status
 * @param createdAt when the loan was created
 * @param disbursedAt when the loan was disbursed (null if not yet)
 * @param schedule the amortization schedule
 */
public record LoanDetails(
        UUID id,
        UUID accountId,
        BigDecimal principal,
        BigDecimal outstandingBalance,
        BigDecimal interestRate,
        int termMonths,
        BigDecimal monthlyPayment,
        ApplicationStatus status,
        Instant createdAt,
        Instant disbursedAt,
        List<AmortizationEntry> schedule
) {
}
