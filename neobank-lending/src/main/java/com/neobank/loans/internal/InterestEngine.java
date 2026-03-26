package com.neobank.loans.internal;

import com.neobank.loans.RiskLevel;
import com.neobank.loans.RiskProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Interest calculation engine using risk-based pricing.
 * Reads RiskProfile from ScopedValue context.
 */
@Component
class InterestEngine {

    /**
     * Base annual interest rate (5%).
     */
    private static final BigDecimal BASE_RATE = new BigDecimal("0.05");

    /**
     * Risk premium for each risk level.
     */
    private static final BigDecimal LOW_RISK_PREMIUM = new BigDecimal("0.01");
    private static final BigDecimal MEDIUM_RISK_PREMIUM = new BigDecimal("0.03");
    private static final BigDecimal HIGH_RISK_PREMIUM = new BigDecimal("0.06");
    private static final BigDecimal VERY_HIGH_RISK_PREMIUM = new BigDecimal("0.10");

    /**
     * Calculate the interest rate based on the current RiskProfile in ScopedValue.
     *
     * @return the calculated annual interest rate (as decimal, e.g., 0.07 for 7%)
     * @throws IllegalStateException if no RiskProfile is bound to the current thread
     */
    BigDecimal calculateInterestRate() {
        RiskProfile riskProfile = LoanContext.getCurrentRiskProfile();
        RiskLevel riskLevel = riskProfile.getRiskLevel();

        BigDecimal riskPremium = switch (riskLevel) {
            case LOW -> LOW_RISK_PREMIUM;
            case MEDIUM -> MEDIUM_RISK_PREMIUM;
            case HIGH -> HIGH_RISK_PREMIUM;
            case VERY_HIGH -> VERY_HIGH_RISK_PREMIUM;
        };

        return BASE_RATE.add(riskPremium);
    }

    /**
     * Calculate the interest rate for a specific RiskProfile without using ScopedValue.
     *
     * @param riskProfile the risk profile to evaluate
     * @return the calculated annual interest rate
     */
    BigDecimal calculateInterestRateFor(RiskProfile riskProfile) {
        RiskLevel riskLevel = riskProfile.getRiskLevel();

        BigDecimal riskPremium = switch (riskLevel) {
            case LOW -> LOW_RISK_PREMIUM;
            case MEDIUM -> MEDIUM_RISK_PREMIUM;
            case HIGH -> HIGH_RISK_PREMIUM;
            case VERY_HIGH -> VERY_HIGH_RISK_PREMIUM;
        };

        return BASE_RATE.add(riskPremium);
    }

    /**
     * Calculate the monthly payment for a loan.
     *
     * @param principal the loan amount
     * @param annualInterestRate the annual interest rate (as decimal)
     * @param termMonths the loan term in months
     * @return the monthly payment amount
     */
    BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualInterestRate, int termMonths) {
        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            // Zero-interest loan
            return principal.divide(BigDecimal.valueOf(termMonths), 2, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(12), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);

        // Monthly payment = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal numerator = monthlyRate.multiply(onePlusR.pow(termMonths));
        BigDecimal denominator = onePlusR.pow(termMonths).subtract(BigDecimal.ONE);

        return principal.multiply(numerator).divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate total interest payable over the loan term.
     *
     * @param principal the loan amount
     * @param annualInterestRate the annual interest rate
     * @param termMonths the loan term in months
     * @return the total interest amount
     */
    BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal annualInterestRate, int termMonths) {
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, annualInterestRate, termMonths);
        BigDecimal totalPayments = monthlyPayment.multiply(BigDecimal.valueOf(termMonths));
        return totalPayments.subtract(principal);
    }
}
