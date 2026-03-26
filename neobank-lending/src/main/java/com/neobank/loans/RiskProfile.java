package com.neobank.loans;

import java.math.BigDecimal;

/**
 * Risk profile for loan assessment.
 * Stored in ScopedValue during loan application processing.
 *
 * @param creditScore the applicant's credit score (300-850)
 * @param debtToIncomeRatio the applicant's debt-to-income ratio
 * @param employmentYears years of current employment
 * @param annualIncome the applicant's annual income
 */
public record RiskProfile(
        int creditScore,
        BigDecimal debtToIncomeRatio,
        int employmentYears,
        BigDecimal annualIncome
) {
    public RiskProfile {
        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("creditScore must be between 300 and 850");
        }
        if (debtToIncomeRatio == null || debtToIncomeRatio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("debtToIncomeRatio must be non-negative");
        }
        if (employmentYears < 0) {
            throw new IllegalArgumentException("employmentYears must be non-negative");
        }
        if (annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("annualIncome must be non-negative");
        }
    }

    /**
     * Get a risk score from 0-100 based on the profile.
     * Higher score = higher risk.
     */
    public int calculateRiskScore() {
        int score = 0;

        // Credit score component (0-40 points)
        if (creditScore < 580) {
            score += 40;
        } else if (creditScore < 670) {
            score += 25;
        } else if (creditScore < 740) {
            score += 10;
        }

        // DTI component (0-30 points)
        if (debtToIncomeRatio.compareTo(new BigDecimal("0.5")) > 0) {
            score += 30;
        } else if (debtToIncomeRatio.compareTo(new BigDecimal("0.4")) > 0) {
            score += 20;
        } else if (debtToIncomeRatio.compareTo(new BigDecimal("0.3")) > 0) {
            score += 10;
        }

        // Employment stability (0-15 points)
        if (employmentYears < 1) {
            score += 15;
        } else if (employmentYears < 3) {
            score += 8;
        } else if (employmentYears < 5) {
            score += 3;
        }

        // Income adequacy (0-15 points)
        if (annualIncome.compareTo(new BigDecimal("30000")) < 0) {
            score += 15;
        } else if (annualIncome.compareTo(new BigDecimal("50000")) < 0) {
            score += 8;
        } else if (annualIncome.compareTo(new BigDecimal("75000")) < 0) {
            score += 3;
        }

        return score;
    }

    /**
     * Determine risk level based on risk score.
     */
    public RiskLevel getRiskLevel() {
        int riskScore = calculateRiskScore();
        if (riskScore < 25) {
            return RiskLevel.LOW;
        } else if (riskScore < 50) {
            return RiskLevel.MEDIUM;
        } else if (riskScore < 75) {
            return RiskLevel.HIGH;
        } else {
            return RiskLevel.VERY_HIGH;
        }
    }
}
