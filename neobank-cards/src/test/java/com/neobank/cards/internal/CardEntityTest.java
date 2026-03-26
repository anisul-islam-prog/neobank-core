package com.neobank.cards.internal;

import com.neobank.cards.CardStatus;
import com.neobank.cards.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CardEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("CardEntity Unit Tests")
class CardEntityTest {

    private CardEntity cardEntity;

    @BeforeEach
    void setUp() {
        cardEntity = new CardEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            CardEntity entity = new CardEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            cardEntity.setId(id);

            // Then
            assertThat(cardEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get account ID")
        void shouldSetAndGetAccountId() {
            // Given
            UUID accountId = UUID.randomUUID();

            // When
            cardEntity.setAccountId(accountId);

            // Then
            assertThat(cardEntity.getAccountId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should set and get card type")
        void shouldSetAndGetCardType() {
            // Given
            CardType cardType = CardType.VIRTUAL;

            // When
            cardEntity.setCardType(cardType);

            // Then
            assertThat(cardEntity.getCardType()).isEqualTo(cardType);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            CardStatus status = CardStatus.ACTIVE;

            // When
            cardEntity.setStatus(status);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get spending limit")
        void shouldSetAndGetSpendingLimit() {
            // Given
            BigDecimal spendingLimit = new BigDecimal("5000.00");

            // When
            cardEntity.setSpendingLimit(spendingLimit);

            // Then
            assertThat(cardEntity.getSpendingLimit()).isEqualByComparingTo(spendingLimit);
        }

        @Test
        @DisplayName("Should set and get cardholder name")
        void shouldSetAndGetCardholderName() {
            // Given
            String cardholderName = "John Doe";

            // When
            cardEntity.setCardholderName(cardholderName);

            // Then
            assertThat(cardEntity.getCardholderName()).isEqualTo(cardholderName);
        }

        @Test
        @DisplayName("Should set and get card number masked")
        void shouldSetAndGetCardNumberMasked() {
            // Given
            String cardNumberMasked = "****-****-****-0366";

            // When
            cardEntity.setCardNumberMasked(cardNumberMasked);

            // Then
            assertThat(cardEntity.getCardNumberMasked()).isEqualTo(cardNumberMasked);
        }

        @Test
        @DisplayName("Should set and get card number encrypted")
        void shouldSetAndGetCardNumberEncrypted() {
            // Given
            String cardNumberEncrypted = "encrypted_card_number";

            // When
            cardEntity.setCardNumberEncrypted(cardNumberEncrypted);

            // Then
            assertThat(cardEntity.getCardNumberEncrypted()).isEqualTo(cardNumberEncrypted);
        }

        @Test
        @DisplayName("Should set and get expiry date")
        void shouldSetAndGetExpiryDate() {
            // Given
            YearMonth expiryDate = YearMonth.now().plusYears(4);

            // When
            cardEntity.setExpiryDate(expiryDate);

            // Then
            assertThat(cardEntity.getExpiryDate()).isEqualTo(expiryDate);
        }

        @Test
        @DisplayName("Should set and get CVV encrypted")
        void shouldSetAndGetCvvEncrypted() {
            // Given
            String cvvEncrypted = "encrypted_cvv";

            // When
            cardEntity.setCvvEncrypted(cvvEncrypted);

            // Then
            assertThat(cardEntity.getCvvEncrypted()).isEqualTo(cvvEncrypted);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            cardEntity.setCreatedAt(createdAt);

            // Then
            assertThat(cardEntity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get activated at timestamp")
        void shouldSetAndGetActivatedAtTimestamp() {
            // Given
            Instant activatedAt = Instant.now();

            // When
            cardEntity.setActivatedAt(activatedAt);

            // Then
            assertThat(cardEntity.getActivatedAt()).isEqualTo(activatedAt);
        }

        @Test
        @DisplayName("Should set and get MCC blocks JSON")
        void shouldSetAndGetMccBlocksJson() {
            // Given
            String mccBlocksJson = "[5999,7995]";

            // When
            cardEntity.setMccBlocksJson(mccBlocksJson);

            // Then
            assertThat(cardEntity.getMccBlocksJson()).isEqualTo(mccBlocksJson);
        }

        @Test
        @DisplayName("Should set and get monthly spent")
        void shouldSetAndGetMonthlySpent() {
            // Given
            BigDecimal monthlySpent = new BigDecimal("1500.00");

            // When
            cardEntity.setMonthlySpent(monthlySpent);

            // Then
            assertThat(cardEntity.getMonthlySpent()).isEqualByComparingTo(monthlySpent);
        }
    }

    @Nested
    @DisplayName("CardType Enum")
    class CardTypeEnumTests {

        @Test
        @DisplayName("Should have VIRTUAL type")
        void shouldHaveVirtualType() {
            // Then
            assertThat(CardType.VIRTUAL).isNotNull();
        }

        @Test
        @DisplayName("Should have PHYSICAL type")
        void shouldHavePhysicalType() {
            // Then
            assertThat(CardType.PHYSICAL).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 2 card types")
        void shouldHaveExactly2CardTypes() {
            // Then
            assertThat(CardType.values()).hasSize(2);
        }

        @Test
        @DisplayName("Should allow valueOf for all type names")
        void shouldAllowValueOfForAllTypeNames() {
            // Then
            assertThat(CardType.valueOf("VIRTUAL")).isEqualTo(CardType.VIRTUAL);
            assertThat(CardType.valueOf("PHYSICAL")).isEqualTo(CardType.PHYSICAL);
        }
    }

    @Nested
    @DisplayName("CardStatus Enum")
    class CardStatusEnumTests {

        @Test
        @DisplayName("Should have ACTIVE status")
        void shouldHaveActiveStatus() {
            // Then
            assertThat(CardStatus.ACTIVE).isNotNull();
        }

        @Test
        @DisplayName("Should have FROZEN status")
        void shouldHaveFrozenStatus() {
            // Then
            assertThat(CardStatus.FROZEN).isNotNull();
        }

        @Test
        @DisplayName("Should have BLOCKED status")
        void shouldHaveBlockedStatus() {
            // Then
            assertThat(CardStatus.BLOCKED).isNotNull();
        }

        @Test
        @DisplayName("Should have REPORTED_STOLEN status")
        void shouldHaveReportedStolenStatus() {
            // Then
            assertThat(CardStatus.REPORTED_STOLEN).isNotNull();
        }

        @Test
        @DisplayName("Should have EXPIRED status")
        void shouldHaveExpiredStatus() {
            // Then
            assertThat(CardStatus.EXPIRED).isNotNull();
        }

        @Test
        @DisplayName("Should have REPLACED status")
        void shouldHaveReplacedStatus() {
            // Then
            assertThat(CardStatus.REPLACED).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 6 status values")
        void shouldHaveExactly6StatusValues() {
            // Then
            assertThat(CardStatus.values()).hasSize(6);
        }

        @Test
        @DisplayName("Should allow valueOf for all status names")
        void shouldAllowValueOfForAllStatusNames() {
            // Then
            assertThat(CardStatus.valueOf("ACTIVE")).isEqualTo(CardStatus.ACTIVE);
            assertThat(CardStatus.valueOf("FROZEN")).isEqualTo(CardStatus.FROZEN);
            assertThat(CardStatus.valueOf("BLOCKED")).isEqualTo(CardStatus.BLOCKED);
            assertThat(CardStatus.valueOf("REPORTED_STOLEN")).isEqualTo(CardStatus.REPORTED_STOLEN);
            assertThat(CardStatus.valueOf("EXPIRED")).isEqualTo(CardStatus.EXPIRED);
            assertThat(CardStatus.valueOf("REPLACED")).isEqualTo(CardStatus.REPLACED);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionsTests {

        @Test
        @DisplayName("Should transition from ACTIVE to FROZEN")
        void shouldTransitionFromActiveToFrozen() {
            // Given
            cardEntity.setStatus(CardStatus.ACTIVE);

            // When
            cardEntity.setStatus(CardStatus.FROZEN);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.FROZEN);
        }

        @Test
        @DisplayName("Should transition from FROZEN to ACTIVE")
        void shouldTransitionFromFrozenToActive() {
            // Given
            cardEntity.setStatus(CardStatus.FROZEN);

            // When
            cardEntity.setStatus(CardStatus.ACTIVE);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to BLOCKED")
        void shouldTransitionFromActiveToBlocked() {
            // Given
            cardEntity.setStatus(CardStatus.ACTIVE);

            // When
            cardEntity.setStatus(CardStatus.BLOCKED);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to REPORTED_STOLEN")
        void shouldTransitionFromActiveToReportedStolen() {
            // Given
            cardEntity.setStatus(CardStatus.ACTIVE);

            // When
            cardEntity.setStatus(CardStatus.REPORTED_STOLEN);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.REPORTED_STOLEN);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to EXPIRED")
        void shouldTransitionFromActiveToExpired() {
            // Given
            cardEntity.setStatus(CardStatus.ACTIVE);

            // When
            cardEntity.setStatus(CardStatus.EXPIRED);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to REPLACED")
        void shouldTransitionFromActiveToReplaced() {
            // Given
            cardEntity.setStatus(CardStatus.ACTIVE);

            // When
            cardEntity.setStatus(CardStatus.REPLACED);

            // Then
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.REPLACED);
        }

        @Test
        @DisplayName("Should allow multiple status transitions")
        void shouldAllowMultipleStatusTransitions() {
            // Given
            cardEntity.setStatus(CardStatus.ACTIVE);

            // When/Then
            cardEntity.setStatus(CardStatus.FROZEN);
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.FROZEN);

            cardEntity.setStatus(CardStatus.ACTIVE);
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.ACTIVE);

            cardEntity.setStatus(CardStatus.BLOCKED);
            assertThat(cardEntity.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            cardEntity.setId(null);

            // Then
            assertThat(cardEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null account ID")
        void shouldHandleNullAccountId() {
            // When
            cardEntity.setAccountId(null);

            // Then
            assertThat(cardEntity.getAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null card type")
        void shouldHandleNullCardType() {
            // When
            cardEntity.setCardType(null);

            // Then
            assertThat(cardEntity.getCardType()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            cardEntity.setStatus(null);

            // Then
            assertThat(cardEntity.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null spending limit")
        void shouldHandleNullSpendingLimit() {
            // When
            cardEntity.setSpendingLimit(null);

            // Then
            assertThat(cardEntity.getSpendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should handle null cardholder name")
        void shouldHandleNullCardholderName() {
            // When
            cardEntity.setCardholderName(null);

            // Then
            assertThat(cardEntity.getCardholderName()).isNull();
        }

        @Test
        @DisplayName("Should handle null card number masked")
        void shouldHandleNullCardNumberMasked() {
            // When
            cardEntity.setCardNumberMasked(null);

            // Then
            assertThat(cardEntity.getCardNumberMasked()).isNull();
        }

        @Test
        @DisplayName("Should handle null card number encrypted")
        void shouldHandleNullCardNumberEncrypted() {
            // When
            cardEntity.setCardNumberEncrypted(null);

            // Then
            assertThat(cardEntity.getCardNumberEncrypted()).isNull();
        }

        @Test
        @DisplayName("Should handle null expiry date")
        void shouldHandleNullExpiryDate() {
            // When
            cardEntity.setExpiryDate(null);

            // Then
            assertThat(cardEntity.getExpiryDate()).isNull();
        }

        @Test
        @DisplayName("Should handle null CVV encrypted")
        void shouldHandleNullCvvEncrypted() {
            // When
            cardEntity.setCvvEncrypted(null);

            // Then
            assertThat(cardEntity.getCvvEncrypted()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            cardEntity.setCreatedAt(null);

            // Then
            assertThat(cardEntity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null activated at timestamp")
        void shouldHandleNullActivatedAtTimestamp() {
            // When
            cardEntity.setActivatedAt(null);

            // Then
            assertThat(cardEntity.getActivatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null MCC blocks JSON")
        void shouldHandleNullMccBlocksJson() {
            // When
            cardEntity.setMccBlocksJson(null);

            // Then
            assertThat(cardEntity.getMccBlocksJson()).isNull();
        }

        @Test
        @DisplayName("Should handle null monthly spent")
        void shouldHandleNullMonthlySpent() {
            // When
            cardEntity.setMonthlySpent(null);

            // Then
            assertThat(cardEntity.getMonthlySpent()).isNull();
        }

        @Test
        @DisplayName("Should handle empty MCC blocks JSON")
        void shouldHandleEmptyMccBlocksJson() {
            // When
            cardEntity.setMccBlocksJson("[]");

            // Then
            assertThat(cardEntity.getMccBlocksJson()).isEqualTo("[]");
        }

        @Test
        @DisplayName("Should handle long cardholder name")
        void shouldHandleLongCardholderName() {
            // Given
            String longName = "a".repeat(100);

            // When
            cardEntity.setCardholderName(longName);

            // Then
            assertThat(cardEntity.getCardholderName()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle zero spending limit")
        void shouldHandleZeroSpendingLimit() {
            // When
            cardEntity.setSpendingLimit(BigDecimal.ZERO);

            // Then
            assertThat(cardEntity.getSpendingLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero monthly spent")
        void shouldHandleZeroMonthlySpent() {
            // When
            cardEntity.setMonthlySpent(BigDecimal.ZERO);

            // Then
            assertThat(cardEntity.getMonthlySpent()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative spending limit")
        void shouldHandleNegativeSpendingLimit() {
            // When
            cardEntity.setSpendingLimit(new BigDecimal("-100.00"));

            // Then
            assertThat(cardEntity.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle spending limit with 4 decimal places")
        void shouldHandleSpendingLimitWith4DecimalPlaces() {
            // When
            cardEntity.setSpendingLimit(new BigDecimal("123.4567"));

            // Then
            assertThat(cardEntity.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large spending limit")
        void shouldHandleVeryLargeSpendingLimit() {
            // When
            cardEntity.setSpendingLimit(new BigDecimal("10000000.00"));

            // Then
            assertThat(cardEntity.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle cardholder name with special characters")
        void shouldHandleCardholderNameWithSpecialCharacters() {
            // Given
            String specialName = "John O'Brien-Smith";

            // When
            cardEntity.setCardholderName(specialName);

            // Then
            assertThat(cardEntity.getCardholderName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Should handle cardholder name with unicode")
        void shouldHandleCardholderNameWithUnicode() {
            // Given
            String unicodeName = "用户 测试";

            // When
            cardEntity.setCardholderName(unicodeName);

            // Then
            assertThat(cardEntity.getCardholderName()).isEqualTo(unicodeName);
        }
    }

    @Nested
    @DisplayName("Complete Entity State")
    class CompleteEntityStateTests {

        @Test
        @DisplayName("Should handle complete entity with all fields set")
        void shouldHandleCompleteEntityWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            CardStatus status = CardStatus.ACTIVE;
            BigDecimal spendingLimit = new BigDecimal("5000.00");
            String cardholderName = "John Doe";
            String cardNumberMasked = "****-****-****-0366";
            String cardNumberEncrypted = "encrypted_card_number";
            YearMonth expiryDate = YearMonth.now().plusYears(4);
            String cvvEncrypted = "encrypted_cvv";
            Instant createdAt = Instant.now();
            Instant activatedAt = Instant.now();
            String mccBlocksJson = "[5999,7995]";
            BigDecimal monthlySpent = new BigDecimal("1500.00");

            // When
            cardEntity.setId(id);
            cardEntity.setAccountId(accountId);
            cardEntity.setCardType(cardType);
            cardEntity.setStatus(status);
            cardEntity.setSpendingLimit(spendingLimit);
            cardEntity.setCardholderName(cardholderName);
            cardEntity.setCardNumberMasked(cardNumberMasked);
            cardEntity.setCardNumberEncrypted(cardNumberEncrypted);
            cardEntity.setExpiryDate(expiryDate);
            cardEntity.setCvvEncrypted(cvvEncrypted);
            cardEntity.setCreatedAt(createdAt);
            cardEntity.setActivatedAt(activatedAt);
            cardEntity.setMccBlocksJson(mccBlocksJson);
            cardEntity.setMonthlySpent(monthlySpent);

            // Then
            assertThat(cardEntity.getId()).isEqualTo(id);
            assertThat(cardEntity.getAccountId()).isEqualTo(accountId);
            assertThat(cardEntity.getCardType()).isEqualTo(cardType);
            assertThat(cardEntity.getStatus()).isEqualTo(status);
            assertThat(cardEntity.getSpendingLimit()).isEqualByComparingTo(spendingLimit);
            assertThat(cardEntity.getCardholderName()).isEqualTo(cardholderName);
            assertThat(cardEntity.getCardNumberMasked()).isEqualTo(cardNumberMasked);
            assertThat(cardEntity.getCardNumberEncrypted()).isEqualTo(cardNumberEncrypted);
            assertThat(cardEntity.getExpiryDate()).isEqualTo(expiryDate);
            assertThat(cardEntity.getCvvEncrypted()).isEqualTo(cvvEncrypted);
            assertThat(cardEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(cardEntity.getActivatedAt()).isEqualTo(activatedAt);
            assertThat(cardEntity.getMccBlocksJson()).isEqualTo(mccBlocksJson);
            assertThat(cardEntity.getMonthlySpent()).isEqualByComparingTo(monthlySpent);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            CardStatus status = CardStatus.ACTIVE;
            String cardholderName = "John Doe";
            String cardNumberMasked = "****-****-****-0366";
            String cardNumberEncrypted = "encrypted_card_number";
            YearMonth expiryDate = YearMonth.now().plusYears(4);
            String cvvEncrypted = "encrypted_cvv";
            Instant createdAt = Instant.now();

            // When
            cardEntity.setId(id);
            cardEntity.setAccountId(accountId);
            cardEntity.setCardType(cardType);
            cardEntity.setStatus(status);
            cardEntity.setCardholderName(cardholderName);
            cardEntity.setCardNumberMasked(cardNumberMasked);
            cardEntity.setCardNumberEncrypted(cardNumberEncrypted);
            cardEntity.setExpiryDate(expiryDate);
            cardEntity.setCvvEncrypted(cvvEncrypted);
            cardEntity.setCreatedAt(createdAt);

            // Then
            assertThat(cardEntity.getId()).isEqualTo(id);
            assertThat(cardEntity.getAccountId()).isEqualTo(accountId);
            assertThat(cardEntity.getCardType()).isEqualTo(cardType);
            assertThat(cardEntity.getStatus()).isEqualTo(status);
            assertThat(cardEntity.getSpendingLimit()).isNull();
            assertThat(cardEntity.getActivatedAt()).isNull();
            assertThat(cardEntity.getMccBlocksJson()).isNull();
            assertThat(cardEntity.getMonthlySpent()).isNull();
        }

        @Test
        @DisplayName("Should handle entity with null spending limit (no limit)")
        void shouldHandleEntityWithNullSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardType cardType = CardType.VIRTUAL;
            CardStatus status = CardStatus.ACTIVE;
            String cardholderName = "John Doe";
            String cardNumberMasked = "****-****-****-0366";
            String cardNumberEncrypted = "encrypted_card_number";
            YearMonth expiryDate = YearMonth.now().plusYears(4);
            String cvvEncrypted = "encrypted_cvv";
            Instant createdAt = Instant.now();

            // When
            cardEntity.setId(id);
            cardEntity.setAccountId(accountId);
            cardEntity.setCardType(cardType);
            cardEntity.setStatus(status);
            cardEntity.setSpendingLimit(null);
            cardEntity.setCardholderName(cardholderName);
            cardEntity.setCardNumberMasked(cardNumberMasked);
            cardEntity.setCardNumberEncrypted(cardNumberEncrypted);
            cardEntity.setExpiryDate(expiryDate);
            cardEntity.setCvvEncrypted(cvvEncrypted);
            cardEntity.setCreatedAt(createdAt);

            // Then
            assertThat(cardEntity.getSpendingLimit()).isNull();
        }
    }
}
