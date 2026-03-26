package com.neobank.cards.internal;

import com.neobank.cards.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CardDetails record using JUnit 5.
 * Tests record construction and field accessors.
 */
@DisplayName("CardDetails Unit Tests")
class CardDetailsTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create CardDetails with all fields")
        void shouldCreateCardDetailsWithAllFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            CardStatus status = CardStatus.ACTIVE;
            BigDecimal spendingLimit = new BigDecimal("5000.00");
            String cardholderName = "John Doe";
            String cardNumberMasked = "****-****-****-0366";
            YearMonth expiryDate = YearMonth.now().plusYears(4);
            String cvvHash = "encrypted_cvv";
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();

            // When
            CardDetails details = new CardDetails(
                    id, accountId, cardType, status, spendingLimit,
                    cardholderName, cardNumberMasked, expiryDate,
                    cvvHash, createdAt, activatedAt
            );

            // Then
            assertThat(details.id()).isEqualTo(id);
            assertThat(details.accountId()).isEqualTo(accountId);
            assertThat(details.cardType()).isEqualTo(cardType);
            assertThat(details.status()).isEqualTo(status);
            assertThat(details.spendingLimit()).isEqualByComparingTo(spendingLimit);
            assertThat(details.cardholderName()).isEqualTo(cardholderName);
            assertThat(details.cardNumberMasked()).isEqualTo(cardNumberMasked);
            assertThat(details.expiryDate()).isEqualTo(expiryDate);
            assertThat(details.cvvHash()).isEqualTo(cvvHash);
            assertThat(details.createdAt()).isEqualTo(createdAt);
            assertThat(details.activatedAt()).isEqualTo(activatedAt);
        }

        @Test
        @DisplayName("Should create CardDetails with null spending limit")
        void shouldCreateCardDetailsWithNullSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            CardStatus status = CardStatus.ACTIVE;
            String cardholderName = "John Doe";
            String cardNumberMasked = "****-****-****-0366";
            YearMonth expiryDate = YearMonth.now().plusYears(4);
            String cvvHash = "encrypted_cvv";
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();

            // When
            CardDetails details = new CardDetails(
                    id, accountId, cardType, status, null,
                    cardholderName, cardNumberMasked, expiryDate,
                    cvvHash, createdAt, activatedAt
            );

            // Then
            assertThat(details.spendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should create CardDetails with null activated at")
        void shouldCreateCardDetailsWithNullActivatedAt() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            CardStatus status = CardStatus.ACTIVE;
            BigDecimal spendingLimit = new BigDecimal("5000.00");
            String cardholderName = "John Doe";
            String cardNumberMasked = "****-****-****-0366";
            YearMonth expiryDate = YearMonth.now().plusYears(4);
            String cvvHash = "encrypted_cvv";
            Instant createdAt = Instant.now();

            // When
            CardDetails details = new CardDetails(
                    id, accountId, cardType, status, spendingLimit,
                    cardholderName, cardNumberMasked, expiryDate,
                    cvvHash, createdAt, null
            );

            // Then
            assertThat(details.activatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Field Accessors")
    class FieldAccessorsTests {

        @Test
        @DisplayName("Should access ID field")
        void shouldAccessIdField() {
            // Given
            UUID id = UUID.randomUUID();
            CardDetails details = createTestCardDetails(id);

            // Then
            assertThat(details.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should access account ID field")
        void shouldAccessAccountIdField() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardDetails details = createTestCardDetailsWithAccount(accountId);

            // Then
            assertThat(details.accountId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should access card type field")
        void shouldAccessCardTypeField() {
            // Given
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.PHYSICAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardType()).isEqualTo(CardType.PHYSICAL);
        }

        @Test
        @DisplayName("Should access status field")
        void shouldAccessStatusField() {
            // Given
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.FROZEN,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.status()).isEqualTo(CardStatus.FROZEN);
        }

        @Test
        @DisplayName("Should access spending limit field")
        void shouldAccessSpendingLimitField() {
            // Given
            BigDecimal spendingLimit = new BigDecimal("10000.00");
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    spendingLimit, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(spendingLimit);
        }

        @Test
        @DisplayName("Should access cardholder name field")
        void shouldAccessCardholderNameField() {
            // Given
            String cardholderName = "Jane Doe";
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, cardholderName, "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardholderName()).isEqualTo(cardholderName);
        }

        @Test
        @DisplayName("Should access card number masked field")
        void shouldAccessCardNumberMaskedField() {
            // Given
            String cardNumberMasked = "****-****-****-1234";
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", cardNumberMasked,
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardNumberMasked()).isEqualTo(cardNumberMasked);
        }

        @Test
        @DisplayName("Should access expiry date field")
        void shouldAccessExpiryDateField() {
            // Given
            YearMonth expiryDate = YearMonth.of(2028, 12);
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.expiryDate()).isEqualTo(expiryDate);
        }

        @Test
        @DisplayName("Should access CVV hash field")
        void shouldAccessCvvHashField() {
            // Given
            String cvvHash = "encrypted_cvv_hash";
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), cvvHash,
                    Instant.now(), null
            );

            // Then
            assertThat(details.cvvHash()).isEqualTo(cvvHash);
        }

        @Test
        @DisplayName("Should access created at field")
        void shouldAccessCreatedAtField() {
            // Given
            Instant createdAt = Instant.now();
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    createdAt, null
            );

            // Then
            assertThat(details.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should access activated at field")
        void shouldAccessActivatedAtField() {
            // Given
            Instant activatedAt = Instant.now();
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), activatedAt
            );

            // Then
            assertThat(details.activatedAt()).isEqualTo(activatedAt);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            CardDetails details = new CardDetails(
                    null, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.id()).isNull();
        }

        @Test
        @DisplayName("Should handle null account ID")
        void shouldHandleNullAccountId() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), null, CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.accountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null card type")
        void shouldHandleNullCardType() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), null, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardType()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, null,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.status()).isNull();
        }

        @Test
        @DisplayName("Should handle null cardholder name")
        void shouldHandleNullCardholderName() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, null, "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardholderName()).isNull();
        }

        @Test
        @DisplayName("Should handle null card number masked")
        void shouldHandleNullCardNumberMasked() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", null,
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardNumberMasked()).isNull();
        }

        @Test
        @DisplayName("Should handle null expiry date")
        void shouldHandleNullExpiryDate() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    null, "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.expiryDate()).isNull();
        }

        @Test
        @DisplayName("Should handle null CVV hash")
        void shouldHandleNullCvvHash() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), null,
                    Instant.now(), null
            );

            // Then
            assertThat(details.cvvHash()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at")
        void shouldHandleNullCreatedAt() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    null, null
            );

            // Then
            assertThat(details.createdAt()).isNull();
        }

        @Test
        @DisplayName("Should handle empty cardholder name")
        void shouldHandleEmptyCardholderName() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardholderName()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long cardholder name")
        void shouldHandleLongCardholderName() {
            // Given
            String longName = "a".repeat(100);

            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, longName, "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardholderName()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle cardholder name with special characters")
        void shouldHandleCardholderNameWithSpecialCharacters() {
            // Given
            String specialName = "John O'Brien-Smith";

            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, specialName, "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardholderName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Should handle cardholder name with unicode")
        void shouldHandleCardholderNameWithUnicode() {
            // Given
            String unicodeName = "用户 测试";

            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, unicodeName, "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.cardholderName()).isEqualTo(unicodeName);
        }

        @Test
        @DisplayName("Should handle zero spending limit")
        void shouldHandleZeroSpendingLimit() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    BigDecimal.ZERO, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative spending limit")
        void shouldHandleNegativeSpendingLimit() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("-100.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle spending limit with 4 decimal places")
        void shouldHandleSpendingLimitWith4DecimalPlaces() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("123.4567"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large spending limit")
        void shouldHandleVeryLargeSpendingLimit() {
            // When
            CardDetails details = new CardDetails(
                    UUID.randomUUID(), UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("10000000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    Instant.now(), null
            );

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another CardDetails with same fields")
        void shouldEqualAnotherCardDetailsWithSameFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();
            YearMonth expiryDate = YearMonth.now().plusYears(4);

            CardDetails details1 = new CardDetails(
                    id, accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash", createdAt, activatedAt
            );

            CardDetails details2 = new CardDetails(
                    id, accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash", createdAt, activatedAt
            );

            // Then
            assertThat(details1).isEqualTo(details2);
        }

        @Test
        @DisplayName("Should not equal CardDetails with different ID")
        void shouldNotEqualCardDetailsWithDifferentId() {
            // Given
            UUID accountId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();
            YearMonth expiryDate = YearMonth.now().plusYears(4);

            CardDetails details1 = new CardDetails(
                    UUID.randomUUID(), accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash", createdAt, activatedAt
            );

            CardDetails details2 = new CardDetails(
                    UUID.randomUUID(), accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash", createdAt, activatedAt
            );

            // Then
            assertThat(details1).isNotEqualTo(details2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();
            YearMonth expiryDate = YearMonth.now().plusYears(4);

            CardDetails details = new CardDetails(
                    id, accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash", createdAt, activatedAt
            );

            // Then
            assertThat(details.hashCode()).isNotNull();
            assertThat(details.hashCode()).isEqualTo(details.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();
            YearMonth expiryDate = YearMonth.now().plusYears(4);

            CardDetails details = new CardDetails(
                    id, accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    expiryDate, "cvv_hash", createdAt, activatedAt
            );

            // Then
            assertThat(details.toString()).contains("CardDetails");
            assertThat(details.toString()).contains("John Doe");
            assertThat(details.toString()).contains("****-****-****-0366");
        }
    }

    /**
     * Helper method to create a test CardDetails.
     */
    private CardDetails createTestCardDetails(UUID id) {
        return new CardDetails(
                id, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                YearMonth.now().plusYears(4), "cvv_hash",
                Instant.now(), null
        );
    }

    /**
     * Helper method to create a test CardDetails with specific account.
     */
    private CardDetails createTestCardDetailsWithAccount(UUID accountId) {
        return new CardDetails(
                UUID.randomUUID(), accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                YearMonth.now().plusYears(4), "cvv_hash",
                Instant.now(), null
        );
    }
}
