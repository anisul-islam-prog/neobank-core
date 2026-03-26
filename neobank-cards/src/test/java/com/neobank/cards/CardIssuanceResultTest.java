package com.neobank.cards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CardIssuanceResult record using JUnit 5.
 * Tests record construction and factory methods.
 */
@DisplayName("CardIssuanceResult Unit Tests")
class CardIssuanceResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create success result")
        void shouldCreateSuccessResult() {
            // Given
            UUID cardId = UUID.randomUUID();
            String maskedNumber = "****-****-****-0366";

            // When
            CardIssuanceResult result = CardIssuanceResult.success(cardId, maskedNumber);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.cardId()).isEqualTo(cardId);
            assertThat(result.cardNumberMasked()).isEqualTo(maskedNumber);
            assertThat(result.message()).isEqualTo("Card issued successfully");
        }

        @Test
        @DisplayName("Should create failure result")
        void shouldCreateFailureResult() {
            // Given
            String reason = "Card issuance failed";

            // When
            CardIssuanceResult result = CardIssuanceResult.failure(reason);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.cardId()).isNull();
            assertThat(result.cardNumberMasked()).isNull();
            assertThat(result.message()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should create failure result with custom message")
        void shouldCreateFailureResultWithCustomMessage() {
            // Given
            String reason = "Account owner must have ACTIVE status";

            // When
            CardIssuanceResult result = CardIssuanceResult.failure(reason);

            // Then
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
            UUID cardId = UUID.randomUUID();
            boolean success = true;
            String message = "Card issued successfully";
            String cardNumberMasked = "****-****-****-0366";

            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    cardId, success, message, cardNumberMasked
            );

            // Then
            assertThat(result.cardId()).isEqualTo(cardId);
            assertThat(result.success()).isEqualTo(success);
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.cardNumberMasked()).isEqualTo(cardNumberMasked);
        }

        @Test
        @DisplayName("Should create result with null card ID")
        void shouldCreateResultWithNullCardId() {
            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    null, false, "Failed", null
            );

            // Then
            assertThat(result.cardId()).isNull();
            assertThat(result.cardNumberMasked()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null card ID in success result")
        void shouldHandleNullCardIdInSuccessResult() {
            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    null, true, "Success", "****-****-****-0366"
            );

            // Then
            assertThat(result.cardId()).isNull();
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    UUID.randomUUID(), true, null, "****-****-****-0366"
            );

            // Then
            assertThat(result.message()).isNull();
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    UUID.randomUUID(), true, "", "****-****-****-0366"
            );

            // Then
            assertThat(result.message()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null masked number")
        void shouldHandleNullMaskedNumber() {
            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    UUID.randomUUID(), true, "Success", null
            );

            // Then
            assertThat(result.cardNumberMasked()).isNull();
        }

        @Test
        @DisplayName("Should handle empty masked number")
        void shouldHandleEmptyMaskedNumber() {
            // When
            CardIssuanceResult result = new CardIssuanceResult(
                    UUID.randomUUID(), true, "Success", ""
            );

            // Then
            assertThat(result.cardNumberMasked()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another result with same fields")
        void shouldEqualAnotherResultWithSameFields() {
            // Given
            UUID cardId = UUID.randomUUID();

            CardIssuanceResult result1 = CardIssuanceResult.success(cardId, "****-****-****-0366");

            CardIssuanceResult result2 = CardIssuanceResult.success(cardId, "****-****-****-0366");

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should not equal result with different card ID")
        void shouldNotEqualResultWithDifferentCardId() {
            // Given
            CardIssuanceResult result1 = CardIssuanceResult.success(
                    UUID.randomUUID(), "****-****-****-0366"
            );

            CardIssuanceResult result2 = CardIssuanceResult.success(
                    UUID.randomUUID(), "****-****-****-0366"
            );

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardIssuanceResult result = CardIssuanceResult.success(cardId, "****-****-****-0366");

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
            UUID cardId = UUID.randomUUID();
            CardIssuanceResult result = CardIssuanceResult.success(cardId, "****-****-****-0366");

            // Then
            assertThat(result.toString()).contains("CardIssuanceResult");
            assertThat(result.toString()).contains("****-****-****-0366");
        }

        @Test
        @DisplayName("Should include success flag in toString")
        void shouldIncludeSuccessFlagInToString() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardIssuanceResult result = CardIssuanceResult.success(cardId, "****-****-****-0366");

            // Then
            assertThat(result.toString()).contains("true");
        }
    }
}
