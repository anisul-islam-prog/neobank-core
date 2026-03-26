package com.neobank.loans;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Public API interface for the loans module.
 * Defines the contract for loan origination and management.
 */
@org.springframework.modulith.NamedInterface("loan-api")
public interface LoanApi {

    /**
     * Apply for a new loan.
     *
     * @param request the loan application request
     * @return the loan application result with status
     */
    LoanApplicationResult apply(LoanApplicationRequest request);

    /**
     * Get loan details by ID.
     *
     * @param loanId the loan identifier
     * @return the loan details or null if not found
     */
    LoanDetails getLoan(UUID loanId);

    /**
     * Disburse an approved loan to the borrower's account.
     *
     * @param loanId the loan identifier
     * @return the disbursement result
     */
    DisbursementResult disburse(UUID loanId);
}
