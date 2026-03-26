package com.neobank.cards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CardIssuanceRequest record using JUnit 5.
 * Tests record construction and validation.
 */
@DisplayName("CardIssuanceRequest Unit Tests")
class CardIssuanceRequestTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            BigDecimal spendingLimit = new BigDecimal("5000.00");
            String cardholderName = "John Doe";

            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, cardType, spendingLimit, cardholderName
            );

            // Then
            assertThat(request.accountId()).isEqualTo(accountId);
            assertThat(request.cardType()).isEqualTo(cardType);
            assertThat(request.spendingLimit()).isEqualByComparingTo(spendingLimit);
            assertThat(request.cardholderName()).isEqualTo(cardholderName);
        }

        @Test
        @DisplayName("Should create request with null spending limit")
        void shouldCreateRequestWithNullSpendingLimit() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            String cardholderName = "John Doe";

            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, cardType, null, cardholderName
            );

            // Then
            assertThat(request.spendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should create request with PHYSICAL card type")
        void shouldCreateRequestWithPhysicalCardType() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.PHYSICAL;
            BigDecimal spendingLimit = new BigDecimal("10000.00");
            String cardholderName = "John Doe";

            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, cardType, spendingLimit, cardholderName
            );

            // Then
            assertThat(request.cardType()).isEqualTo(CardType.PHYSICAL);
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when account ID is null")
        void shouldThrowExceptionWhenAccountIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    null, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("accountId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when card type is null")
        void shouldThrowExceptionWhenCardTypeIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), null, new BigDecimal("5000.00"), "John Doe"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardType must not be null");
        }

        @Test
        @DisplayName("Should throw exception when cardholder name is null")
        void shouldThrowExceptionWhenCardholderNameIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardholderName must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when cardholder name is blank")
        void shouldThrowExceptionWhenCardholderNameIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), "   "
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardholderName must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when cardholder name is empty")
        void shouldThrowExceptionWhenCardholderNameIsEmpty() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), ""
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardholderName must not be blank");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null spending limit")
        void shouldHandleNullSpendingLimit() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, null, "John Doe"
            );

            // Then
            assertThat(request.spendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should handle zero spending limit")
        void shouldHandleZeroSpendingLimit() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, BigDecimal.ZERO, "John Doe"
            );

            // Then
            assertThat(request.spendingLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative spending limit")
        void shouldHandleNegativeSpendingLimit() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("-100.00"), "John Doe"
            );

            // Then
            assertThat(request.spendingLimit()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle spending limit with 4 decimal places")
        void shouldHandleSpendingLimitWith4DecimalPlaces() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("123.4567"), "John Doe"
            );

            // Then
            assertThat(request.spendingLimit()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large spending limit")
        void shouldHandleVeryLargeSpendingLimit() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("10000000.00"), "John Doe"
            );

            // Then
            assertThat(request.spendingLimit()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle cardholder name with special characters")
        void shouldHandleCardholderNameWithSpecialCharacters() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), "John O'Brien-Smith"
            );

            // Then
            assertThat(request.cardholderName()).isEqualTo("John O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle cardholder name with unicode")
        void shouldHandleCardholderNameWithUnicode() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), "用户 测试"
            );

            // Then
            assertThat(request.cardholderName()).isEqualTo("用户 测试");
        }

        @Test
        @DisplayName("Should handle long cardholder name")
        void shouldHandleLongCardholderName() {
            // When
            CardIssuanceRequest request = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), "a".repeat(100)
            );

            // Then
            assertThat(request.cardholderName()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another request with same fields")
        void shouldEqualAnotherRequestWithSameFields() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal spendingLimit = new BigDecimal("5000.00");

            CardIssuanceRequest request1 = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, spendingLimit, "John Doe"
            );

            CardIssuanceRequest request2 = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, spendingLimit, "John Doe"
            );

            // Then
            assertThat(request1).isEqualTo(request2);
        }

        @Test
        @DisplayName("Should not equal request with different account ID")
        void shouldNotEqualRequestWithDifferentAccountId() {
            // Given
            BigDecimal spendingLimit = new BigDecimal("5000.00");

            CardIssuanceRequest request1 = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, spendingLimit, "John Doe"
            );

            CardIssuanceRequest request2 = new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, spendingLimit, "John Doe"
            );

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal spendingLimit = new BigDecimal("5000.00");

            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, spendingLimit, "John Doe"
            );

            // Then
            assertThat(request.hashCode()).isNotNull();
            assertThat(request.hashCode()).isEqualTo(request.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal spendingLimit = new BigDecimal("5000.00");

            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, spendingLimit, "John Doe"
            );

            // Then
            assertThat(request.toString()).contains("CardIssuanceRequest");
            assertThat(request.toString()).contains("John Doe");
        }
    }
}
