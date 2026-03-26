package com.neobank.loans.internal;

import com.neobank.loans.AmortizationEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AmortizationSchedule using JUnit 5.
 * Tests amortization schedule generation and calculations.
 */
@DisplayName("AmortizationSchedule Unit Tests")
class AmortizationScheduleTest {

    private AmortizationSchedule amortizationSchedule;

    @BeforeEach
    void setUp() {
        amortizationSchedule = new AmortizationSchedule();
    }

    @Nested
    @DisplayName("Schedule Generation")
    class ScheduleGenerationTests {

        @Test
        @DisplayName("Should generate schedule for standard loan")
        void shouldGenerateScheduleForStandardLoan() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(36);
            assertThat(schedule.get(0).paymentNumber()).isEqualTo(1);
            assertThat(schedule.get(35).paymentNumber()).isEqualTo(36);
        }

        @Test
        @DisplayName("Should generate schedule with correct first payment")
        void shouldGenerateScheduleWithCorrectFirstPayment() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            AmortizationEntry firstPayment = schedule.get(0);
            assertThat(firstPayment.paymentAmount()).isEqualByComparingTo(new BigDecimal("308.77"));
            assertThat(firstPayment.interestPortion()).isEqualByComparingTo(new BigDecimal("58.33"));
            assertThat(firstPayment.principalPortion()).isEqualByComparingTo(new BigDecimal("250.44"));
        }

        @Test
        @DisplayName("Should generate schedule with zero final balance")
        void shouldGenerateScheduleWithZeroFinalBalance() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            AmortizationEntry lastPayment = schedule.get(35);
            assertThat(lastPayment.remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should generate schedule with decreasing balance")
        void shouldGenerateScheduleWithDecreasingBalance() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            BigDecimal previousBalance = principal;
            for (AmortizationEntry entry : schedule) {
                assertThat(entry.remainingBalance()).isLessThan(previousBalance);
                previousBalance = entry.remainingBalance();
            }
        }

        @Test
        @DisplayName("Should generate schedule with increasing principal portion")
        void shouldGenerateScheduleWithIncreasingPrincipalPortion() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            BigDecimal previousPrincipal = BigDecimal.ZERO;
            for (AmortizationEntry entry : schedule) {
                assertThat(entry.principalPortion()).isGreaterThan(previousPrincipal);
                previousPrincipal = entry.principalPortion();
            }
        }

        @Test
        @DisplayName("Should generate schedule with decreasing interest portion")
        void shouldGenerateScheduleWithDecreasingInterestPortion() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            BigDecimal previousInterest = schedule.get(0).interestPortion().add(BigDecimal.ONE);
            for (AmortizationEntry entry : schedule) {
                assertThat(entry.interestPortion()).isLessThan(previousInterest);
                previousInterest = entry.interestPortion();
            }
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
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal totalInterest = amortizationSchedule.calculateTotalInterest(schedule);

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
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal totalInterest = amortizationSchedule.calculateTotalInterest(schedule);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(new BigDecimal("15996.80"));
        }

        @Test
        @DisplayName("Should return zero total interest for zero rate loan")
        void shouldReturnZeroTotalInterestForZeroRateLoan() {
            // Given
            BigDecimal principal = new BigDecimal("12000.00");
            BigDecimal annualRate = BigDecimal.ZERO;
            int termMonths = 12;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal totalInterest = amortizationSchedule.calculateTotalInterest(schedule);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate total interest for short term loan")
        void shouldCalculateTotalInterestForShortTermLoan() {
            // Given
            BigDecimal principal = new BigDecimal("5000.00");
            BigDecimal annualRate = new BigDecimal("0.08");
            int termMonths = 12;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal totalInterest = amortizationSchedule.calculateTotalInterest(schedule);

            // Then
            assertThat(totalInterest).isEqualByComparingTo(new BigDecimal("219.28"));
        }
    }

    @Nested
    @DisplayName("Remaining Balance Calculation")
    class RemainingBalanceCalculationTests {

        @Test
        @DisplayName("Should get remaining balance after specific payment")
        void shouldGetRemainingBalanceAfterSpecificPayment() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal remainingBalance = amortizationSchedule.getRemainingBalance(schedule, 12);

            // Then
            assertThat(remainingBalance).isGreaterThan(BigDecimal.ZERO);
            assertThat(remainingBalance).isLessThan(principal);
        }

        @Test
        @DisplayName("Should return original principal for payment 0")
        void shouldReturnOriginalPrincipalForPayment0() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal remainingBalance = amortizationSchedule.getRemainingBalance(schedule, 0);

            // Then
            assertThat(remainingBalance).isEqualByComparingTo(principal);
        }

        @Test
        @DisplayName("Should return zero for final payment")
        void shouldReturnZeroForFinalPayment() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal remainingBalance = amortizationSchedule.getRemainingBalance(schedule, 36);

            // Then
            assertThat(remainingBalance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return null for payment number out of range")
        void shouldReturnNullForPaymentNumberOutOfRange() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal remainingBalance = amortizationSchedule.getRemainingBalance(schedule, 50);

            // Then
            assertThat(remainingBalance).isNull();
        }

        @Test
        @DisplayName("Should return null for negative payment number")
        void shouldReturnNullForNegativePaymentNumber() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal remainingBalance = amortizationSchedule.getRemainingBalance(schedule, -5);

            // Then
            assertThat(remainingBalance).isNull();
        }

        @Test
        @DisplayName("Should return balance at halfway point")
        void shouldReturnBalanceAtHalfwayPoint() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // When
            BigDecimal remainingBalance = amortizationSchedule.getRemainingBalance(schedule, 18);

            // Then
            assertThat(remainingBalance).isGreaterThan(BigDecimal.ZERO);
            assertThat(remainingBalance).isLessThan(principal);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should generate schedule for very small loan")
        void shouldGenerateScheduleForVerySmallLoan() {
            // Given
            BigDecimal principal = new BigDecimal("100.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 12;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(12);
            assertThat(schedule.get(11).remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should generate schedule for very large loan")
        void shouldGenerateScheduleForVeryLargeLoan() {
            // Given
            BigDecimal principal = new BigDecimal("1000000.00");
            BigDecimal annualRate = new BigDecimal("0.05");
            int termMonths = 360;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(360);
            assertThat(schedule.get(359).remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should generate schedule for single month loan")
        void shouldGenerateScheduleForSingleMonthLoan() {
            // Given
            BigDecimal principal = new BigDecimal("1000.00");
            BigDecimal annualRate = new BigDecimal("0.06");
            int termMonths = 1;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(1);
            assertThat(schedule.get(0).remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should generate schedule for zero interest loan")
        void shouldGenerateScheduleForZeroInterestLoan() {
            // Given
            BigDecimal principal = new BigDecimal("1200.00");
            BigDecimal annualRate = BigDecimal.ZERO;
            int termMonths = 12;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(12);
            assertThat(schedule.get(0).interestPortion()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(schedule.get(0).principalPortion()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should generate schedule for high interest rate")
        void shouldGenerateScheduleForHighInterestRate() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.15");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(36);
            assertThat(schedule.get(0).interestPortion()).isGreaterThan(new BigDecimal("100"));
        }

        @Test
        @DisplayName("Should generate schedule for low interest rate")
        void shouldGenerateScheduleForLowInterestRate() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.03");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(36);
            assertThat(schedule.get(0).interestPortion()).isLessThan(new BigDecimal("30"));
        }

        @Test
        @DisplayName("Should handle principal with 2 decimal places")
        void shouldHandlePrincipalWith2DecimalPlaces() {
            // Given
            BigDecimal principal = new BigDecimal("10000.99");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(36);
            assertThat(schedule.get(35).remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle interest rate with 4 decimal places")
        void shouldHandleInterestRateWith4DecimalPlaces() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.0725");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(36);
        }

        @Test
        @DisplayName("Should handle very long term loan")
        void shouldHandleVeryLongTermLoan() {
            // Given
            BigDecimal principal = new BigDecimal("200000.00");
            BigDecimal annualRate = new BigDecimal("0.04");
            int termMonths = 480;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            assertThat(schedule).hasSize(480);
            assertThat(schedule.get(479).remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle final payment adjustment")
        void shouldHandleFinalPaymentAdjustment() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            AmortizationEntry lastPayment = schedule.get(35);
            assertThat(lastPayment.principalPortion()).isEqualByComparingTo(lastPayment.remainingBalance().add(lastPayment.principalPortion()));
        }
    }

    @Nested
    @DisplayName("Schedule Validation")
    class ScheduleValidationTests {

        @Test
        @DisplayName("Should verify payment amount is constant")
        void shouldVerifyPaymentAmountIsConstant() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            BigDecimal firstPayment = schedule.get(0).paymentAmount();
            for (int i = 1; i < schedule.size() - 1; i++) {
                assertThat(schedule.get(i).paymentAmount()).isEqualByComparingTo(firstPayment);
            }
        }

        @Test
        @DisplayName("Should verify principal plus interest equals payment")
        void shouldVerifyPrincipalPlusInterestEqualsPayment() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            for (int i = 0; i < schedule.size() - 1; i++) {
                AmortizationEntry entry = schedule.get(i);
                BigDecimal sum = entry.principalPortion().add(entry.interestPortion());
                assertThat(sum).isEqualByComparingTo(entry.paymentAmount());
            }
        }

        @Test
        @DisplayName("Should verify total principal equals original principal")
        void shouldVerifyTotalPrincipalEqualsOriginalPrincipal() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal annualRate = new BigDecimal("0.07");
            int termMonths = 36;

            // When
            List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(principal, annualRate, termMonths);

            // Then
            BigDecimal totalPrincipal = schedule.stream()
                    .map(AmortizationEntry::principalPortion)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(totalPrincipal).isEqualByComparingTo(principal);
        }
    }
}
