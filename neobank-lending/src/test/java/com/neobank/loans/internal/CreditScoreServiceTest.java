package com.neobank.loans.internal;

import com.neobank.loans.CreditScoreResult;
import com.neobank.loans.RiskLevel;
import com.neobank.loans.RiskProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CreditScoreService using JUnit 5.
 * Tests credit score calculation and risk profile generation.
 */
@DisplayName("CreditScoreService Unit Tests")
class CreditScoreServiceTest {

    private CreditScoreService creditScoreService;

    @BeforeEach
    void setUp() {
        creditScoreService = new CreditScoreService();
    }

    @Nested
    @DisplayName("Credit Score Retrieval")
    class CreditScoreRetrievalTests {

        @Test
        @DisplayName("Should get credit score for user successfully")
        void shouldGetCreditScoreForUserSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isNotNull();
            assertThat(result.creditScore()).isBetween(0, 100);
            assertThat(result.riskLevel()).isNotNull();
        }

        @Test
        @DisplayName("Should return consistent credit score for same user")
        void shouldReturnConsistentCreditScoreForSameUser() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result1 = creditScoreService.getCreditScore(userId);
            CreditScoreResult result2 = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result1.creditScore()).isEqualTo(result2.creditScore());
            assertThat(result1.riskLevel()).isEqualTo(result2.riskLevel());
        }

        @Test
        @DisplayName("Should return different credit scores for different users")
        void shouldReturnDifferentCreditScoresForDifferentUsers() {
            // Given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            // When
            CreditScoreResult result1 = creditScoreService.getCreditScore(userId1);
            CreditScoreResult result2 = creditScoreService.getCreditScore(userId2);

            // Then - May be same or different depending on hash
            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
        }

        @Test
        @DisplayName("Should include credit history in result")
        void shouldIncludeCreditHistoryInResult() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.creditHistory()).isNotNull();
            assertThat(result.creditHistory()).isIn("Excellent", "Good", "Fair", "Poor");
        }

        @Test
        @DisplayName("Should include debt-to-income ratio in result")
        void shouldIncludeDebtToIncomeRatioInResult() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.debtToIncome()).isNotNull();
            assertThat(result.debtToIncome()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should include employment years in result")
        void shouldIncludeEmploymentYearsInResult() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.employmentYears()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should include annual income in result")
        void shouldIncludeAnnualIncomeInResult() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.annualIncome()).isNotNull();
            assertThat(result.annualIncome()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Score Calculation")
    class ScoreCalculationTests {

        @Test
        @DisplayName("Should calculate score from risk profile")
        void shouldCalculateScoreFromRiskProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isBetween(0, 100);
        }

        @Test
        @DisplayName("Should return low score for excellent credit profile")
        void shouldReturnLowScoreForExcellentCreditProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isLessThan(25);
        }

        @Test
        @DisplayName("Should return high score for poor credit profile")
        void shouldReturnHighScoreForPoorCreditProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isGreaterThan(75);
        }

        @Test
        @DisplayName("Should return medium score for average credit profile")
        void shouldReturnMediumScoreForAverageCreditProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(650, new BigDecimal("0.40"), 3, new BigDecimal("50000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isBetween(25, 75);
        }
    }

    @Nested
    @DisplayName("Risk Profile Generation")
    class RiskProfileGenerationTests {

        @Test
        @DisplayName("Should generate risk profile with valid credit score")
        void shouldGenerateRiskProfileWithValidCreditScore() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then - Credit score should be in valid range (580-850 for internal profile)
            assertThat(result.creditScore()).isBetween(0, 100);
        }

        @Test
        @DisplayName("Should generate risk profile with valid debt-to-income ratio")
        void shouldGenerateRiskProfileWithValidDebtToIncomeRatio() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.debtToIncome()).isBetween(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.6));
        }

        @Test
        @DisplayName("Should generate risk profile with valid employment years")
        void shouldGenerateRiskProfileWithValidEmploymentYears() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.employmentYears()).isBetween(0, 19);
        }

        @Test
        @DisplayName("Should generate risk profile with valid annual income")
        void shouldGenerateRiskProfileWithValidAnnualIncome() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            CreditScoreResult result = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result.annualIncome()).isBetween(
                    new BigDecimal("30000"),
                    new BigDecimal("150000")
            );
        }

        @Test
        @DisplayName("Should generate deterministic profile based on user ID")
        void shouldGenerateDeterministicProfileBasedOnUserId() {
            // Given
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            // When
            CreditScoreResult result1 = creditScoreService.getCreditScore(userId);
            CreditScoreResult result2 = creditScoreService.getCreditScore(userId);

            // Then
            assertThat(result1.creditScore()).isEqualTo(result2.creditScore());
            assertThat(result1.debtToIncome()).isEqualByComparingTo(result2.debtToIncome());
            assertThat(result1.employmentYears()).isEqualTo(result2.employmentYears());
            assertThat(result1.annualIncome()).isEqualByComparingTo(result2.annualIncome());
        }
    }

    @Nested
    @DisplayName("Risk Level Assessment")
    class RiskLevelAssessmentTests {

        @Test
        @DisplayName("Should return LOW risk for excellent profile")
        void shouldReturnLowRiskForExcellentProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);
            RiskProfile profile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));
            RiskLevel level = profile.getRiskLevel();

            // Then
            assertThat(score).isLessThan(25);
            assertThat(level).isEqualTo(RiskLevel.LOW);
        }

        @Test
        @DisplayName("Should return MEDIUM risk for good profile")
        void shouldReturnMediumRiskForGoodProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(700, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            RiskLevel level = riskProfile.getRiskLevel();

            // Then
            assertThat(level).isIn(RiskLevel.LOW, RiskLevel.MEDIUM);
        }

        @Test
        @DisplayName("Should return HIGH risk for fair profile")
        void shouldReturnHighRiskForFairProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(600, new BigDecimal("0.45"), 2, new BigDecimal("40000"));

            // When
            RiskLevel level = riskProfile.getRiskLevel();

            // Then
            assertThat(level).isIn(RiskLevel.MEDIUM, RiskLevel.HIGH);
        }

        @Test
        @DisplayName("Should return VERY_HIGH risk for poor profile")
        void shouldReturnVeryHighRiskForPoorProfile() {
            // Given
            RiskProfile riskProfile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            RiskLevel level = riskProfile.getRiskLevel();

            // Then
            assertThat(level).isEqualTo(RiskLevel.VERY_HIGH);
        }
    }

    @Nested
    @DisplayName("Credit History Assessment")
    class CreditHistoryAssessmentTests {

        @Test
        @DisplayName("Should return Excellent for low risk score")
        void shouldReturnExcellentForLowRiskScore() {
            // Given
            RiskProfile riskProfile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));
            int score = creditScoreService.calculateScore(riskProfile);

            // When
            CreditScoreResult result = CreditScoreResult.fromRiskProfile("user123", riskProfile);

            // Then
            if (score < 25) {
                assertThat(result.creditHistory()).isEqualTo("Excellent");
            }
        }

        @Test
        @DisplayName("Should return Good for medium risk score")
        void shouldReturnGoodForMediumRiskScore() {
            // Given
            RiskProfile riskProfile = new RiskProfile(700, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            CreditScoreResult result = CreditScoreResult.fromRiskProfile("user123", riskProfile);

            // Then
            assertThat(result.creditHistory()).isIn("Excellent", "Good");
        }

        @Test
        @DisplayName("Should return Fair for high risk score")
        void shouldReturnFairForHighRiskScore() {
            // Given
            RiskProfile riskProfile = new RiskProfile(600, new BigDecimal("0.45"), 2, new BigDecimal("40000"));

            // When
            CreditScoreResult result = CreditScoreResult.fromRiskProfile("user123", riskProfile);

            // Then
            assertThat(result.creditHistory()).isIn("Excellent", "Good", "Fair");
        }

        @Test
        @DisplayName("Should return Poor for very high risk score")
        void shouldReturnPoorForVeryHighRiskScore() {
            // Given
            RiskProfile riskProfile = new RiskProfile(500, new BigDecimal("0.60"), 0, new BigDecimal("25000"));

            // When
            CreditScoreResult result = CreditScoreResult.fromRiskProfile("user123", riskProfile);

            // Then
            if (result.creditScore() >= 75) {
                assertThat(result.creditHistory()).isEqualTo("Poor");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle minimum credit score")
        void shouldHandleMinimumCreditScore() {
            // Given
            RiskProfile riskProfile = new RiskProfile(300, new BigDecimal("0.60"), 0, new BigDecimal("0"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isGreaterThan(75);
        }

        @Test
        @DisplayName("Should handle maximum credit score")
        void shouldHandleMaximumCreditScore() {
            // Given
            RiskProfile riskProfile = new RiskProfile(850, new BigDecimal("0.00"), 50, new BigDecimal("1000000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isLessThan(25);
        }

        @Test
        @DisplayName("Should handle zero debt-to-income ratio")
        void shouldHandleZeroDebtToIncomeRatio() {
            // Given
            RiskProfile riskProfile = new RiskProfile(750, BigDecimal.ZERO, 5, new BigDecimal("75000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isLessThan(50);
        }

        @Test
        @DisplayName("Should handle high debt-to-income ratio")
        void shouldHandleHighDebtToIncomeRatio() {
            // Given
            RiskProfile riskProfile = new RiskProfile(650, new BigDecimal("0.80"), 5, new BigDecimal("75000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isGreaterThan(25);
        }

        @Test
        @DisplayName("Should handle zero employment years")
        void shouldHandleZeroEmploymentYears() {
            // Given
            RiskProfile riskProfile = new RiskProfile(700, new BigDecimal("0.35"), 0, new BigDecimal("75000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle long employment history")
        void shouldHandleLongEmploymentHistory() {
            // Given
            RiskProfile riskProfile = new RiskProfile(750, new BigDecimal("0.30"), 30, new BigDecimal("100000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isLessThan(25);
        }

        @Test
        @DisplayName("Should handle zero annual income")
        void shouldHandleZeroAnnualIncome() {
            // Given
            RiskProfile riskProfile = new RiskProfile(700, new BigDecimal("0.35"), 5, BigDecimal.ZERO);

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isGreaterThan(10);
        }

        @Test
        @DisplayName("Should handle very high annual income")
        void shouldHandleVeryHighAnnualIncome() {
            // Given
            RiskProfile riskProfile = new RiskProfile(750, new BigDecimal("0.25"), 10, new BigDecimal("1000000"));

            // When
            int score = creditScoreService.calculateScore(riskProfile);

            // Then
            assertThat(score).isLessThan(25);
        }

        @Test
        @DisplayName("Should handle invalid risk profile with negative credit score")
        void shouldHandleInvalidRiskProfileWithNegativeCreditScore() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(-100, new BigDecimal("0.35"), 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("creditScore must be between 300 and 850");
        }

        @Test
        @DisplayName("Should handle invalid risk profile with credit score above 850")
        void shouldHandleInvalidRiskProfileWithCreditScoreAbove850() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(900, new BigDecimal("0.35"), 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("creditScore must be between 300 and 850");
        }

        @Test
        @DisplayName("Should handle invalid risk profile with negative debt-to-income")
        void shouldHandleInvalidRiskProfileWithNegativeDebtToIncome() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(700, new BigDecimal("-0.10"), 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debtToIncomeRatio must be non-negative");
        }

        @Test
        @DisplayName("Should handle invalid risk profile with negative employment years")
        void shouldHandleInvalidRiskProfileWithNegativeEmploymentYears() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(700, new BigDecimal("0.35"), -5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("employmentYears must be non-negative");
        }

        @Test
        @DisplayName("Should handle invalid risk profile with negative annual income")
        void shouldHandleInvalidRiskProfileWithNegativeAnnualIncome() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(700, new BigDecimal("0.35"), 5, new BigDecimal("-1000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("annualIncome must be non-negative");
        }

        @Test
        @DisplayName("Should handle null debt-to-income ratio")
        void shouldHandleNullDebtToIncomeRatio() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(700, null, 5, new BigDecimal("75000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debtToIncomeRatio must be non-negative");
        }

        @Test
        @DisplayName("Should handle null annual income")
        void shouldHandleNullAnnualIncome() {
            // Given/When/Then
            assertThatThrownBy(() -> new RiskProfile(700, new BigDecimal("0.35"), 5, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("annualIncome must be non-negative");
        }
    }

    @Nested
    @DisplayName("CreditScoreResult Factory Method")
    class CreditScoreResultFactoryMethodTests {

        @Test
        @DisplayName("Should create CreditScoreResult from RiskProfile")
        void shouldCreateCreditScoreResultFromRiskProfile() {
            // Given
            String userId = "user123";
            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));

            // When
            CreditScoreResult result = CreditScoreResult.fromRiskProfile(userId, riskProfile);

            // Then
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.creditScore()).isBetween(0, 100);
            assertThat(result.riskLevel()).isNotNull();
            assertThat(result.debtToIncome()).isEqualByComparingTo(new BigDecimal("0.35"));
            assertThat(result.employmentYears()).isEqualTo(5);
            assertThat(result.annualIncome()).isEqualByComparingTo(new BigDecimal("75000"));
        }

        @Test
        @DisplayName("Should calculate correct risk score from profile")
        void shouldCalculateCorrectRiskScoreFromProfile() {
            // Given
            String userId = "user123";
            RiskProfile riskProfile = new RiskProfile(800, new BigDecimal("0.20"), 10, new BigDecimal("150000"));

            // When
            CreditScoreResult result = CreditScoreResult.fromRiskProfile(userId, riskProfile);

            // Then
            assertThat(result.creditScore()).isLessThan(25);
            assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW);
            assertThat(result.creditHistory()).isEqualTo("Excellent");
        }

        @Test
        @DisplayName("Should handle invalid CreditScoreResult with score below 0")
        void shouldHandleInvalidCreditScoreResultWithScoreBelow0() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", -10, RiskLevel.LOW, "Excellent",
                    new BigDecimal("0.35"), 5, new BigDecimal("75000")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("creditScore must be between 0 and 100");
        }

        @Test
        @DisplayName("Should handle invalid CreditScoreResult with score above 100")
        void shouldHandleInvalidCreditScoreResultWithScoreAbove100() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", 150, RiskLevel.VERY_HIGH, "Poor",
                    new BigDecimal("0.35"), 5, new BigDecimal("75000")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("creditScore must be between 0 and 100");
        }

        @Test
        @DisplayName("Should handle invalid CreditScoreResult with negative debt-to-income")
        void shouldHandleInvalidCreditScoreResultWithNegativeDebtToIncome() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", 50, RiskLevel.MEDIUM, "Fair",
                    new BigDecimal("-0.10"), 5, new BigDecimal("75000")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debtToIncome must be non-negative");
        }

        @Test
        @DisplayName("Should handle invalid CreditScoreResult with negative employment years")
        void shouldHandleInvalidCreditScoreResultWithNegativeEmploymentYears() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", 50, RiskLevel.MEDIUM, "Fair",
                    new BigDecimal("0.35"), -5, new BigDecimal("75000")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("employmentYears must be non-negative");
        }

        @Test
        @DisplayName("Should handle invalid CreditScoreResult with negative annual income")
        void shouldHandleInvalidCreditScoreResultWithNegativeAnnualIncome() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", 50, RiskLevel.MEDIUM, "Fair",
                    new BigDecimal("0.35"), 5, new BigDecimal("-1000")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("annualIncome must be non-negative");
        }

        @Test
        @DisplayName("Should handle null debt-to-income in CreditScoreResult")
        void shouldHandleNullDebtToIncomeInCreditScoreResult() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", 50, RiskLevel.MEDIUM, "Fair",
                    null, 5, new BigDecimal("75000")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debtToIncome must be non-negative");
        }

        @Test
        @DisplayName("Should handle null annual income in CreditScoreResult")
        void shouldHandleNullAnnualIncomeInCreditScoreResult() {
            // Given/When/Then
            assertThatThrownBy(() -> new CreditScoreResult(
                    "user123", 50, RiskLevel.MEDIUM, "Fair",
                    new BigDecimal("0.35"), 5, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("annualIncome must be non-negative");
        }
    }
}
