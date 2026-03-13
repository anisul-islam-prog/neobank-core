package com.neobank.loans;

import java.time.Instant;
import java.util.UUID;

/**
 * Result of a loan disbursement.
 *
 * @param loanId the loan identifier
 * @param success whether the disbursement was successful
 * @param message a descriptive message
 * @param disbursedAt the timestamp of disbursement
 */
public record DisbursementResult(
        UUID loanId,
        boolean success,
        String message,
        Instant disbursedAt
) {
    public static DisbursementResult success(UUID loanId, Instant disbursedAt) {
        return new DisbursementResult(loanId, true, "Loan disbursed successfully", disbursedAt);
    }

    public static DisbursementResult failure(UUID loanId, String reason) {
        return new DisbursementResult(loanId, false, reason, null);
    }
}
