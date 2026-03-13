package com.neobank.loans;

/**
 * Status of a loan application.
 */
public enum ApplicationStatus {
    /**
     * Application submitted, awaiting review.
     */
    PENDING,

    /**
     * Loan approved, ready for disbursement.
     */
    APPROVED,

    /**
     * Loan application rejected.
     */
    REJECTED,

    /**
     * Loan disbursed to borrower's account.
     */
    DISBURSED,

    /**
     * Loan active and being repaid.
     */
    ACTIVE,

    /**
     * Loan fully repaid.
     */
    PAID_OFF,

    /**
     * Loan in default.
     */
    DEFAULT
}
