package com.neobank.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for RiskProfile record using JUnit 5.
 * Tests record construction, validation, and risk calculations.
 */
@DisplayName("RiskProfile Unit Tests")
class RiskProfileTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create RiskProfile with all fields")
        void shouldCreateRiskProfileWithAllFields() {
            // Given
            int creditScore = 720;
            BigDecimal debtToIncomeRatio = new BigDecimal("0.35");
            int employmentYears = 5;
            BigDecimal annualIncome = new BigDecimal("75000");

            // When
            RiskProfile profile = new RiskProfile(creditScore, debtToIncomeRatio, employmentYears, annualIncome);

            // Then
            assertThat(profile.creditScore()).isEqualTo(creditScore);
            assertThat(profile.debtToIncomeRatio()).isEqualByComparingTo(debtToIncomeRatio);
            assertThat(profile.employmentYears()).isEqualTo(employmentYears);
            assertThat(profile.annualIncome()).isEqualByComparingTo(annualIncome);
        }

        @Test
        @DisplayName("Should create RiskProfile with minimum credit score")
        void shouldCreateRiskProfileWithMinimumCreditScore() {
            // Given
            int creditScore = 300;

            // When
            RiskProfile profile = new RiskProfile(creditScore, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.creditScore()).isEqualTo(300);
        }

        @Test
        @DisplayName("Should create RiskProfile with maximum credit score")
        void shouldCreateRiskProfileWithMaximumCreditScore() {
            // Given
            int creditScore = 850;

            // When
            RiskProfile profile = new RiskProfile(creditScore, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.creditScore()).isEqualTo(850);
        }

        @Test
        @DisplayName("Should create RiskProfile with zero debt-to-income ratio")
        void shouldCreateRiskProfileWithZeroDebtToIncomeRatio() {
            // Given
            BigDecimal debtToIncomeRatio = BigDecimal.ZERO;

            // When
            RiskProfile profile = new RiskProfile(720, debtToIncomeRatio, 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.debtToIncomeRatio()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should create RiskProfile with zero employment years")
        void shouldCreateRiskProfileWithZeroEmploymentYears() {
            // Given
            int employmentYears = 0;

            // When
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), employmentYears, new BigDecimal("75000"));

            // Then
            assertThat(profile.employmentYears()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create RiskProfile with zero annual income")
        void shouldCreateRiskProfileWithZeroAnnualIncome() {
            // Given
            BigDecimal annualIncome = BigDecimal.ZERO;

            // When
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, annualIncome);

            // Then
            assertThat(profile.annualIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when credit score is below 300")
        void shouldThrowExceptionWhenCreditScoreIsBelow300() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(299, new BigDecimal("0.35"), 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("creditScore must be between 300 and 850");
        }

        @Test
        @DisplayName("Should throw exception when credit score is above 850")
        void shouldThrowExceptionWhenCreditScoreIsAbove850() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(851, new BigDecimal("0.35"), 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("creditScore must be between 300 and 850");
        }

        @Test
        @DisplayName("Should throw exception when debt-to-income ratio is null")
        void shouldThrowExceptionWhenDebtToIncomeRatioIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(720, null, 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debtToIncomeRatio must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when debt-to-income ratio is negative")
        void shouldThrowExceptionWhenDebtToIncomeRatioIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(720, new BigDecimal("-0.10"), 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debtToIncomeRatio must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when employment years is negative")
        void shouldThrowExceptionWhenEmploymentYearsIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(720, new BigDecimal("0.35"), -1, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("employmentYears must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when annual income is null")
        void shouldThrowExceptionWhenAnnualIncomeIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(720, new BigDecimal("0.35"), 5, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("annualIncome must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when annual income is negative")
        void shouldThrowExceptionWhenAnnualIncomeIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("-1000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("annualIncome must be non-negative");
        }
    }

    @Nested
    @DisplayName("Risk Score Calculation")
    class RiskScoreCalculationTests {

        @Test
        @DisplayName("Should calculate low risk score for excellent profile")
        void shouldCalculateLowRiskScoreForExcellentProfile() {
            // Given
            RiskProfile profile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then
            assertThat(riskScore).isLessThan(25);
        }

        @Test
        @DisplayName("Should calculate medium risk score for good profile")
        void shouldCalculateMediumRiskScoreForGoodProfile() {
            // Given
            RiskProfile profile = new RiskProfile(700, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then
            assertThat(riskScore).isBetween(25, 49);
        }

        @Test
        @DisplayName("Should calculate high risk score for fair profile")
        void shouldCalculateHighRiskScoreForFairProfile() {
            // Given
            RiskProfile profile = new RiskProfile(600, new BigDecimal("0.45"), 2, new BigDecimal("40000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then
            assertThat(riskScore).isBetween(50, 74);
        }

        @Test
        @DisplayName("Should calculate very high risk score for poor profile")
        void shouldCalculateVeryHighRiskScoreForPoorProfile() {
            // Given
            RiskProfile profile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then
            assertThat(riskScore).isGreaterThanOrEqualTo(75);
        }

        @Test
        @DisplayName("Should calculate risk score with credit score component")
        void shouldCalculateRiskScoreWithCreditScoreComponent() {
            // Given - Low credit score
            RiskProfile profile = new RiskProfile(550, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should have 40 points from credit score
            assertThat(riskScore).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("Should calculate risk score with DTI component")
        void shouldCalculateRiskScoreWithDtiComponent() {
            // Given - High DTI
            RiskProfile profile = new RiskProfile(800, new BigDecimal("0.55"), 10, new BigDecimal("150000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should have 30 points from DTI
            assertThat(riskScore).isGreaterThanOrEqualTo(30);
        }

        @Test
        @DisplayName("Should calculate risk score with employment component")
        void shouldCalculateRiskScoreWithEmploymentComponent() {
            // Given - No employment history
            RiskProfile profile = new RiskProfile(800, new BigDecimal("0.20"), 0, new BigDecimal("150000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should have 15 points from employment
            assertThat(riskScore).isGreaterThanOrEqualTo(15);
        }

        @Test
        @DisplayName("Should calculate risk score with income component")
        void shouldCalculateRiskScoreWithIncomeComponent() {
            // Given - Low income
            RiskProfile profile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("25000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should have 15 points from income
            assertThat(riskScore).isGreaterThanOrEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Risk Level Determination")
    class RiskLevelDeterminationTests {

        @Test
        @DisplayName("Should return LOW risk level for score below 25")
        void shouldReturnLowRiskLevelForScoreBelow25() {
            // Given
            RiskProfile profile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            RiskLevel level = profile.getRiskLevel();

            // Then
            assertThat(level).isEqualTo(RiskLevel.LOW);
        }

        @Test
        @DisplayName("Should return MEDIUM risk level for score 25-49")
        void shouldReturnMediumRiskLevelForScore25To49() {
            // Given
            RiskProfile profile = new RiskProfile(680, new BigDecimal("0.38"), 4, new BigDecimal("60000"));

            // When
            RiskLevel level = profile.getRiskLevel();

            // Then
            assertThat(level).isIn(RiskLevel.LOW, RiskLevel.MEDIUM);
        }

        @Test
        @DisplayName("Should return HIGH risk level for score 50-74")
        void shouldReturnHighRiskLevelForScore50To74() {
            // Given
            RiskProfile profile = new RiskProfile(600, new BigDecimal("0.45"), 2, new BigDecimal("40000"));

            // When
            RiskLevel level = profile.getRiskLevel();

            // Then
            assertThat(level).isIn(RiskLevel.MEDIUM, RiskLevel.HIGH);
        }

        @Test
        @DisplayName("Should return VERY_HIGH risk level for score 75 or above")
        void shouldReturnVeryHighRiskLevelForScore75OrAbove() {
            // Given
            RiskProfile profile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            RiskLevel level = profile.getRiskLevel();

            // Then
            assertThat(level).isEqualTo(RiskLevel.VERY_HIGH);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle boundary credit score of 300")
        void shouldHandleBoundaryCreditScoreOf300() {
            // Given
            RiskProfile profile = new RiskProfile(300, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.creditScore()).isEqualTo(300);
        }

        @Test
        @DisplayName("Should handle boundary credit score of 850")
        void shouldHandleBoundaryCreditScoreOf850() {
            // Given
            RiskProfile profile = new RiskProfile(850, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.creditScore()).isEqualTo(850);
        }

        @Test
        @DisplayName("Should handle very high debt-to-income ratio")
        void shouldHandleVeryHighDebtToIncomeRatio() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.99"), 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.debtToIncomeRatio()).isEqualByComparingTo(new BigDecimal("0.99"));
        }

        @Test
        @DisplayName("Should handle very long employment history")
        void shouldHandleVeryLongEmploymentHistory() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 50, new BigDecimal("75000"));

            // Then
            assertThat(profile.employmentYears()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should handle very high annual income")
        void shouldHandleVeryHighAnnualIncome() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("1000000"));

            // Then
            assertThat(profile.annualIncome()).isEqualByComparingTo(new BigDecimal("1000000"));
        }

        @Test
        @DisplayName("Should handle debt-to-income ratio with 4 decimal places")
        void shouldHandleDebtToIncomeRatioWith4DecimalPlaces() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.3567"), 5, new BigDecimal("75000"));

            // Then
            assertThat(profile.debtToIncomeRatio()).isEqualByComparingTo(new BigDecimal("0.3567"));
        }

        @Test
        @DisplayName("Should handle annual income with 2 decimal places")
        void shouldHandleAnnualIncomeWith2DecimalPlaces() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000.99"));

            // Then
            assertThat(profile.annualIncome()).isEqualByComparingTo(new BigDecimal("75000.99"));
        }

        @Test
        @DisplayName("Should handle boundary DTI of 0.3")
        void shouldHandleBoundaryDtiOf0_3() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.30"), 5, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for DTI
            assertThat(riskScore).isLessThan(40);
        }

        @Test
        @DisplayName("Should handle boundary DTI of 0.4")
        void shouldHandleBoundaryDtiOf0_4() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.40"), 5, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should add 10 points for DTI
            assertThat(riskScore).isGreaterThanOrEqualTo(10);
        }

        @Test
        @DisplayName("Should handle boundary DTI of 0.5")
        void shouldHandleBoundaryDtiOf0_5() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.50"), 5, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should add 20 points for DTI
            assertThat(riskScore).isGreaterThanOrEqualTo(20);
        }

        @Test
        @DisplayName("Should handle boundary employment of 1 year")
        void shouldHandleBoundaryEmploymentOf1Year() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 1, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for employment
            assertThat(riskScore).isLessThan(15);
        }

        @Test
        @DisplayName("Should handle boundary employment of 3 years")
        void shouldHandleBoundaryEmploymentOf3Years() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 3, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for employment
            assertThat(riskScore).isLessThan(15);
        }

        @Test
        @DisplayName("Should handle boundary employment of 5 years")
        void shouldHandleBoundaryEmploymentOf5Years() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for employment
            assertThat(riskScore).isLessThan(15);
        }

        @Test
        @DisplayName("Should handle boundary income of 30000")
        void shouldHandleBoundaryIncomeOf30000() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("30000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for income
            assertThat(riskScore).isLessThan(15);
        }

        @Test
        @DisplayName("Should handle boundary income of 50000")
        void shouldHandleBoundaryIncomeOf50000() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("50000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for income
            assertThat(riskScore).isLessThan(15);
        }

        @Test
        @DisplayName("Should handle boundary income of 75000")
        void shouldHandleBoundaryIncomeOf75000() {
            // Given
            RiskProfile profile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            int riskScore = profile.calculateRiskScore();

            // Then - Should not add points for income
            assertThat(riskScore).isLessThan(15);
        }
    }
}
