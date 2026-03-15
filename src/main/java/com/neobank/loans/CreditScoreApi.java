package com.neobank.loans;

import java.util.UUID;

/**
 * Public API interface for credit score operations.
 * Allows users to fetch their credit score and risk assessment.
 */
public interface CreditScoreApi {

    /**
     * Get the credit score for a user.
     *
     * @param userId the user identifier
     * @return the credit score result with risk assessment
     */
    CreditScoreResult getCreditScore(UUID userId);

    /**
     * Calculate a credit score based on risk profile.
     *
     * @param riskProfile the user's risk profile
     * @return the calculated credit score (0-100)
     */
    int calculateScore(RiskProfile riskProfile);
}
