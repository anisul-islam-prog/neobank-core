package com.neobank.cards.internal;

import com.neobank.auth.api.UserStatusChecker;
import com.neobank.cards.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CardService using JUnit 5 and Mockito.
 * Tests card issuance, retrieval, and management operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CardService Unit Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private UserStatusChecker userStatusChecker;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, cardMapper, encryptionService, userStatusChecker);
    }

    @Nested
    @DisplayName("Card Issuance")
    class CardIssuanceTests {

        @Test
        @DisplayName("Should issue VIRTUAL card successfully")
        void shouldIssueVirtualCardSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );

            String cardNumber = "4532015112830366";
            String maskedNumber = "****-****-****-0366";
            String encryptedCardNumber = "encrypted_card_number";
            String cvv = "123";
            String encryptedCvv = "encrypted_cvv";
            UUID cardId = UUID.randomUUID();

            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberMasked(maskedNumber);

            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCardNumber);
            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCvv);
            given(cardMapper.toEntity(any(CardIssuanceRequest.class), any(String.class), any(String.class), any(String.class))).willReturn(entity);
            given(cardRepository.save(entity)).willReturn(entity);

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.cardId()).isEqualTo(cardId);
            assertThat(result.cardNumberMasked()).startsWith("****-****-****-");
            verify(cardRepository).save(entity);
        }

        @Test
        @DisplayName("Should issue PHYSICAL card successfully")
        void shouldIssuePhysicalCardSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.PHYSICAL, new BigDecimal("10000.00"), "Jane Doe"
            );

            String cardNumber = "4532015112830367";
            String maskedNumber = "****-****-****-0367";
            String encryptedCardNumber = "encrypted_card_number";
            String cvv = "456";
            String encryptedCvv = "encrypted_cvv";
            UUID cardId = UUID.randomUUID();

            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberMasked(maskedNumber);

            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCardNumber);
            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCvv);
            given(cardMapper.toEntity(any(CardIssuanceRequest.class), any(String.class), any(String.class), any(String.class))).willReturn(entity);
            given(cardRepository.save(entity)).willReturn(entity);

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.cardId()).isEqualTo(cardId);
        }

        @Test
        @DisplayName("Should handle card issuance failure")
        void shouldHandleCardIssuanceFailure() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );

            given(encryptionService.encrypt(any(String.class))).willThrow(new RuntimeException("Encryption failed"));

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Card issuance failed");
        }

        @Test
        @DisplayName("Should generate unique card numbers for multiple cards")
        void shouldGenerateUniqueCardNumbersForMultipleCards() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request1 = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );
            CardIssuanceRequest request2 = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            );

            CardEntity entity1 = new CardEntity();
            entity1.setId(UUID.randomUUID());
            entity1.setCardNumberMasked("****-****-****-1111");

            CardEntity entity2 = new CardEntity();
            entity2.setId(UUID.randomUUID());
            entity2.setCardNumberMasked("****-****-****-2222");

            given(encryptionService.encrypt(any(String.class))).willReturn("encrypted");
            given(cardMapper.toEntity(any(), any(), any(), any())).willReturn(entity1, entity2);
            given(cardRepository.save(any())).willReturn(entity1, entity2);

            // When
            CardIssuanceResult result1 = cardService.issueCard(request1);
            CardIssuanceResult result2 = cardService.issueCard(request2);

            // Then
            assertThat(result1.cardId()).isNotEqualTo(result2.cardId());
        }

        @Test
        @DisplayName("Should handle null account ID in request")
        void shouldHandleNullAccountIdInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    null, CardType.VIRTUAL, new BigDecimal("5000.00"), "John Doe"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("accountId must not be null");
        }

        @Test
        @DisplayName("Should handle null card type in request")
        void shouldHandleNullCardTypeInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), null, new BigDecimal("5000.00"), "John Doe"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardType must not be null");
        }

        @Test
        @DisplayName("Should handle null cardholder name in request")
        void shouldHandleNullCardholderNameInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardholderName must not be blank");
        }

        @Test
        @DisplayName("Should handle blank cardholder name in request")
        void shouldHandleBlankCardholderNameInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new CardIssuanceRequest(
                    UUID.randomUUID(), CardType.VIRTUAL, new BigDecimal("5000.00"), "   "
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cardholderName must not be blank");
        }

        @Test
        @DisplayName("Should handle null spending limit")
        void shouldHandleNullSpendingLimit() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, null, "John Doe"
            );

            String cardNumber = "4532015112830366";
            String maskedNumber = "****-****-****-0366";
            String encryptedCardNumber = "encrypted_card_number";
            String cvv = "123";
            String encryptedCvv = "encrypted_cvv";
            UUID cardId = UUID.randomUUID();

            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberMasked(maskedNumber);

            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCardNumber);
            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCvv);
            given(cardMapper.toEntity(any(CardIssuanceRequest.class), any(String.class), any(String.class), any(String.class))).willReturn(entity);
            given(cardRepository.save(entity)).willReturn(entity);

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle cardholder name with special characters")
        void shouldHandleCardholderNameWithSpecialCharacters() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "John O'Brien-Smith"
            );

            String cardNumber = "4532015112830366";
            String maskedNumber = "****-****-****-0366";
            String encryptedCardNumber = "encrypted_card_number";
            String cvv = "123";
            String encryptedCvv = "encrypted_cvv";
            UUID cardId = UUID.randomUUID();

            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberMasked(maskedNumber);

            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCardNumber);
            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCvv);
            given(cardMapper.toEntity(any(CardIssuanceRequest.class), any(String.class), any(String.class), any(String.class))).willReturn(entity);
            given(cardRepository.save(entity)).willReturn(entity);

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle cardholder name with unicode characters")
        void shouldHandleCardholderNameWithUnicodeCharacters() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "用户 测试"
            );

            String cardNumber = "4532015112830366";
            String maskedNumber = "****-****-****-0366";
            String encryptedCardNumber = "encrypted_card_number";
            String cvv = "123";
            String encryptedCvv = "encrypted_cvv";
            UUID cardId = UUID.randomUUID();

            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberMasked(maskedNumber);

            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCardNumber);
            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCvv);
            given(cardMapper.toEntity(any(CardIssuanceRequest.class), any(String.class), any(String.class), any(String.class))).willReturn(entity);
            given(cardRepository.save(entity)).willReturn(entity);

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("Card Retrieval")
    class CardRetrievalTests {

        @Test
        @DisplayName("Should get card by ID successfully")
        void shouldGetCardByIdSuccessfully() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setAccountId(UUID.randomUUID());
            entity.setCardType(CardType.VIRTUAL);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setCardNumberMasked("****-****-****-0366");

            CardDetails details = new CardDetails(
                    cardId, entity.getAccountId(), entity.getCardType(), entity.getStatus(),
                    entity.getSpendingLimit(), entity.getCardholderName(),
                    entity.getCardNumberMasked(), entity.getExpiryDate(),
                    entity.getCvvEncrypted(), entity.getCreatedAt(), entity.getActivatedAt()
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.getCard(cardId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(cardId);
        }

        @Test
        @DisplayName("Should return null when card not found by ID")
        void shouldReturnNullWhenCardNotFoundById() {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardRepository.findById(cardId)).willReturn(Optional.empty());

            // When
            CardDetails result = cardService.getCard(cardId);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should get all cards for account")
        void shouldGetAllCardsForAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardEntity entity1 = new CardEntity();
            entity1.setId(UUID.randomUUID());
            entity1.setAccountId(accountId);

            CardEntity entity2 = new CardEntity();
            entity2.setId(UUID.randomUUID());
            entity2.setAccountId(accountId);

            CardDetails details1 = new CardDetails(
                    entity1.getId(), entity1.getAccountId(), entity1.getCardType(), entity1.getStatus(),
                    entity1.getSpendingLimit(), entity1.getCardholderName(),
                    entity1.getCardNumberMasked(), entity1.getExpiryDate(),
                    entity1.getCvvEncrypted(), entity1.getCreatedAt(), entity1.getActivatedAt()
            );

            CardDetails details2 = new CardDetails(
                    entity2.getId(), entity2.getAccountId(), entity2.getCardType(), entity2.getStatus(),
                    entity2.getSpendingLimit(), entity2.getCardholderName(),
                    entity2.getCardNumberMasked(), entity2.getExpiryDate(),
                    entity2.getCvvEncrypted(), entity2.getCreatedAt(), entity2.getActivatedAt()
            );

            given(cardRepository.findByAccountId(accountId)).willReturn(List.of(entity1, entity2));
            given(cardMapper.toDetails(entity1)).willReturn(details1);
            given(cardMapper.toDetails(entity2)).willReturn(details2);

            // When
            List<CardDetails> results = cardService.getCardsForAccount(accountId);

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no cards for account")
        void shouldReturnEmptyListWhenNoCardsForAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            given(cardRepository.findByAccountId(accountId)).willReturn(List.of());

            // When
            List<CardDetails> results = cardService.getCardsForAccount(accountId);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Card Status Update")
    class CardStatusUpdateTests {

        @Test
        @DisplayName("Should update card status to FROZEN")
        void shouldUpdateCardStatusToFrozen() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.FROZEN,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateStatus(cardId, CardStatus.FROZEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(CardStatus.FROZEN);
            verify(cardRepository).save(entity);
        }

        @Test
        @DisplayName("Should update card status to BLOCKED")
        void shouldUpdateCardStatusToBlocked() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.BLOCKED,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateStatus(cardId, CardStatus.BLOCKED);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should update card status to EXPIRED")
        void shouldUpdateCardStatusToExpired() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.EXPIRED,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateStatus(cardId, CardStatus.EXPIRED);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(CardStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should update card status to REPORTED_STOLEN")
        void shouldUpdateCardStatusToReportedStolen() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.REPORTED_STOLEN,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateStatus(cardId, CardStatus.REPORTED_STOLEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(CardStatus.REPORTED_STOLEN);
        }

        @Test
        @DisplayName("Should update card status to REPLACED")
        void shouldUpdateCardStatusToReplaced() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.REPLACED,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateStatus(cardId, CardStatus.REPLACED);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(CardStatus.REPLACED);
        }

        @Test
        @DisplayName("Should set activatedAt when status changes to ACTIVE")
        void shouldSetActivatedAtWhenStatusChangesToActive() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.FROZEN);
            entity.setActivatedAt(null);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), Instant.now()
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            cardService.updateStatus(cardId, CardStatus.ACTIVE);

            // Then
            assertThat(entity.getActivatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not change activatedAt when already set")
        void shouldNotChangeActivatedAtWhenAlreadySet() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.FROZEN);
            Instant existingActivatedAt = Instant.now();
            entity.setActivatedAt(existingActivatedAt);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), existingActivatedAt
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            cardService.updateStatus(cardId, CardStatus.ACTIVE);

            // Then
            assertThat(entity.getActivatedAt()).isEqualTo(existingActivatedAt);
        }

        @Test
        @DisplayName("Should return null when card not found for status update")
        void shouldReturnNullWhenCardNotFoundForStatusUpdate() {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardRepository.findById(cardId)).willReturn(Optional.empty());

            // When
            CardDetails result = cardService.updateStatus(cardId, CardStatus.FROZEN);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Spending Limit Update")
    class SpendingLimitUpdateTests {

        @Test
        @DisplayName("Should update spending limit successfully")
        void shouldUpdateSpendingLimitSuccessfully() {
            // Given
            UUID cardId = UUID.randomUUID();
            BigDecimal newLimit = new BigDecimal("10000.00");
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setSpendingLimit(new BigDecimal("5000.00"));

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    newLimit, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateSpendingLimit(cardId, newLimit);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getSpendingLimit()).isEqualByComparingTo(newLimit);
            verify(cardRepository).save(entity);
        }

        @Test
        @DisplayName("Should update spending limit to null (no limit)")
        void shouldUpdateSpendingLimitToNull() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setSpendingLimit(new BigDecimal("5000.00"));

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateSpendingLimit(cardId, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getSpendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should update spending limit to zero")
        void shouldUpdateSpendingLimitToZero() {
            // Given
            UUID cardId = UUID.randomUUID();
            BigDecimal newLimit = BigDecimal.ZERO;
            CardEntity entity = new CardEntity();
            entity.setId(cardId);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    newLimit, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateSpendingLimit(cardId, newLimit);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getSpendingLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return null when card not found for spending limit update")
        void shouldReturnNullWhenCardNotFoundForSpendingLimitUpdate() {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardRepository.findById(cardId)).willReturn(Optional.empty());

            // When
            CardDetails result = cardService.updateSpendingLimit(cardId, new BigDecimal("10000.00"));

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Internal Methods")
    class InternalMethodsTests {

        @Test
        @DisplayName("Should get decrypted card number")
        void shouldGetDecryptedCardNumber() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberEncrypted("encrypted_card_number");
            String decryptedNumber = "4532015112830366";

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(encryptionService.decrypt("encrypted_card_number")).willReturn(decryptedNumber);

            // When
            String result = cardService.getDecryptedCardNumber(cardId);

            // Then
            assertThat(result).isEqualTo(decryptedNumber);
        }

        @Test
        @DisplayName("Should return null when card not found for decryption")
        void shouldReturnNullWhenCardNotFoundForDecryption() {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardRepository.findById(cardId)).willReturn(Optional.empty());

            // When
            String result = cardService.getDecryptedCardNumber(cardId);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should get decrypted CVV")
        void shouldGetDecryptedCvv() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCvvEncrypted("encrypted_cvv");
            String decryptedCvv = "123";

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(encryptionService.decrypt("encrypted_cvv")).willReturn(decryptedCvv);

            // When
            String result = cardService.getDecryptedCvv(cardId);

            // Then
            assertThat(result).isEqualTo(decryptedCvv);
        }

        @Test
        @DisplayName("Should return null when card not found for CVV decryption")
        void shouldReturnNullWhenCardNotFoundForCvvDecryption() {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardRepository.findById(cardId)).willReturn(Optional.empty());

            // When
            String result = cardService.getDecryptedCvv(cardId);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should block MCC codes for card")
        void shouldBlockMccCodesForCard() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            List<Integer> mccCodes = List.of(5999, 7995);

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));

            // When
            cardService.blockMccCodes(cardId, mccCodes);

            // Then
            verify(cardRepository).findById(cardId);
        }

        @Test
        @DisplayName("Should allow transaction for ACTIVE card within limit")
        void shouldAllowTransactionForActiveCardWithinLimit() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("5000.00"));
            entity.setMonthlySpent(new BigDecimal("1000.00"));

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));

            // When
            boolean allowed = cardService.isTransactionAllowed(cardId, new BigDecimal("500.00"), 5411);

            // Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("Should deny transaction for non-ACTIVE card")
        void shouldDenyTransactionForNonActiveCard() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.FROZEN);

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));

            // When
            boolean allowed = cardService.isTransactionAllowed(cardId, new BigDecimal("500.00"), 5411);

            // Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("Should deny transaction exceeding spending limit")
        void shouldDenyTransactionExceedingSpendingLimit() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(new BigDecimal("5000.00"));
            entity.setMonthlySpent(new BigDecimal("4900.00"));

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));

            // When
            boolean allowed = cardService.isTransactionAllowed(cardId, new BigDecimal("500.00"), 5411);

            // Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("Should allow transaction when no spending limit set")
        void shouldAllowTransactionWhenNoSpendingLimitSet() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);
            entity.setSpendingLimit(null);

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));

            // When
            boolean allowed = cardService.isTransactionAllowed(cardId, new BigDecimal("500.00"), 5411);

            // Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("Should return false when card not found for transaction check")
        void shouldReturnFalseWhenCardNotFoundForTransactionCheck() {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardRepository.findById(cardId)).willReturn(Optional.empty());

            // When
            boolean allowed = cardService.isTransactionAllowed(cardId, new BigDecimal("500.00"), 5411);

            // Then
            assertThat(allowed).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large spending limit")
        void shouldHandleVeryLargeSpendingLimit() {
            // Given
            UUID cardId = UUID.randomUUID();
            BigDecimal newLimit = new BigDecimal("10000000.00");
            CardEntity entity = new CardEntity();
            entity.setId(cardId);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    newLimit, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateSpendingLimit(cardId, newLimit);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getSpendingLimit()).isEqualByComparingTo(newLimit);
        }

        @Test
        @DisplayName("Should handle spending limit with 4 decimal places")
        void shouldHandleSpendingLimitWith4DecimalPlaces() {
            // Given
            UUID cardId = UUID.randomUUID();
            BigDecimal newLimit = new BigDecimal("123.4567");
            CardEntity entity = new CardEntity();
            entity.setId(cardId);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    newLimit, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            CardDetails result = cardService.updateSpendingLimit(cardId, newLimit);

            // Then
            assertThat(result).isNotNull();
            assertThat(entity.getSpendingLimit()).isEqualByComparingTo(newLimit);
        }

        @Test
        @DisplayName("Should handle multiple status updates in sequence")
        void shouldHandleMultipleStatusUpdatesInSequence() {
            // Given
            UUID cardId = UUID.randomUUID();
            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setStatus(CardStatus.ACTIVE);

            CardDetails details = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.FROZEN,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "encrypted_cvv",
                    Instant.now(), null
            );

            given(cardRepository.findById(cardId)).willReturn(Optional.of(entity));
            given(cardRepository.save(entity)).willReturn(entity);
            given(cardMapper.toDetails(entity)).willReturn(details);

            // When
            cardService.updateStatus(cardId, CardStatus.FROZEN);
            entity.setStatus(CardStatus.FROZEN);
            cardService.updateStatus(cardId, CardStatus.BLOCKED);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should handle cardholder name with very long string")
        void shouldHandleCardholderNameWithVeryLongString() {
            // Given
            UUID accountId = UUID.randomUUID();
            CardIssuanceRequest request = new CardIssuanceRequest(
                    accountId, CardType.VIRTUAL, new BigDecimal("5000.00"), "a".repeat(100)
            );

            String cardNumber = "4532015112830366";
            String maskedNumber = "****-****-****-0366";
            String encryptedCardNumber = "encrypted_card_number";
            String cvv = "123";
            String encryptedCvv = "encrypted_cvv";
            UUID cardId = UUID.randomUUID();

            CardEntity entity = new CardEntity();
            entity.setId(cardId);
            entity.setCardNumberMasked(maskedNumber);

            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCardNumber);
            given(encryptionService.encrypt(any(String.class))).willReturn(encryptedCvv);
            given(cardMapper.toEntity(any(CardIssuanceRequest.class), any(String.class), any(String.class), any(String.class))).willReturn(entity);
            given(cardRepository.save(entity)).willReturn(entity);

            // When
            CardIssuanceResult result = cardService.issueCard(request);

            // Then
            assertThat(result.success()).isTrue();
        }
    }
}
