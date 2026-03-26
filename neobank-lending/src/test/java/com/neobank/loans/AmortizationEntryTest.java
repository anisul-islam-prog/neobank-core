package com.neobank.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AmortizationEntry record using JUnit 5.
 * Tests record construction and field accessors.
 */
@DisplayName("AmortizationEntry Unit Tests")
class AmortizationEntryTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create entry with all fields")
        void shouldCreateEntryWithAllFields() {
            // Given
            int paymentNumber = 1;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(paymentNumber);
            assertThat(entry.paymentAmount()).isEqualByComparingTo(paymentAmount);
            assertThat(entry.principalPortion()).isEqualByComparingTo(principalPortion);
            assertThat(entry.interestPortion()).isEqualByComparingTo(interestPortion);
            assertThat(entry.remainingBalance()).isEqualByComparingTo(remainingBalance);
        }

        @Test
        @DisplayName("Should create entry for first payment")
        void shouldCreateEntryForFirstPayment() {
            // Given
            int paymentNumber = 1;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should create entry for final payment")
        void shouldCreateEntryForFinalPayment() {
            // Given
            int paymentNumber = 36;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("306.98");
            BigDecimal interestPortion = new BigDecimal("1.79");
            BigDecimal remainingBalance = BigDecimal.ZERO;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(36);
            assertThat(entry.remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should create entry with zero remaining balance")
        void shouldCreateEntryWithZeroRemainingBalance() {
            // Given
            int paymentNumber = 36;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("306.98");
            BigDecimal interestPortion = new BigDecimal("1.79");
            BigDecimal remainingBalance = BigDecimal.ZERO;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry.remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Field Validation")
    class FieldValidationTests {

        @Test
        @DisplayName("Should handle payment number of 1")
        void shouldHandlePaymentNumberOf1() {
            // Given
            int paymentNumber = 1;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, new BigDecimal("308.77"), new BigDecimal("250.44"),
                    new BigDecimal("58.33"), new BigDecimal("9749.56")
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle large payment number")
        void shouldHandleLargePaymentNumber() {
            // Given
            int paymentNumber = 360;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, new BigDecimal("1342.05"), new BigDecimal("1335.05"),
                    new BigDecimal("7.00"), BigDecimal.ZERO
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(360);
        }

        @Test
        @DisplayName("Should handle zero payment number")
        void shouldHandleZeroPaymentNumber() {
            // Given
            int paymentNumber = 0;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, new BigDecimal("308.77"), new BigDecimal("250.44"),
                    new BigDecimal("58.33"), new BigDecimal("10000.00")
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle negative payment number")
        void shouldHandleNegativePaymentNumber() {
            // Given
            int paymentNumber = -1;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, new BigDecimal("308.77"), new BigDecimal("250.44"),
                    new BigDecimal("58.33"), new BigDecimal("10250.44")
            );

            // Then
            assertThat(entry.paymentNumber()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero payment amount")
        void shouldHandleZeroPaymentAmount() {
            // Given
            BigDecimal paymentAmount = BigDecimal.ZERO;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, paymentAmount, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("10000.00")
            );

            // Then
            assertThat(entry.paymentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero principal portion")
        void shouldHandleZeroPrincipalPortion() {
            // Given
            BigDecimal principalPortion = BigDecimal.ZERO;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, new BigDecimal("308.77"), principalPortion, new BigDecimal("308.77"), new BigDecimal("10000.00")
            );

            // Then
            assertThat(entry.principalPortion()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero interest portion")
        void shouldHandleZeroInterestPortion() {
            // Given
            BigDecimal interestPortion = BigDecimal.ZERO;

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, new BigDecimal("308.77"), new BigDecimal("308.77"), interestPortion, new BigDecimal("9691.23")
            );

            // Then
            assertThat(entry.interestPortion()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle payment amount with 2 decimal places")
        void shouldHandlePaymentAmountWith2DecimalPlaces() {
            // Given
            BigDecimal paymentAmount = new BigDecimal("308.77");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, paymentAmount, new BigDecimal("250.44"), new BigDecimal("58.33"), new BigDecimal("9749.56")
            );

            // Then
            assertThat(entry.paymentAmount()).isEqualByComparingTo(paymentAmount);
        }

        @Test
        @DisplayName("Should handle principal portion with 2 decimal places")
        void shouldHandlePrincipalPortionWith2DecimalPlaces() {
            // Given
            BigDecimal principalPortion = new BigDecimal("250.44");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, new BigDecimal("308.77"), principalPortion, new BigDecimal("58.33"), new BigDecimal("9749.56")
            );

            // Then
            assertThat(entry.principalPortion()).isEqualByComparingTo(principalPortion);
        }

        @Test
        @DisplayName("Should handle interest portion with 2 decimal places")
        void shouldHandleInterestPortionWith2DecimalPlaces() {
            // Given
            BigDecimal interestPortion = new BigDecimal("58.33");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, new BigDecimal("308.77"), new BigDecimal("250.44"), interestPortion, new BigDecimal("9749.56")
            );

            // Then
            assertThat(entry.interestPortion()).isEqualByComparingTo(interestPortion);
        }

        @Test
        @DisplayName("Should handle remaining balance with 2 decimal places")
        void shouldHandleRemainingBalanceWith2DecimalPlaces() {
            // Given
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, new BigDecimal("308.77"), new BigDecimal("250.44"), new BigDecimal("58.33"), remainingBalance
            );

            // Then
            assertThat(entry.remainingBalance()).isEqualByComparingTo(remainingBalance);
        }

        @Test
        @DisplayName("Should handle very large payment amount")
        void shouldHandleVeryLargePaymentAmount() {
            // Given
            BigDecimal paymentAmount = new BigDecimal("50000.00");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, paymentAmount, new BigDecimal("49000.00"), new BigDecimal("1000.00"), new BigDecimal("950000.00")
            );

            // Then
            assertThat(entry.paymentAmount()).isEqualByComparingTo(paymentAmount);
        }

        @Test
        @DisplayName("Should handle very large remaining balance")
        void shouldHandleVeryLargeRemainingBalance() {
            // Given
            BigDecimal remainingBalance = new BigDecimal("950000.00");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, new BigDecimal("50000.00"), new BigDecimal("49000.00"), new BigDecimal("1000.00"), remainingBalance
            );

            // Then
            assertThat(entry.remainingBalance()).isEqualByComparingTo(remainingBalance);
        }

        @Test
        @DisplayName("Should handle principal plus interest equals payment")
        void shouldHandlePrincipalPlusInterestEqualsPayment() {
            // Given
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");

            // When
            AmortizationEntry entry = new AmortizationEntry(
                    1, paymentAmount, principalPortion, interestPortion, new BigDecimal("9749.56")
            );

            // Then
            BigDecimal sum = entry.principalPortion().add(entry.interestPortion());
            assertThat(sum).isEqualByComparingTo(entry.paymentAmount());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another entry with same fields")
        void shouldEqualAnotherEntryWithSameFields() {
            // Given
            int paymentNumber = 1;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            AmortizationEntry entry1 = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );
            AmortizationEntry entry2 = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry1).isEqualTo(entry2);
        }

        @Test
        @DisplayName("Should not equal entry with different payment number")
        void shouldNotEqualEntryWithDifferentPaymentNumber() {
            // Given
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            AmortizationEntry entry1 = new AmortizationEntry(
                    1, paymentAmount, principalPortion, interestPortion, remainingBalance
            );
            AmortizationEntry entry2 = new AmortizationEntry(
                    2, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry1).isNotEqualTo(entry2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            int paymentNumber = 1;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry.hashCode()).isNotNull();
            assertThat(entry.hashCode()).isEqualTo(entry.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            int paymentNumber = 1;
            BigDecimal paymentAmount = new BigDecimal("308.77");
            BigDecimal principalPortion = new BigDecimal("250.44");
            BigDecimal interestPortion = new BigDecimal("58.33");
            BigDecimal remainingBalance = new BigDecimal("9749.56");

            AmortizationEntry entry = new AmortizationEntry(
                    paymentNumber, paymentAmount, principalPortion, interestPortion, remainingBalance
            );

            // Then
            assertThat(entry.toString()).contains("AmortizationEntry");
            assertThat(entry.toString()).contains(String.valueOf(paymentNumber));
        }

        @Test
        @DisplayName("Should include payment amount in toString")
        void shouldIncludePaymentAmountInToString() {
            // Given
            BigDecimal paymentAmount = new BigDecimal("308.77");

            AmortizationEntry entry = new AmortizationEntry(
                    1, paymentAmount, new BigDecimal("250.44"), new BigDecimal("58.33"), new BigDecimal("9749.56")
            );

            // Then
            assertThat(entry.toString()).contains("308.77");
        }
    }
}
