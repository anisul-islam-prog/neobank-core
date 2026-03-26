package com.neobank.loans;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Result of a loan application.
 *
 * @param loanId the assigned loan ID (null if application failed)
 * @param status the application status
 * @param message a descriptive message
 * @param calculatedMonthlyPayment the calculated monthly payment (if approved)
 */
public record LoanApplicationResult(
        UUID loanId,
        ApplicationStatus status,
        String message,
        BigDecimal calculatedMonthlyPayment
) {
    public static LoanApplicationResult pending(UUID loanId, BigDecimal monthlyPayment) {
        return new LoanApplicationResult(loanId, ApplicationStatus.PENDING, "Application submitted for review", monthlyPayment);
    }

    public static LoanApplicationResult rejected(String reason) {
        return new LoanApplicationResult(null, ApplicationStatus.REJECTED, reason, null);
    }

    public static LoanApplicationResult approved(UUID loanId, BigDecimal monthlyPayment) {
        return new LoanApplicationResult(loanId, ApplicationStatus.APPROVED, "Loan approved", monthlyPayment);
    }

    public static LoanApplicationResult failure(String reason) {
        return new LoanApplicationResult(null, ApplicationStatus.PENDING, reason, null);
    }
}
