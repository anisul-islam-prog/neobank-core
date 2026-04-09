package com.neobank.cards.internal;

import com.neobank.cards.CardStatus;
import com.neobank.cards.CardType;
import com.neobank.cards.CardsIntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for CardRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@Import(CardsIntegrationTestConfig.class)
@Sql(scripts = "/init-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("CardRepository Integration Tests")
class CardRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CardRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve card by ID")
        void shouldSaveAndRetrieveCardById() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);

            // When
            CardEntity saved = repository.save(card);
            CardEntity retrieved = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getCardholderName()).isEqualTo(card.getCardholderName());
            assertThat(retrieved.getCardType()).isEqualTo(card.getCardType());
        }

        @Test
        @DisplayName("Should save card with VIRTUAL type")
        void shouldSaveCardWithVirtualType() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setCardType(CardType.VIRTUAL);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getCardType()).isEqualTo(CardType.VIRTUAL);
        }

        @Test
        @DisplayName("Should save card with PHYSICAL type")
        void shouldSaveCardWithPhysicalType() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setCardType(CardType.PHYSICAL);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getCardType()).isEqualTo(CardType.PHYSICAL);
        }

        @Test
        @DisplayName("Should save card with ACTIVE status")
        void shouldSaveCardWithActiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setStatus(CardStatus.ACTIVE);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getStatus()).isEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should save card with FROZEN status")
        void shouldSaveCardWithFrozenStatus() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setStatus(CardStatus.FROZEN);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getStatus()).isEqualTo(CardStatus.FROZEN);
        }

        @Test
        @DisplayName("Should save card with BLOCKED status")
        void shouldSaveCardWithBlockedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setStatus(CardStatus.BLOCKED);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should save card with spending limit")
        void shouldSaveCardWithSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setSpendingLimit(new BigDecimal("5000.00"));

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Should save card with null spending limit")
        void shouldSaveCardWithNullSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setSpendingLimit(null);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getSpendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should save card with activated at timestamp")
        void shouldSaveCardWithActivatedAtTimestamp() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setActivatedAt(Instant.now());

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getActivatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should save card with MCC blocks JSON")
        void shouldSaveCardWithMccBlocksJson() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setMccBlocksJson("[5999,7995]");

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getMccBlocksJson()).isEqualTo("[5999,7995]");
        }

        @Test
        @DisplayName("Should save card with monthly spent")
        void shouldSaveCardWithMonthlySpent() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setMonthlySpent(new BigDecimal("1500.00"));

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getMonthlySpent()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }
    }

    @Nested
    @DisplayName("Find By Account ID")
    class FindByAccountIdTests {

        @Test
        @DisplayName("Should find all cards for account")
        void shouldFindAllCardsForAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            repository.save(createTestCardWithAccount(UUID.randomUUID(), accountId));
            repository.save(createTestCardWithAccount(UUID.randomUUID(), accountId));
            repository.save(createTestCardWithAccount(UUID.randomUUID(), accountId));

            // When
            List<CardEntity> results = repository.findByAccountId(accountId);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results).allMatch(c -> c.getAccountId().equals(accountId));
        }

        @Test
        @DisplayName("Should return empty list when no cards for account")
        void shouldReturnEmptyListWhenNoCardsForAccount() {
            // Given
            UUID nonExistentAccountId = UUID.randomUUID();

            // When
            List<CardEntity> results = repository.findByAccountId(nonExistentAccountId);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should find cards with different statuses for same account")
        void shouldFindCardsWithDifferentStatusesForSameAccount() {
            // Given
            UUID accountId = UUID.randomUUID();

            CardEntity card1 = createTestCardWithAccount(UUID.randomUUID(), accountId);
            card1.setStatus(CardStatus.ACTIVE);

            CardEntity card2 = createTestCardWithAccount(UUID.randomUUID(), accountId);
            card2.setStatus(CardStatus.FROZEN);

            CardEntity card3 = createTestCardWithAccount(UUID.randomUUID(), accountId);
            card3.setStatus(CardStatus.BLOCKED);

            repository.saveAll(List.of(card1, card2, card3));

            // When
            List<CardEntity> results = repository.findByAccountId(accountId);

            // Then
            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("Should find cards with different types for same account")
        void shouldFindCardsWithDifferentTypesForSameAccount() {
            // Given
            UUID accountId = UUID.randomUUID();

            CardEntity card1 = createTestCardWithAccount(UUID.randomUUID(), accountId);
            card1.setCardType(CardType.VIRTUAL);

            CardEntity card2 = createTestCardWithAccount(UUID.randomUUID(), accountId);
            card2.setCardType(CardType.PHYSICAL);

            repository.saveAll(List.of(card1, card2));

            // When
            List<CardEntity> results = repository.findByAccountId(accountId);

            // Then
            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Exists By Card Number Encrypted")
    class ExistsByCardNumberEncryptedTests {

        @Test
        @DisplayName("Should return true when encrypted card number exists")
        void shouldReturnTrueWhenEncryptedCardNumberExists() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setCardNumberEncrypted("encrypted_card_number");
            repository.save(card);

            // When
            boolean exists = repository.existsByCardNumberEncrypted("encrypted_card_number");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when encrypted card number does not exist")
        void shouldReturnFalseWhenEncryptedCardNumberDoesNotExist() {
            // Given
            String nonExistentEncryptedNumber = "non_existent_encrypted";

            // When
            boolean exists = repository.existsByCardNumberEncrypted(nonExistentEncryptedNumber);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Unique Constraints")
    class UniqueConstraintsTests {

        @Test
        @DisplayName("Should enforce unique card number masked constraint")
        void shouldEnforceUniqueCardNumberMaskedConstraint() {
            // Given
            UUID id1 = UUID.randomUUID();
            CardEntity card1 = createTestCard(id1);
            card1.setCardNumberMasked("****-****-****-0366");
            repository.save(card1);
            entityManager.flush();

            UUID id2 = UUID.randomUUID();
            CardEntity card2 = createTestCard(id2);
            card2.setCardNumberMasked("****-****-****-0366");

            // When/Then - Hibernate throws ConstraintViolationException which is wrapped by Spring
            org.junit.jupiter.api.Assertions.assertThrows(
                    org.hibernate.exception.ConstraintViolationException.class,
                    () -> {
                        repository.save(card2);
                        entityManager.flush();
                    }
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null spending limit")
        void shouldHandleNullSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setSpendingLimit(null);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getSpendingLimit()).isNull();
        }

        @Test
        @DisplayName("Should handle null activated at timestamp")
        void shouldHandleNullActivatedAtTimestamp() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setActivatedAt(null);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getActivatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null MCC blocks JSON")
        void shouldHandleNullMccBlocksJson() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setMccBlocksJson(null);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getMccBlocksJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty MCC blocks JSON")
        void shouldHandleEmptyMccBlocksJson() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setMccBlocksJson("[]");

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getMccBlocksJson()).isEqualTo("[]");
        }

        @Test
        @DisplayName("Should handle zero spending limit")
        void shouldHandleZeroSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setSpendingLimit(BigDecimal.ZERO);

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getSpendingLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle spending limit with 4 decimal places")
        void shouldHandleSpendingLimitWith4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setSpendingLimit(new BigDecimal("123.4567"));

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large spending limit")
        void shouldHandleVeryLargeSpendingLimit() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setSpendingLimit(new BigDecimal("10000000.00"));

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle long cardholder name")
        void shouldHandleLongCardholderName() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setCardholderName("a".repeat(100));

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getCardholderName()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle cardholder name with special characters")
        void shouldHandleCardholderNameWithSpecialCharacters() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setCardholderName("John O'Brien-Smith");

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getCardholderName()).isEqualTo("John O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle cardholder name with unicode")
        void shouldHandleCardholderNameWithUnicode() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card.setCardholderName("用户 测试");

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getCardholderName()).isEqualTo("用户 测试");
        }

        @Test
        @DisplayName("Should handle deleting cards")
        void shouldHandleDeletingCards() {
            // Given
            UUID id = UUID.randomUUID();
            CardEntity card = createTestCard(id);
            card = repository.save(card);

            // When
            repository.delete(card);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all cards")
        void shouldHandleDeletingAllCards() {
            // Given
            List<CardEntity> cards = List.of(
                    createTestCard(UUID.randomUUID()),
                    createTestCard(UUID.randomUUID()),
                    createTestCard(UUID.randomUUID())
            );
            repository.saveAll(cards);

            // When
            repository.deleteAll();

            // Then
            assertThat(repository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should handle card with all fields set")
        void shouldHandleCardWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardEntity card = new CardEntity();
            card.setId(id);
            card.setAccountId(accountId);
            card.setCardType(CardType.VIRTUAL);
            card.setStatus(CardStatus.ACTIVE);
            card.setSpendingLimit(new BigDecimal("5000.00"));
            card.setCardholderName("John Doe");
            card.setCardNumberMasked("****-****-****-0366");
            card.setCardNumberEncrypted("encrypted_card_number");
            card.setExpiryDate(YearMonth.now().plusYears(4));
            card.setCvvEncrypted("encrypted_cvv");
            card.setCreatedAt(Instant.now());
            card.setActivatedAt(Instant.now());
            card.setMccBlocksJson("[5999,7995]");
            card.setMonthlySpent(new BigDecimal("1500.00"));

            // When
            CardEntity saved = repository.save(card);

            // Then
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getAccountId()).isEqualTo(accountId);
            assertThat(saved.getCardType()).isEqualTo(CardType.VIRTUAL);
            assertThat(saved.getStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(saved.getSpendingLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(saved.getCardholderName()).isEqualTo("John Doe");
            assertThat(saved.getCardNumberMasked()).isEqualTo("****-****-****-0366");
            assertThat(saved.getExpiryDate()).isEqualTo(YearMonth.now().plusYears(4));
            assertThat(saved.getMccBlocksJson()).isEqualTo("[5999,7995]");
            assertThat(saved.getMonthlySpent()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }
    }

    /**
     * Helper method to create a test card.
     */
    private CardEntity createTestCard(UUID id) {
        CardEntity card = new CardEntity();
        card.setId(id);
        card.setAccountId(UUID.randomUUID());
        card.setCardType(CardType.VIRTUAL);
        card.setStatus(CardStatus.ACTIVE);
        card.setSpendingLimit(new BigDecimal("5000.00"));
        card.setCardholderName("John Doe");
        card.setCardNumberMasked("****-****-****-" + String.format("%04d", id.hashCode() % 10000));
        card.setCardNumberEncrypted("encrypted_card_number");
        card.setExpiryDate(YearMonth.now().plusYears(4));
        card.setCvvEncrypted("encrypted_cvv");
        card.setCreatedAt(Instant.now());
        return card;
    }

    /**
     * Helper method to create a test card with specific account.
     */
    private CardEntity createTestCardWithAccount(UUID id, UUID accountId) {
        CardEntity card = createTestCard(id);
        card.setAccountId(accountId);
        return card;
    }
}
