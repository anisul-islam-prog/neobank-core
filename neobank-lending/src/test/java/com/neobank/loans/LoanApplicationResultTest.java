package com.neobank.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LoanApplicationResult record using JUnit 5.
 * Tests record construction and factory methods.
 */
@DisplayName("LoanApplicationResult Unit Tests")
class LoanApplicationResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create pending result")
        void shouldCreatePendingResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            LoanApplicationResult result = LoanApplicationResult.pending(loanId, monthlyPayment);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(result.loanId()).isEqualTo(loanId);
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
            assertThat(result.message()).contains("review");
        }

        @Test
        @DisplayName("Should create approved result")
        void shouldCreateApprovedResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            LoanApplicationResult result = LoanApplicationResult.approved(loanId, monthlyPayment);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.APPROVED);
            assertThat(result.loanId()).isEqualTo(loanId);
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
            assertThat(result.message()).isEqualTo("Loan approved");
        }

        @Test
        @DisplayName("Should create rejected result")
        void shouldCreateRejectedResult() {
            // Given
            String reason = "Credit score too low";

            // When
            LoanApplicationResult result = LoanApplicationResult.rejected(reason);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.REJECTED);
            assertThat(result.loanId()).isNull();
            assertThat(result.calculatedMonthlyPayment()).isNull();
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should create failure result")
        void shouldCreateFailureResult() {
            // Given
            String reason = "Application processing failed";

            // When
            LoanApplicationResult result = LoanApplicationResult.failure(reason);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(result.loanId()).isNull();
            assertThat(result.calculatedMonthlyPayment()).isNull();
            assertThat(result.message()).isEqualTo(reason);
        }
    }

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create result with all fields")
        void shouldCreateResultWithAllFields() {
            // Given
            UUID loanId = UUID.randomUUID();
            ApplicationStatus status = ApplicationStatus.APPROVED;
            String message = "Loan approved";
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            LoanApplicationResult result = new LoanApplicationResult(loanId, status, message, monthlyPayment);

            // Then
            assertThat(result.loanId()).isEqualTo(loanId);
            assertThat(result.status()).isEqualTo(status);
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
        }

        @Test
        @DisplayName("Should create result with null loan ID")
        void shouldCreateResultWithNullLoanId() {
            // Given
            ApplicationStatus status = ApplicationStatus.REJECTED;
            String message = "Rejected";

            // When
            LoanApplicationResult result = new LoanApplicationResult(null, status, message, null);

            // Then
            assertThat(result.loanId()).isNull();
            assertThat(result.calculatedMonthlyPayment()).isNull();
        }

        @Test
        @DisplayName("Should create result with null monthly payment")
        void shouldCreateResultWithNullMonthlyPayment() {
            // Given
            UUID loanId = UUID.randomUUID();
            ApplicationStatus status = ApplicationStatus.PENDING;
            String message = "Pending review";

            // When
            LoanApplicationResult result = new LoanApplicationResult(loanId, status, message, null);

            // Then
            assertThat(result.calculatedMonthlyPayment()).isNull();
        }
    }

    @Nested
    @DisplayName("Status Values")
    class StatusValuesTests {

        @Test
        @DisplayName("Should have PENDING status for pending result")
        void shouldHavePendingStatusForPendingResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            LoanApplicationResult result = LoanApplicationResult.pending(loanId, monthlyPayment);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should have APPROVED status for approved result")
        void shouldHaveApprovedStatusForApprovedResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            LoanApplicationResult result = LoanApplicationResult.approved(loanId, monthlyPayment);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.APPROVED);
        }

        @Test
        @DisplayName("Should have REJECTED status for rejected result")
        void shouldHaveRejectedStatusForRejectedResult() {
            // Given
            String reason = "Credit score too low";

            // When
            LoanApplicationResult result = LoanApplicationResult.rejected(reason);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.REJECTED);
        }

        @Test
        @DisplayName("Should have PENDING status for failure result")
        void shouldHavePendingStatusForFailureResult() {
            // Given
            String reason = "Processing failed";

            // When
            LoanApplicationResult result = LoanApplicationResult.failure(reason);

            // Then
            assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero monthly payment")
        void shouldHandleZeroMonthlyPayment() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = BigDecimal.ZERO;

            // When
            LoanApplicationResult result = LoanApplicationResult.approved(loanId, monthlyPayment);

            // Then
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle very large monthly payment")
        void shouldHandleVeryLargeMonthlyPayment() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("50000.00");

            // When
            LoanApplicationResult result = LoanApplicationResult.approved(loanId, monthlyPayment);

            // Then
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
        }

        @Test
        @DisplayName("Should handle monthly payment with 2 decimal places")
        void shouldHandleMonthlyPaymentWith2DecimalPlaces() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            LoanApplicationResult result = LoanApplicationResult.approved(loanId, monthlyPayment);

            // Then
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
        }

        @Test
        @DisplayName("Should handle long rejection reason")
        void shouldHandleLongRejectionReason() {
            // Given
            String reason = "a".repeat(200);

            // When
            LoanApplicationResult result = LoanApplicationResult.rejected(reason);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle rejection reason with special characters")
        void shouldHandleRejectionReasonWithSpecialCharacters() {
            // Given
            String reason = "Credit score < 580: High risk!";

            // When
            LoanApplicationResult result = LoanApplicationResult.rejected(reason);

            // Then
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle rejection reason with unicode characters")
        void shouldHandleRejectionReasonWithUnicodeCharacters() {
            // Given
            String reason = "信用评分太低";

            // When
            LoanApplicationResult result = LoanApplicationResult.rejected(reason);

            // Then
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle empty rejection reason")
        void shouldHandleEmptyRejectionReason() {
            // Given
            String reason = "";

            // When
            LoanApplicationResult result = LoanApplicationResult.rejected(reason);

            // Then
            assertThat(result.message()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another result with same fields")
        void shouldEqualAnotherResultWithSameFields() {
            // Given
            UUID loanId = UUID.randomUUID();
            ApplicationStatus status = ApplicationStatus.APPROVED;
            String message = "Approved";
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            LoanApplicationResult result1 = new LoanApplicationResult(loanId, status, message, monthlyPayment);
            LoanApplicationResult result2 = new LoanApplicationResult(loanId, status, message, monthlyPayment);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should not equal result with different loan ID")
        void shouldNotEqualResultWithDifferentLoanId() {
            // Given
            ApplicationStatus status = ApplicationStatus.APPROVED;
            String message = "Approved";
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            LoanApplicationResult result1 = new LoanApplicationResult(
                    UUID.randomUUID(), status, message, monthlyPayment
            );
            LoanApplicationResult result2 = new LoanApplicationResult(
                    UUID.randomUUID(), status, message, monthlyPayment
            );

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID loanId = UUID.randomUUID();
            ApplicationStatus status = ApplicationStatus.APPROVED;
            String message = "Approved";
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            LoanApplicationResult result = new LoanApplicationResult(loanId, status, message, monthlyPayment);

            // Then
            assertThat(result.hashCode()).isNotNull();
            assertThat(result.hashCode()).isEqualTo(result.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            UUID loanId = UUID.randomUUID();
            ApplicationStatus status = ApplicationStatus.APPROVED;
            String message = "Approved";
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            LoanApplicationResult result = new LoanApplicationResult(loanId, status, message, monthlyPayment);

            // Then
            assertThat(result.toString()).contains("LoanApplicationResult");
            assertThat(result.toString()).contains(status.toString());
        }

        @Test
        @DisplayName("Should include monthly payment in toString")
        void shouldIncludeMonthlyPaymentInToString() {
            // Given
            UUID loanId = UUID.randomUUID();
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            LoanApplicationResult result = LoanApplicationResult.approved(loanId, monthlyPayment);

            // Then
            assertThat(result.toString()).contains("308.77");
        }
    }
}
