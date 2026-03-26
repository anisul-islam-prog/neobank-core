package com.neobank.loans;

import java.math.BigDecimal;

/**
 * Credit score result for a user.
 *
 * @param userId the user identifier
 * @param creditScore the credit score (0-100, higher = higher risk)
 * @param riskLevel the risk level assessment
 * @param creditHistory the user's credit history summary
 * @param debtToIncome the debt-to-income ratio
 * @param employmentYears years of current employment
 * @param annualIncome the annual income
 */
public record CreditScoreResult(
        String userId,
        int creditScore,
        RiskLevel riskLevel,
        String creditHistory,
        BigDecimal debtToIncome,
        int employmentYears,
        BigDecimal annualIncome
) {
    public CreditScoreResult {
        if (creditScore < 0 || creditScore > 100) {
            throw new IllegalArgumentException("creditScore must be between 0 and 100");
        }
        if (debtToIncome == null || debtToIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("debtToIncome must be non-negative");
        }
        if (employmentYears < 0) {
            throw new IllegalArgumentException("employmentYears must be non-negative");
        }
        if (annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("annualIncome must be non-negative");
        }
    }

    /**
     * Create a credit score result from a risk profile.
     */
    public static CreditScoreResult fromRiskProfile(String userId, RiskProfile profile) {
        int score = profile.calculateRiskScore();
        RiskLevel level = profile.getRiskLevel();
        
        String creditHistory = score < 25 ? "Excellent" :
                               score < 50 ? "Good" :
                               score < 75 ? "Fair" : "Poor";

        return new CreditScoreResult(
                userId,
                score,
                level,
                creditHistory,
                profile.debtToIncomeRatio(),
                profile.employmentYears(),
                profile.annualIncome()
        );
    }
}
