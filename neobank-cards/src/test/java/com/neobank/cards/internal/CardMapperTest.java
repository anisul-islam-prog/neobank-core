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

/**
 * Unit tests for CardMapper using JUnit 5.
 * Tests entity-to-domain and domain-to-entity mapping.
 */
@DisplayName("CardMapper Unit Tests")
class CardMapperTest {

    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapper();
    }

    @Nested
    @DisplayName("To Entity Mapping")
    class ToEntityMappingTests {

        @Test
        @DisplayName("Should map CardIssuanceRequest to CardEntity")
        void shouldMapCardIssuanceRequestToCardEntity() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );
            String cardNumberMasked = "****-****-****-0366";
            String cardNumberEncrypted = "encrypted_card_number";
            String cvvEncrypted = "encrypted_cvv";

            // When
            CardEntity entity = cardMapper.toEntity(request, cardNumberMasked, cardNumberEncrypted, cvvEncrypted);

            // Then
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getAccountId()).isEqualTo(accountId);
            assertThat(entity.getCardType()).isEqualTo(CardType.VIRTUAL);
            assertThat(entity.getStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(entity.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(entity.getCardholderName()).isEqualTo("John Doe");
            assertThat(entity.getCardNumberMasked()).isEqualTo(cardNumberMasked);
            assertThat(entity.getCardNumberEncrypted()).isEqualTo(cardNumberEncrypted);
            assertThat(entity.getCvvEncrypted()).isEqualTo(cvvEncrypted);
            assertThat(entity.getMccBlocksJson()).isEqualTo("[]");
            assertThat(entity.getMonthlySpent()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should set ACTIVE status for new card")
        void shouldSetActiveStatusForNewCard() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getStatus()).isEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should set expiry date 4 years from now")
        void shouldSetExpiryDate4YearsFromNow() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );
            YearMonth expectedExpiry = YearMonth.now().plusYears(4);

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getExpiryDate().getYear()).isEqualTo(expectedExpiry.getYear());
            assertThat(entity.getExpiryDate().getMonth()).isEqualTo(expectedExpiry.getMonth());
        }

        @Test
        @DisplayName("Should set created at timestamp")
        void shouldSetCreatedAtTimestamp() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );
            Instant beforeMapping = Instant.now();

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getCreatedAt()).isAfterOrEqualTo(beforeMapping);
        }

        @Test
        @DisplayName("Should map PHYSICAL card type")
        void shouldMapPhysicalCardType() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.PHYSICAL, new BigDecimal("10000.00"), "John Doe"
            );

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getCardType()).isEqualTo(CardType.PHYSICAL);
        }

        @Test
        @DisplayName("Should map null spending limit")
        void shouldMapNullSpendingLimit() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, null, "John Doe"
            );

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getSpendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should map cardholder name with special characters")
        void shouldMapCardholderNameWithSpecialCharacters() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John O'Brien-Smith"
            );

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getCardholderName()).isEqualTo("John O'Brien-Smith");
        }

        @Test
        @DisplayName("Should map cardholder name with unicode")
        void shouldMapCardholderNameWithUnicode() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "用户 测试"
            );

            // When
            CardEntity entity = cardMapper.toEntity(request, "****-****-****-0366", "encrypted", "encrypted");

            // Then
            assertThat(entity.getCardholderName()).isEqualTo("用户 测试");
        }
    }

    @Nested
    @DisplayName("To Details Mapping")
    class ToDetailsMappingTests {

        @Test
        @DisplayName("Should map CardEntity to CardDetails")
        void shouldMapCardEntityToCardDetails() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(id);
            entity.setAccountId(accountId);
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("5000.00"));
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.id()).isEqualTo(id);
            assertThat(details.accountId()).isEqualTo(accountId);
            assertThat(details.cardType()).isEqualTo(CardType.VIRTUAL);
            assertThat(details.status()).isEqualTo(CardStatus.ACTIVE);
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(details.cardholderName()).isEqualTo("John Doe");
            assertThat(details.cardNumberMasked()).isEqualTo("****-****-****-0366");
            assertThat(details.expiryDate()).isEqualTo(entity.getExpiryDate());
            assertThat(details.cvvHash()).isEqualTo("encrypted_cvv");
            assertThat(details.createdAt()).isEqualTo(entity.getCreatedAt());
            assertThat(details.activatedAt()).isEqualTo(entity.getActivatedAt());
        }

        @Test
        @DisplayName("Should map entity with null spending limit")
        void shouldMapEntityWithNullSpendingLimit() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(null);
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(null);

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.spendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should map entity with null activated at")
        void shouldMapEntityWithNullActivatedAt() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("5000.00"));
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(null);

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.activatedAt()).isNull();
        }

        @Test
        @DisplayName("Should map entity with FROZEN status")
        void shouldMapEntityWithFrozenStatus() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.FROZEN);
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.status()).isEqualTo(CardStatus.FROZEN);
        }

        @Test
        @DisplayName("Should map entity with BLOCKED status")
        void shouldMapEntityWithBlockedStatus() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.BLOCKED);
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.status()).isEqualTo(CardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should map entity with PHYSICAL card type")
        void shouldMapEntityWithPhysicalCardType() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.PHYSICAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.cardType()).isEqualTo(CardType.PHYSICAL);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle entity with null fields")
        void shouldHandleEntityWithNullFields() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(null);
            entity.setAccountId(null);
            entity.setCardType(null);
            entity.setStatus(null);
            entity.setSpendingLimit(null);
            entity.setCardholderName(null);
            entity.setCardNumberMasked(null);
            entity.setExpiryDate(null);
            entity.setCvvEncrypted(null);
            entity.setCreatedAt(null);
            entity.setActivatedAt(null);

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.id()).isNull();
            assertThat(details.accountId()).isNull();
            assertThat(details.cardType()).isNull();
            assertThat(details.status()).isNull();
            assertThat(details.spendingLimit()).isNull();
            assertThat(details.cardholderName()).isNull();
            assertThat(details.cardNumberMasked()).isNull();
            assertThat(details.expiryDate()).isNull();
            assertThat(details.cvvHash()).isNull();
            assertThat(details.createdAt()).isNull();
            assertThat(details.activatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle entity with zero spending limit")
        void shouldHandleEntityWithZeroSpendingLimit() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(BigDecimal.ZERO);
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle entity with negative spending limit")
        void shouldHandleEntityWithNegativeSpendingLimit() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("-100.00"));
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle entity with very large spending limit")
        void shouldHandleEntityWithVeryLargeSpendingLimit() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("10000000.00"));
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle entity with spending limit with 4 decimal places")
        void shouldHandleEntityWithSpendingLimitWith4DecimalPlaces() {
            // Given
            CardEntity entity = new CardEntity();
            entity.setId(UUID.randomUUID());
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("123.4567"));
            entity.setCardholderName("John Doe");
            entity.setCardNumberMasked("****-****-****-0366");
            entity.setExpiryDate(YearMonth.now().plusYears(4));
            entity.setCvvEncrypted("encrypted_cvv");
            entity.setCreatedAt(Instant.now());
            entity.setActivatedAt(Instant.now());

            // When
            CardDetails details = cardMapper.toDetails(entity);

            // Then
            assertThat(details.spendingLimit()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }
    }
}
