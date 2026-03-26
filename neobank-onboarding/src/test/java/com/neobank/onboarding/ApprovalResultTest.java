package com.neobank.onboarding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ApprovalResult record using JUnit 5.
 * Tests record construction and factory methods.
 */
@DisplayName("ApprovalResult Unit Tests")
class ApprovalResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create success result")
        void shouldCreateSuccessResult() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "User approved";

            // When
            ApprovalResult result = ApprovalResult.success(userId, message);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create failure result")
        void shouldCreateFailureResult() {
            // Given
            String reason = "Insufficient privileges";

            // When
            ApprovalResult result = ApprovalResult.failure(reason);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.userId()).isNull();
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
            UUID userId = UUID.randomUUID();
            boolean success = true;
            String message = "Success";

            // When
            ApprovalResult result = new ApprovalResult(userId, success, message);

            // Then
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.success()).isEqualTo(success);
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create result with null user ID")
        void shouldCreateResultWithNullUserId() {
            // Given
            boolean success = false;
            String message = "Failed";

            // When
            ApprovalResult result = new ApprovalResult(null, success, message);

            // Then
            assertThat(result.userId()).isNull();
            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should create result with empty message")
        void shouldCreateResultWithEmptyMessage() {
            // Given
            UUID userId = UUID.randomUUID();
            boolean success = true;
            String message = "";

            // When
            ApprovalResult result = new ApprovalResult(userId, success, message);

            // Then
            assertThat(result.message()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle long success message")
        void shouldHandleLongSuccessMessage() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "a".repeat(200);

            // When
            ApprovalResult result = ApprovalResult.success(userId, message);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle long failure message")
        void shouldHandleLongFailureMessage() {
            // Given
            String reason = "b".repeat(200);

            // When
            ApprovalResult result = ApprovalResult.failure(reason);

            // Then
            assertThat(result.message()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle message with special characters")
        void shouldHandleMessageWithSpecialCharacters() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "User approved: Status changed to ACTIVE!";

            // When
            ApprovalResult result = ApprovalResult.success(userId, message);

            // Then
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should handle message with unicode characters")
        void shouldHandleMessageWithUnicodeCharacters() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "用户已批准";

            // When
            ApprovalResult result = ApprovalResult.success(userId, message);

            // Then
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = new ApprovalResult(userId, true, null);

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
            UUID userId = UUID.randomUUID();
            String message = "Success";

            ApprovalResult result1 = ApprovalResult.success(userId, message);
            ApprovalResult result2 = ApprovalResult.success(userId, message);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should not equal result with different user ID")
        void shouldNotEqualResultWithDifferentUserId() {
            // Given
            String message = "Success";

            ApprovalResult result1 = ApprovalResult.success(UUID.randomUUID(), message);
            ApprovalResult result2 = ApprovalResult.success(UUID.randomUUID(), message);

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "Success";

            ApprovalResult result = ApprovalResult.success(userId, message);

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
            UUID userId = UUID.randomUUID();
            String message = "Success";

            ApprovalResult result = ApprovalResult.success(userId, message);

            // Then
            assertThat(result.toString()).contains("ApprovalResult");
            assertThat(result.toString()).contains("true");
        }

        @Test
        @DisplayName("Should include user ID in toString")
        void shouldIncludeUserIdInToString() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "Success";

            ApprovalResult result = ApprovalResult.success(userId, message);

            // Then
            assertThat(result.toString()).contains(userId.toString());
        }
    }
}
