package com.neobank.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DisbursementResult record using JUnit 5.
 * Tests record construction and factory methods.
 */
@DisplayName("DisbursementResult Unit Tests")
class DisbursementResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create success result")
        void shouldCreateSuccessResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            java.time.Instant disbursedAt = java.time.Instant.now();

            // When
            DisbursementResult result = DisbursementResult.success(loanId, disbursedAt);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.loanId()).isEqualTo(loanId);
            assertThat(result.disbursedAt()).isEqualTo(disbursedAt);
            assertThat(result.message()).isEqualTo("Loan disbursed successfully");
        }

        @Test
        @DisplayName("Should create failure result with reason")
        void shouldCreateFailureResultWithReason() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "Account not found";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.loanId()).isEqualTo(loanId);
            assertThat(result.message()).isEqualTo(reason);
            assertThat(result.disbursedAt()).isNull();
        }

        @Test
        @DisplayName("Should create failure result for loan not found")
        void shouldCreateFailureResultForLoanNotFound() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "Loan not found";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should create failure result for invalid status")
        void shouldCreateFailureResultForInvalidStatus() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "Loan must be in PENDING or APPROVED status";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.success()).isFalse();
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
            boolean success = true;
            String message = "Success";
            java.time.Instant disbursedAt = java.time.Instant.now();

            // When
            DisbursementResult result = new DisbursementResult(loanId, success, message, disbursedAt);

            // Then
            assertThat(result.loanId()).isEqualTo(loanId);
            assertThat(result.success()).isEqualTo(success);
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.disbursedAt()).isEqualTo(disbursedAt);
        }

        @Test
        @DisplayName("Should create result with null disbursed at")
        void shouldCreateResultWithNullDisbursedAt() {
            // Given
            UUID loanId = UUID.randomUUID();
            boolean success = false;
            String message = "Failed";

            // When
            DisbursementResult result = new DisbursementResult(loanId, success, message, null);

            // Then
            assertThat(result.disbursedAt()).isNull();
        }

        @Test
        @DisplayName("Should create result with empty message")
        void shouldCreateResultWithEmptyMessage() {
            // Given
            UUID loanId = UUID.randomUUID();
            boolean success = false;
            String message = "";

            // When
            DisbursementResult result = new DisbursementResult(loanId, success, message, null);

            // Then
            assertThat(result.message()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Success Flag")
    class SuccessFlagTests {

        @Test
        @DisplayName("Should return true for success result")
        void shouldReturnTrueForSuccessResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            java.time.Instant disbursedAt = java.time.Instant.now();

            // When
            DisbursementResult result = DisbursementResult.success(loanId, disbursedAt);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should return false for failure result")
        void shouldReturnFalseForFailureResult() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "Failed";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle long failure reason")
        void shouldHandleLongFailureReason() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "a".repeat(200);

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle failure reason with special characters")
        void shouldHandleFailureReasonWithSpecialCharacters() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "Error: Account API returned 500!";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle failure reason with unicode characters")
        void shouldHandleFailureReasonWithUnicodeCharacters() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "错误：账户不存在";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle empty failure reason")
        void shouldHandleEmptyFailureReason() {
            // Given
            UUID loanId = UUID.randomUUID();
            String reason = "";

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, reason);

            // Then
            assertThat(result.message()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null failure reason")
        void shouldHandleNullFailureReason() {
            // Given
            UUID loanId = UUID.randomUUID();

            // When
            DisbursementResult result = DisbursementResult.failure(loanId, null);

            // Then
            assertThat(result.message()).isNull();
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
            java.time.Instant disbursedAt = java.time.Instant.now();

            DisbursementResult result1 = DisbursementResult.success(loanId, disbursedAt);
            DisbursementResult result2 = DisbursementResult.success(loanId, disbursedAt);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should not equal result with different loan ID")
        void shouldNotEqualResultWithDifferentLoanId() {
            // Given
            java.time.Instant disbursedAt = java.time.Instant.now();

            DisbursementResult result1 = DisbursementResult.success(UUID.randomUUID(), disbursedAt);
            DisbursementResult result2 = DisbursementResult.success(UUID.randomUUID(), disbursedAt);

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID loanId = UUID.randomUUID();
            java.time.Instant disbursedAt = java.time.Instant.now();

            DisbursementResult result = DisbursementResult.success(loanId, disbursedAt);

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
            java.time.Instant disbursedAt = java.time.Instant.now();

            DisbursementResult result = DisbursementResult.success(loanId, disbursedAt);

            // Then
            assertThat(result.toString()).contains("DisbursementResult");
            assertThat(result.toString()).contains("true");
        }

        @Test
        @DisplayName("Should include loan ID in toString")
        void shouldIncludeLoanIdInToString() {
            // Given
            UUID loanId = UUID.randomUUID();
            java.time.Instant disbursedAt = java.time.Instant.now();

            DisbursementResult result = DisbursementResult.success(loanId, disbursedAt);

            // Then
            assertThat(result.toString()).contains(loanId.toString());
        }
    }
}
