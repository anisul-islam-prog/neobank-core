package com.neobank.core.transfers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TransactionResult sealed interface using JUnit 5.
 * Tests Success, Failure, and Pending record types.
 */
@DisplayName("TransactionResult Unit Tests")
class TransactionResultTest {

    @Nested
    @DisplayName("Success Record")
    class SuccessRecordTests {

        @Test
        @DisplayName("Should create Success result with message")
        void shouldCreateSuccessResultWithMessage() {
            // Given
            String message = "Transfer completed successfully";

            // When
            TransactionResult.Success result = new TransactionResult.Success(message);

            // Then
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when message is null")
        void shouldThrowExceptionWhenMessageIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransactionResult.Success(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("message must not be null");
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            // When
            TransactionResult.Success result = new TransactionResult.Success("");

            // Then
            assertThat(result.message()).isEmpty();
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should handle long message")
        void shouldHandleLongMessage() {
            // Given
            String longMessage = "a".repeat(200);

            // When
            TransactionResult.Success result = new TransactionResult.Success(longMessage);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should equal another Success with same message")
        void shouldEqualAnotherSuccessWithSameMessage() {
            // Given
            String message = "Transfer completed successfully";

            TransactionResult.Success result1 = new TransactionResult.Success(message);
            TransactionResult.Success result2 = new TransactionResult.Success(message);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            TransactionResult.Success result = new TransactionResult.Success("Test message");

            // Then
            assertThat(result.hashCode()).isNotNull();
            assertThat(result.hashCode()).isEqualTo(result.hashCode());
        }
    }

    @Nested
    @DisplayName("Failure Record")
    class FailureRecordTests {

        @Test
        @DisplayName("Should create Failure result with message")
        void shouldCreateFailureResultWithMessage() {
            // Given
            String message = "Insufficient balance";

            // When
            TransactionResult.Failure result = new TransactionResult.Failure(message);

            // Then
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when message is null")
        void shouldThrowExceptionWhenMessageIsNullForFailure() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransactionResult.Failure(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("message must not be null");
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessageForFailure() {
            // When
            TransactionResult.Failure result = new TransactionResult.Failure("");

            // Then
            assertThat(result.message()).isEmpty();
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should handle long message")
        void shouldHandleLongMessageForFailure() {
            // Given
            String longMessage = "a".repeat(200);

            // When
            TransactionResult.Failure result = new TransactionResult.Failure(longMessage);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should equal another Failure with same message")
        void shouldEqualAnotherFailureWithSameMessage() {
            // Given
            String message = "Insufficient balance";

            TransactionResult.Failure result1 = new TransactionResult.Failure(message);
            TransactionResult.Failure result2 = new TransactionResult.Failure(message);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCodeForFailure() {
            // Given
            TransactionResult.Failure result = new TransactionResult.Failure("Test failure");

            // Then
            assertThat(result.hashCode()).isNotNull();
            assertThat(result.hashCode()).isEqualTo(result.hashCode());
        }
    }

    @Nested
    @DisplayName("Pending Record")
    class PendingRecordTests {

        @Test
        @DisplayName("Should create Pending result with message and authorizationId")
        void shouldCreatePendingResultWithMessageAndAuthorizationId() {
            // Given
            String message = "Transfer requires approval";
            UUID authorizationId = UUID.randomUUID();

            // When
            TransactionResult.Pending result = new TransactionResult.Pending(message, authorizationId);

            // Then
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.authorizationId()).isEqualTo(authorizationId);
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when message is null")
        void shouldThrowExceptionWhenMessageIsNullForPending() {
            // Given
            UUID authorizationId = UUID.randomUUID();

            // Given/When/Then
            assertThatThrownBy(() -> new TransactionResult.Pending(null, authorizationId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("message must not be null");
        }

        @Test
        @DisplayName("Should throw exception when authorizationId is null")
        void shouldThrowExceptionWhenAuthorizationIdIsNull() {
            // Given
            String message = "Transfer requires approval";

            // Given/When/Then
            assertThatThrownBy(() -> new TransactionResult.Pending(message, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("authorizationId must not be null");
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessageForPending() {
            // Given
            UUID authorizationId = UUID.randomUUID();

            // When
            TransactionResult.Pending result = new TransactionResult.Pending("", authorizationId);

            // Then
            assertThat(result.message()).isEmpty();
            assertThat(result.authorizationId()).isEqualTo(authorizationId);
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should handle long message")
        void shouldHandleLongMessageForPending() {
            // Given
            String longMessage = "a".repeat(200);
            UUID authorizationId = UUID.randomUUID();

            // When
            TransactionResult.Pending result = new TransactionResult.Pending(longMessage, authorizationId);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should equal another Pending with same fields")
        void shouldEqualAnotherPendingWithSameFields() {
            // Given
            String message = "Transfer requires approval";
            UUID authorizationId = UUID.randomUUID();

            TransactionResult.Pending result1 = new TransactionResult.Pending(message, authorizationId);
            TransactionResult.Pending result2 = new TransactionResult.Pending(message, authorizationId);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCodeForPending() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            TransactionResult.Pending result = new TransactionResult.Pending("Test pending", authorizationId);

            // Then
            assertThat(result.hashCode()).isNotNull();
            assertThat(result.hashCode()).isEqualTo(result.hashCode());
        }
    }

    @Nested
    @DisplayName("Polymorphic Behavior")
    class PolymorphicBehaviorTests {

        @Test
        @DisplayName("Should return true for isSuccess on Success")
        void shouldReturnTrueForIsSuccessOnSuccess() {
            // Given
            TransactionResult.Success success = new TransactionResult.Success("Success");

            // Then
            assertThat(success.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isSuccess on Failure")
        void shouldReturnFalseForIsSuccessOnFailure() {
            // Given
            TransactionResult.Failure failure = new TransactionResult.Failure("Failure");

            // Then
            assertThat(failure.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should return false for isSuccess on Pending")
        void shouldReturnFalseForIsSuccessOnPending() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            TransactionResult.Pending pending = new TransactionResult.Pending("Pending", authorizationId);

            // Then
            assertThat(pending.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should handle as Success in switch expression")
        void shouldHandleAsSuccessInSwitchExpression() {
            // Given
            TransactionResult result = new TransactionResult.Success("Transfer completed");

            // When/Then
            if (result instanceof TransactionResult.Success success) {
                assertThat(success.message()).isEqualTo("Transfer completed");
            }
        }

        @Test
        @DisplayName("Should handle as Failure in switch expression")
        void shouldHandleAsFailureInSwitchExpression() {
            // Given
            TransactionResult result = new TransactionResult.Failure("Insufficient balance");

            // When/Then
            if (result instanceof TransactionResult.Failure failure) {
                assertThat(failure.message()).isEqualTo("Insufficient balance");
            }
        }

        @Test
        @DisplayName("Should handle as Pending in switch expression")
        void shouldHandleAsPendingInSwitchExpression() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            TransactionResult result = new TransactionResult.Pending("Requires approval", authorizationId);

            // When/Then
            if (result instanceof TransactionResult.Pending pending) {
                assertThat(pending.authorizationId()).isEqualTo(authorizationId);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle message with special characters")
        void shouldHandleMessageWithSpecialCharacters() {
            // Given
            String message = "Transfer failed: Error code #123!";

            // When
            TransactionResult.Failure result = new TransactionResult.Failure(message);

            // Then
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should handle message with unicode characters")
        void shouldHandleMessageWithUnicodeCharacters() {
            // Given
            String message = "转账失败：余额不足";

            // When
            TransactionResult.Failure result = new TransactionResult.Failure(message);

            // Then
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should handle different authorization IDs")
        void shouldHandleDifferentAuthorizationIds() {
            // Given
            UUID authorizationId1 = UUID.randomUUID();
            UUID authorizationId2 = UUID.randomUUID();

            // When
            TransactionResult.Pending result1 = new TransactionResult.Pending("Pending", authorizationId1);
            TransactionResult.Pending result2 = new TransactionResult.Pending("Pending", authorizationId2);

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should not equal Success with different type")
        void shouldNotEqualSuccessWithDifferentType() {
            // Given
            TransactionResult.Success success = new TransactionResult.Success("Success");
            TransactionResult.Failure failure = new TransactionResult.Failure("Failure");

            // Then
            assertThat(success).isNotEqualTo(failure);
        }

        @Test
        @DisplayName("Should not equal Pending with different authorizationId")
        void shouldNotEqualPendingWithDifferentAuthorizationId() {
            // Given
            String message = "Pending";

            TransactionResult.Pending result1 = new TransactionResult.Pending(message, UUID.randomUUID());
            TransactionResult.Pending result2 = new TransactionResult.Pending(message, UUID.randomUUID());

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format for Success")
        void shouldHaveCorrectToStringFormatForSuccess() {
            // Given
            TransactionResult.Success result = new TransactionResult.Success("Transfer completed");

            // Then
            assertThat(result.toString()).contains("Success");
            assertThat(result.toString()).contains("Transfer completed");
        }

        @Test
        @DisplayName("Should have correct toString format for Failure")
        void shouldHaveCorrectToStringFormatForFailure() {
            // Given
            TransactionResult.Failure result = new TransactionResult.Failure("Insufficient balance");

            // Then
            assertThat(result.toString()).contains("Failure");
            assertThat(result.toString()).contains("Insufficient balance");
        }

        @Test
        @DisplayName("Should have correct toString format for Pending")
        void shouldHaveCorrectToStringFormatForPending() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            TransactionResult.Pending result = new TransactionResult.Pending("Requires approval", authorizationId);

            // Then
            assertThat(result.toString()).contains("Pending");
            assertThat(result.toString()).contains("Requires approval");
        }
    }
}
