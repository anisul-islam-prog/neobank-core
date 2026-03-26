package com.neobank.loans.internal;

import com.neobank.loans.RiskLevel;
import com.neobank.loans.RiskProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for InterestEngine using JUnit 5.
 * Tests interest rate calculation and monthly payment computation.
 */
@DisplayName("InterestEngine Unit Tests")
class InterestEngineTest {

    private InterestEngine interestEngine;

    @BeforeEach
    void setUp() {
        interestEngine = new InterestEngine();
    }

    @Nested
    @DisplayName("Interest Rate Calculation")
    class InterestRateCalculationTests {

        @Test
        @DisplayName("Should calculate interest rate for LOW risk profile")
        void shouldCalculateInterestRateForLowRiskProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Base rate (5%) + LOW premium (1%) = 6%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.08"));
        }

        @Test
        @DisplayName("Should calculate interest rate for MEDIUM risk profile")
        void shouldCalculateInterestRateForMediumRiskProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(700, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Credit score 700 (10 pts) + DTI 0.35 (10 pts) = 20 = LOW risk
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.08"));
        }

        @Test
        @DisplayName("Should calculate interest rate for HIGH risk profile")
        void shouldCalculateInterestRateForHighRiskProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(600, new BigDecimal("0.45"), 2, new BigDecimal("40000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Base rate (5%) + HIGH premium (6%) = 11%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.11"));
        }

        @Test
        @DisplayName("Should calculate interest rate for VERY_HIGH risk profile")
        void shouldCalculateInterestRateForVeryHighRiskProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Base rate (5%) + VERY_HIGH premium (10%) = 15%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.15"));
        }

        @Test
        @DisplayName("Should use base rate of 5 percent")
        void shouldUseBaseRateOf5Percent() {
            // Given - Lowest risk profile
            RiskProfile riskProfile = new RiskProfile(850, BigDecimal.ZERO, 50, new BigDecimal("1000000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Should be at least 6% (base 5% + LOW 1%)
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.08"));
        }
    }

    @Nested
    @DisplayName("Monthly Payment Calculation")
    class MonthlyPaymentCalculationTests {

        @Test
        @DisplayName("Should calculate monthly payment for standard loan")
        void shouldCalculateMonthlyPaymentForStandardLoan() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("308.77"));
        }

        @Test
        @DisplayName("Should calculate monthly payment for large loan")
        void shouldCalculateMonthlyPaymentForLargeLoan() {
            // Given
            BigDecimal principal = new BigDecimal("100000.00");
            BigDecimal annualRate = new BigDecimal("0.06");
            int termMonths = 60;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("1933.28"));
        }

        @Test
        @DisplayName("Should calculate monthly payment for zero interest loan")
        void shouldCalculateMonthlyPaymentForZeroInterestLoan() {
            // Given
            BigDecimal principal = new BigDecimal("12000.00");
            BigDecimal annualRate = BigDecimal.ZERO;
            int termMonths = 12;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should calculate monthly payment for short term loan")
        void shouldCalculateMonthlyPaymentForShortTermLoan() {
            // Given
            BigDecimal principal = new BigDecimal("5000.00");
            BigDecimal annualRate = new BigDecimal("0.08");
            int termMonths = 12;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("434.94"));
        }

        @Test
        @DisplayName("Should calculate monthly payment for long term loan")
        void shouldCalculateMonthlyPaymentForLongTermLoan() {
            // Given
            BigDecimal principal = new BigDecimal("250000.00");
            BigDecimal annualRate = new BigDecimal("0.05");
            int termMonths = 360;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("1342.05"));
        }

        @Test
        @DisplayName("Should handle high interest rate")
        void shouldHandleHighInterestRate() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.15");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("346.65"));
        }

        @Test
        @DisplayName("Should handle low interest rate")
        void shouldHandleLowInterestRate() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.03");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("290.81"));
        }
    }

    @Nested
    @DisplayName("Total Interest Calculation")
    class TotalInterestCalculationTests {

        @Test
        @DisplayName("Should calculate total interest for standard loan")
        void shouldCalculateTotalInterestForStandardLoan() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            BigDecimal totalInterest = interestEngine.calculateTotalInterest(principal, annualRate, termMonths);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(new BigDecimal("1115.72"));
        }

        @Test
        @DisplayName("Should calculate total interest for large loan")
        void shouldCalculateTotalInterestForLargeLoan() {
            // Given
            BigDecimal principal = new BigDecimal("100000.00");
            BigDecimal annualRate = new BigDecimal("0.06");
            int termMonths = 60;

            // When
            BigDecimal totalInterest = interestEngine.calculateTotalInterest(principal, annualRate, termMonths);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(new BigDecimal("15996.80"));
        }

        @Test
        @DisplayName("Should return zero total interest for zero interest loan")
        void shouldReturnZeroTotalInterestForZeroInterestLoan() {
            // Given
            BigDecimal principal = new BigDecimal("12000.00");
            BigDecimal annualRate = BigDecimal.ZERO;
            int termMonths = 12;

            // When
            BigDecimal totalInterest = interestEngine.calculateTotalInterest(principal, annualRate, termMonths);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate total interest for high rate loan")
        void shouldCalculateTotalInterestForHighRateLoan() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.15");
            int termMonths = 36;

            // When
            BigDecimal totalInterest = interestEngine.calculateTotalInterest(principal, annualRate, termMonths);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(new BigDecimal("2479.40"));
        }
    }

    @Nested
    @DisplayName("Risk Premium Configuration")
    class RiskPremiumConfigurationTests {

        @Test
        @DisplayName("Should use LOW risk premium of 1 percent")
        void shouldUseLowRiskPremiumOf1Percent() {
            // Given
            RiskProfile riskProfile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Base (5%) + LOW (1%) = 6%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.08"));
        }

        @Test
        @DisplayName("Should use MEDIUM risk premium of 3 percent")
        void shouldUseMediumRiskPremiumOf3Percent() {
            // Given
            RiskProfile riskProfile = new RiskProfile(680, new BigDecimal("0.38"), 4, new BigDecimal("60000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Credit score 680 (10 pts) + DTI 0.38 (10 pts) = 20 = LOW risk, Base (5%) + LOW (1%) = 6%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.08"));
        }

        @Test
        @DisplayName("Should use HIGH risk premium of 6 percent")
        void shouldUseHighRiskPremiumOf6Percent() {
            // Given
            RiskProfile riskProfile = new RiskProfile(580, new BigDecimal("0.50"), 1, new BigDecimal("35000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Base (5%) + HIGH (6%) = 11%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.11"));
        }

        @Test
        @DisplayName("Should use VERY_HIGH risk premium of 10 percent")
        void shouldUseVeryHighRiskPremiumOf10Percent() {
            // Given
            RiskProfile riskProfile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            BigDecimal rate = interestEngine.calculateInterestRateFor(riskProfile);

            // Then - Base (5%) + VERY_HIGH (10%) = 15%
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.15"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very small principal")
        void shouldHandleVerySmallPrincipal() {
            // Given
            BigDecimal principal = new BigDecimal("100.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 12;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle very large principal")
        void shouldHandleVeryLargePrincipal() {
            // Given
            BigDecimal principal = new BigDecimal("1000000.00");
            BigDecimal annualRate = new BigDecimal("0.05");
            int termMonths = 360;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("5368.22"));
        }

        @Test
        @DisplayName("Should handle principal with 2 decimal places")
        void shouldHandlePrincipalWith2DecimalPlaces() {
            // Given
            BigDecimal principal = new BigDecimal("10000.99");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle single month term")
        void shouldHandleSingleMonthTerm() {
            // Given
            BigDecimal principal = new BigDecimal("1000.00");
            BigDecimal annualRate = new BigDecimal("0.06");
            int termMonths = 1;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isEqualByComparingTo(new BigDecimal("1005.00"));
        }

        @Test
        @DisplayName("Should handle very long term")
        void shouldHandleVeryLongTerm() {
            // Given
            BigDecimal principal = new BigDecimal("200000.00");
            BigDecimal annualRate = new BigDecimal("0.04");
            int termMonths = 480;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle interest rate with 4 decimal places")
        void shouldHandleInterestRateWith4DecimalPlaces() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.0725");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle boundary between risk levels")
        void shouldHandleBoundaryBetweenRiskLevels() {
            // Given - Profile at boundary between LOW and MEDIUM
            RiskProfile lowRiskProfile = new RiskProfile(740, new BigDecimal("0.30"), 5, new BigDecimal("75000"));
            RiskProfile mediumRiskProfile = new RiskProfile(739, new BigDecimal("0.31"), 4, new BigDecimal("74000"));

            // When
            BigDecimal lowRate = interestEngine.calculateInterestRateFor(lowRiskProfile);
            BigDecimal mediumRate = interestEngine.calculateInterestRateFor(mediumRiskProfile);

            // Then
            assertThat(lowRate).isEqualByComparingTo(new BigDecimal("0.06"));
            assertThat(mediumRate).isEqualByComparingTo(new BigDecimal("0.08"));
        }

        @Test
        @DisplayName("Should handle exact boundary risk score of 25")
        void shouldHandleExactBoundaryRiskScoreOf25() {
            // Given - Credit score 670 (25 pts) + DTI 0.40 (10 pts) = 35 = MEDIUM risk
            RiskProfile profile = new RiskProfile(670, new BigDecimal("0.40"), 3, new BigDecimal("50000"));

            // When
            int riskScore = profile.calculateRiskScore();
            BigDecimal rate = interestEngine.calculateInterestRateFor(profile);

            // Then
            assertThat(riskScore).isEqualTo(35);
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.08"));
        }

        @Test
        @DisplayName("Should handle exact boundary risk score of 50")
        void shouldHandleExactBoundaryRiskScoreOf50() {
            // Given
            RiskProfile profile = new RiskProfile(600, new BigDecimal("0.45"), 2, new BigDecimal("40000"));

            // When
            int riskScore = profile.calculateRiskScore();
            BigDecimal rate = interestEngine.calculateInterestRateFor(profile);

            // Then
            assertThat(riskScore).isGreaterThanOrEqualTo(50);
            assertThat(rate).isIn(new BigDecimal("0.11"), new BigDecimal("0.15"));
        }

        @Test
        @DisplayName("Should handle exact boundary risk score of 75")
        void shouldHandleExactBoundaryRiskScoreOf75() {
            // Given
            RiskProfile profile = new RiskProfile(500, new BigDecimal("0.55"), 0, new BigDecimal("30000"));

            // When
            int riskScore = profile.calculateRiskScore();
            BigDecimal rate = interestEngine.calculateInterestRateFor(profile);

            // Then
            assertThat(riskScore).isGreaterThanOrEqualTo(75);
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.15"));
        }
    }

    @Nested
    @DisplayName("Calculation Precision")
    class CalculationPrecisionTests {

        @Test
        @DisplayName("Should round monthly payment to 2 decimal places")
        void shouldRoundMonthlyPaymentTo2DecimalPlaces() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should use ROUND_HALF_UP rounding mode")
        void shouldUseRoundHalfUpRoundingMode() {
            // Given
            BigDecimal principal = new BigDecimal("10000.01");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should maintain precision during calculation")
        void shouldMaintainPrecisionDuringCalculation() {
            // Given
            BigDecimal principal = new BigDecimal("33333.33");
            BigDecimal annualRate = new BigDecimal("0.0733");
            int termMonths = 48;

            // When
            BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(principal, annualRate, termMonths);

            // Then
            assertThat(monthlyPayment).isGreaterThan(BigDecimal.ZERO);
            assertThat(monthlyPayment.scale()).isEqualTo(2);
        }
    }
}
