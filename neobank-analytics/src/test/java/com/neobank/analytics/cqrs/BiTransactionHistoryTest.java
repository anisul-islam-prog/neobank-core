package com.neobank.analytics.cqrs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BiTransactionHistory entity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("BiTransactionHistory Unit Tests")
class BiTransactionHistoryTest {

    private BiTransactionHistory biTransaction;

    @BeforeEach
    void setUp() {
        biTransaction = new BiTransactionHistory();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            BiTransactionHistory entity = new BiTransactionHistory();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            biTransaction.setId(id);

            // Then
            assertThat(biTransaction.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get transfer ID")
        void shouldSetAndGetTransferId() {
            // Given
            UUID transferId = UUID.randomUUID();

            // When
            biTransaction.setTransferId(transferId);

            // Then
            assertThat(biTransaction.getTransferId()).isEqualTo(transferId);
        }

        @Test
        @DisplayName("Should set and get from account ID")
        void shouldSetAndGetFromAccountId() {
            // Given
            UUID fromAccountId = UUID.randomUUID();

            // When
            biTransaction.setFromAccountId(fromAccountId);

            // Then
            assertThat(biTransaction.getFromAccountId()).isEqualTo(fromAccountId);
        }

        @Test
        @DisplayName("Should set and get from owner name")
        void shouldSetAndGetFromOwnerName() {
            // Given
            String fromOwnerName = "John Doe";

            // When
            biTransaction.setFromOwnerName(fromOwnerName);

            // Then
            assertThat(biTransaction.getFromOwnerName()).isEqualTo(fromOwnerName);
        }

        @Test
        @DisplayName("Should set and get to account ID")
        void shouldSetAndGetToAccountId() {
            // Given
            UUID toAccountId = UUID.randomUUID();

            // When
            biTransaction.setToAccountId(toAccountId);

            // Then
            assertThat(biTransaction.getToAccountId()).isEqualTo(toAccountId);
        }

        @Test
        @DisplayName("Should set and get to owner name")
        void shouldSetAndGetToOwnerName() {
            // Given
            String toOwnerName = "Jane Doe";

            // When
            biTransaction.setToOwnerName(toOwnerName);

            // Then
            assertThat(biTransaction.getToOwnerName()).isEqualTo(toOwnerName);
        }

        @Test
        @DisplayName("Should set and get amount")
        void shouldSetAndGetAmount() {
            // Given
            BigDecimal amount = new BigDecimal("500.00");

            // When
            biTransaction.setAmount(amount);

            // Then
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should set and get currency")
        void shouldSetAndGetCurrency() {
            // Given
            String currency = "USD";

            // When
            biTransaction.setCurrency(currency);

            // Then
            assertThat(biTransaction.getCurrency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            String status = "COMPLETED";

            // When
            biTransaction.setStatus(status);

            // Then
            assertThat(biTransaction.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get occurred at timestamp")
        void shouldSetAndGetOccurredAtTimestamp() {
            // Given
            Instant occurredAt = Instant.now();

            // When
            biTransaction.setOccurredAt(occurredAt);

            // Then
            assertThat(biTransaction.getOccurredAt()).isEqualTo(occurredAt);
        }

        @Test
        @DisplayName("Should set and get processed at timestamp")
        void shouldSetAndGetProcessedAtTimestamp() {
            // Given
            Instant processedAt = Instant.now();

            // When
            biTransaction.setProcessedAt(processedAt);

            // Then
            assertThat(biTransaction.getProcessedAt()).isEqualTo(processedAt);
        }

        @Test
        @DisplayName("Should set and get from balance before")
        void shouldSetAndGetFromBalanceBefore() {
            // Given
            BigDecimal fromBalanceBefore = new BigDecimal("1000.00");

            // When
            biTransaction.setFromBalanceBefore(fromBalanceBefore);

            // Then
            assertThat(biTransaction.getFromBalanceBefore()).isEqualByComparingTo(fromBalanceBefore);
        }

        @Test
        @DisplayName("Should set and get from balance after")
        void shouldSetAndGetFromBalanceAfter() {
            // Given
            BigDecimal fromBalanceAfter = new BigDecimal("500.00");

            // When
            biTransaction.setFromBalanceAfter(fromBalanceAfter);

            // Then
            assertThat(biTransaction.getFromBalanceAfter()).isEqualByComparingTo(fromBalanceAfter);
        }

        @Test
        @DisplayName("Should set and get to balance before")
        void shouldSetAndGetToBalanceBefore() {
            // Given
            BigDecimal toBalanceBefore = new BigDecimal("200.00");

            // When
            biTransaction.setToBalanceBefore(toBalanceBefore);

            // Then
            assertThat(biTransaction.getToBalanceBefore()).isEqualByComparingTo(toBalanceBefore);
        }

        @Test
        @DisplayName("Should set and get to balance after")
        void shouldSetAndGetToBalanceAfter() {
            // Given
            BigDecimal toBalanceAfter = new BigDecimal("700.00");

            // When
            biTransaction.setToBalanceAfter(toBalanceAfter);

            // Then
            assertThat(biTransaction.getToBalanceAfter()).isEqualByComparingTo(toBalanceAfter);
        }

        @Test
        @DisplayName("Should set and get channel")
        void shouldSetAndGetChannel() {
            // Given
            String channel = "CORE_BANKING";

            // When
            biTransaction.setChannel(channel);

            // Then
            assertThat(biTransaction.getChannel()).isEqualTo(channel);
        }

        @Test
        @DisplayName("Should set and get transaction type")
        void shouldSetAndGetTransactionType() {
            // Given
            String transactionType = "TRANSFER";

            // When
            biTransaction.setTransactionType(transactionType);

            // Then
            assertThat(biTransaction.getTransactionType()).isEqualTo(transactionType);
        }

        @Test
        @DisplayName("Should set and get metadata")
        void shouldSetAndGetMetadata() {
            // Given
            String metadata = "{\"key\":\"value\"}";

            // When
            biTransaction.setMetadata(metadata);

            // Then
            assertThat(biTransaction.getMetadata()).isEqualTo(metadata);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            biTransaction.setId(null);

            // Then
            assertThat(biTransaction.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null transfer ID")
        void shouldHandleNullTransferId() {
            // When
            biTransaction.setTransferId(null);

            // Then
            assertThat(biTransaction.getTransferId()).isNull();
        }

        @Test
        @DisplayName("Should handle null from account ID")
        void shouldHandleNullFromAccountId() {
            // When
            biTransaction.setFromAccountId(null);

            // Then
            assertThat(biTransaction.getFromAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null from owner name")
        void shouldHandleNullFromOwnerName() {
            // When
            biTransaction.setFromOwnerName(null);

            // Then
            assertThat(biTransaction.getFromOwnerName()).isNull();
        }

        @Test
        @DisplayName("Should handle null to account ID")
        void shouldHandleNullToAccountId() {
            // When
            biTransaction.setToAccountId(null);

            // Then
            assertThat(biTransaction.getToAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null to owner name")
        void shouldHandleNullToOwnerName() {
            // When
            biTransaction.setToOwnerName(null);

            // Then
            assertThat(biTransaction.getToOwnerName()).isNull();
        }

        @Test
        @DisplayName("Should handle null amount")
        void shouldHandleNullAmount() {
            // When
            biTransaction.setAmount(null);

            // Then
            assertThat(biTransaction.getAmount()).isNull();
        }

        @Test
        @DisplayName("Should handle null currency")
        void shouldHandleNullCurrency() {
            // When
            biTransaction.setCurrency(null);

            // Then
            assertThat(biTransaction.getCurrency()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            biTransaction.setStatus(null);

            // Then
            assertThat(biTransaction.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null occurred at timestamp")
        void shouldHandleNullOccurredAtTimestamp() {
            // When
            biTransaction.setOccurredAt(null);

            // Then
            assertThat(biTransaction.getOccurredAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null processed at timestamp")
        void shouldHandleNullProcessedAtTimestamp() {
            // When
            biTransaction.setProcessedAt(null);

            // Then
            assertThat(biTransaction.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null from balance before")
        void shouldHandleNullFromBalanceBefore() {
            // When
            biTransaction.setFromBalanceBefore(null);

            // Then
            assertThat(biTransaction.getFromBalanceBefore()).isNull();
        }

        @Test
        @DisplayName("Should handle null from balance after")
        void shouldHandleNullFromBalanceAfter() {
            // When
            biTransaction.setFromBalanceAfter(null);

            // Then
            assertThat(biTransaction.getFromBalanceAfter()).isNull();
        }

        @Test
        @DisplayName("Should handle null to balance before")
        void shouldHandleNullToBalanceBefore() {
            // When
            biTransaction.setToBalanceBefore(null);

            // Then
            assertThat(biTransaction.getToBalanceBefore()).isNull();
        }

        @Test
        @DisplayName("Should handle null to balance after")
        void shouldHandleNullToBalanceAfter() {
            // When
            biTransaction.setToBalanceAfter(null);

            // Then
            assertThat(biTransaction.getToBalanceAfter()).isNull();
        }

        @Test
        @DisplayName("Should handle null channel")
        void shouldHandleNullChannel() {
            // When
            biTransaction.setChannel(null);

            // Then
            assertThat(biTransaction.getChannel()).isNull();
        }

        @Test
        @DisplayName("Should handle null transaction type")
        void shouldHandleNullTransactionType() {
            // When
            biTransaction.setTransactionType(null);

            // Then
            assertThat(biTransaction.getTransactionType()).isNull();
        }

        @Test
        @DisplayName("Should handle null metadata")
        void shouldHandleNullMetadata() {
            // When
            biTransaction.setMetadata(null);

            // Then
            assertThat(biTransaction.getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata")
        void shouldHandleEmptyMetadata() {
            // When
            biTransaction.setMetadata("{}");

            // Then
            assertThat(biTransaction.getMetadata()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long metadata")
        void shouldHandleLongMetadata() {
            // Given
            String longMetadata = "a".repeat(400);

            // When
            biTransaction.setMetadata(longMetadata);

            // Then
            assertThat(biTransaction.getMetadata()).hasSize(400);
        }

        @Test
        @DisplayName("Should handle amount with 4 decimal places")
        void shouldHandleAmountWith4DecimalPlaces() {
            // Given
            BigDecimal amount = new BigDecimal("123.4567");

            // When
            biTransaction.setAmount(amount);

            // Then
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            // Given
            BigDecimal amount = BigDecimal.ZERO;

            // When
            biTransaction.setAmount(amount);

            // Then
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative amount")
        void shouldHandleNegativeAmount() {
            // Given
            BigDecimal amount = new BigDecimal("-100.00");

            // When
            biTransaction.setAmount(amount);

            // Then
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should handle very large amount")
        void shouldHandleVeryLargeAmount() {
            // Given
            BigDecimal amount = new BigDecimal("10000000.00");

            // When
            biTransaction.setAmount(amount);

            // Then
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should handle different currencies")
        void shouldHandleDifferentCurrencies() {
            // Given
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF"};

            for (String currency : currencies) {
                // When
                biTransaction.setCurrency(currency);

                // Then
                assertThat(biTransaction.getCurrency()).isEqualTo(currency);
            }
        }

        @Test
        @DisplayName("Should handle different statuses")
        void shouldHandleDifferentStatuses() {
            // Given
            String[] statuses = {"COMPLETED", "PENDING", "FAILED", "REVERSED"};

            for (String status : statuses) {
                // When
                biTransaction.setStatus(status);

                // Then
                assertThat(biTransaction.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("Should handle different transaction types")
        void shouldHandleDifferentTransactionTypes() {
            // Given
            String[] types = {"TRANSFER", "DEPOSIT", "WITHDRAWAL", "PAYMENT"};

            for (String type : types) {
                // When
                biTransaction.setTransactionType(type);

                // Then
                assertThat(biTransaction.getTransactionType()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("Should handle different channels")
        void shouldHandleDifferentChannels() {
            // Given
            String[] channels = {"CORE_BANKING", "MOBILE", "WEB", "ATM", "BRANCH"};

            for (String channel : channels) {
                // When
                biTransaction.setChannel(channel);

                // Then
                assertThat(biTransaction.getChannel()).isEqualTo(channel);
            }
        }

        @Test
        @DisplayName("Should handle from owner name with special characters")
        void shouldHandleFromOwnerNameWithSpecialCharacters() {
            // Given
            String fromOwnerName = "John O'Brien-Smith";

            // When
            biTransaction.setFromOwnerName(fromOwnerName);

            // Then
            assertThat(biTransaction.getFromOwnerName()).isEqualTo(fromOwnerName);
        }

        @Test
        @DisplayName("Should handle from owner name with unicode")
        void shouldHandleFromOwnerNameWithUnicode() {
            // Given
            String fromOwnerName = "用户 测试";

            // When
            biTransaction.setFromOwnerName(fromOwnerName);

            // Then
            assertThat(biTransaction.getFromOwnerName()).isEqualTo(fromOwnerName);
        }

        @Test
        @DisplayName("Should handle to owner name with special characters")
        void shouldHandleToOwnerNameWithSpecialCharacters() {
            // Given
            String toOwnerName = "Jane O'Brien-Smith";

            // When
            biTransaction.setToOwnerName(toOwnerName);

            // Then
            assertThat(biTransaction.getToOwnerName()).isEqualTo(toOwnerName);
        }

        @Test
        @DisplayName("Should handle to owner name with unicode")
        void shouldHandleToOwnerNameWithUnicode() {
            // Given
            String toOwnerName = "用户 测试";

            // When
            biTransaction.setToOwnerName(toOwnerName);

            // Then
            assertThat(biTransaction.getToOwnerName()).isEqualTo(toOwnerName);
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
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            String fromOwnerName = "John Doe";
            UUID toAccountId = UUID.randomUUID();
            String toOwnerName = "Jane Doe";
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String status = "COMPLETED";
            Instant occurredAt = Instant.now();
            Instant processedAt = Instant.now();
            BigDecimal fromBalanceBefore = new BigDecimal("1000.00");
            BigDecimal fromBalanceAfter = new BigDecimal("500.00");
            BigDecimal toBalanceBefore = new BigDecimal("200.00");
            BigDecimal toBalanceAfter = new BigDecimal("700.00");
            String channel = "CORE_BANKING";
            String transactionType = "TRANSFER";
            String metadata = "{\"key\":\"value\"}";

            // When
            biTransaction.setId(id);
            biTransaction.setTransferId(transferId);
            biTransaction.setFromAccountId(fromAccountId);
            biTransaction.setFromOwnerName(fromOwnerName);
            biTransaction.setToAccountId(toAccountId);
            biTransaction.setToOwnerName(toOwnerName);
            biTransaction.setAmount(amount);
            biTransaction.setCurrency(currency);
            biTransaction.setStatus(status);
            biTransaction.setOccurredAt(occurredAt);
            biTransaction.setProcessedAt(processedAt);
            biTransaction.setFromBalanceBefore(fromBalanceBefore);
            biTransaction.setFromBalanceAfter(fromBalanceAfter);
            biTransaction.setToBalanceBefore(toBalanceBefore);
            biTransaction.setToBalanceAfter(toBalanceAfter);
            biTransaction.setChannel(channel);
            biTransaction.setTransactionType(transactionType);
            biTransaction.setMetadata(metadata);

            // Then
            assertThat(biTransaction.getId()).isEqualTo(id);
            assertThat(biTransaction.getTransferId()).isEqualTo(transferId);
            assertThat(biTransaction.getFromAccountId()).isEqualTo(fromAccountId);
            assertThat(biTransaction.getFromOwnerName()).isEqualTo(fromOwnerName);
            assertThat(biTransaction.getToAccountId()).isEqualTo(toAccountId);
            assertThat(biTransaction.getToOwnerName()).isEqualTo(toOwnerName);
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(amount);
            assertThat(biTransaction.getCurrency()).isEqualTo(currency);
            assertThat(biTransaction.getStatus()).isEqualTo(status);
            assertThat(biTransaction.getOccurredAt()).isEqualTo(occurredAt);
            assertThat(biTransaction.getProcessedAt()).isEqualTo(processedAt);
            assertThat(biTransaction.getFromBalanceBefore()).isEqualByComparingTo(fromBalanceBefore);
            assertThat(biTransaction.getFromBalanceAfter()).isEqualByComparingTo(fromBalanceAfter);
            assertThat(biTransaction.getToBalanceBefore()).isEqualByComparingTo(toBalanceBefore);
            assertThat(biTransaction.getToBalanceAfter()).isEqualByComparingTo(toBalanceAfter);
            assertThat(biTransaction.getChannel()).isEqualTo(channel);
            assertThat(biTransaction.getTransactionType()).isEqualTo(transactionType);
            assertThat(biTransaction.getMetadata()).isEqualTo(metadata);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String status = "COMPLETED";
            Instant occurredAt = Instant.now();

            // When
            biTransaction.setId(id);
            biTransaction.setTransferId(transferId);
            biTransaction.setFromAccountId(fromAccountId);
            biTransaction.setToAccountId(toAccountId);
            biTransaction.setAmount(amount);
            biTransaction.setCurrency(currency);
            biTransaction.setStatus(status);
            biTransaction.setOccurredAt(occurredAt);

            // Then
            assertThat(biTransaction.getId()).isEqualTo(id);
            assertThat(biTransaction.getTransferId()).isEqualTo(transferId);
            assertThat(biTransaction.getFromAccountId()).isEqualTo(fromAccountId);
            assertThat(biTransaction.getToAccountId()).isEqualTo(toAccountId);
            assertThat(biTransaction.getAmount()).isEqualByComparingTo(amount);
            assertThat(biTransaction.getCurrency()).isEqualTo(currency);
            assertThat(biTransaction.getStatus()).isEqualTo(status);
            assertThat(biTransaction.getOccurredAt()).isEqualTo(occurredAt);
            assertThat(biTransaction.getFromOwnerName()).isNull();
            assertThat(biTransaction.getToOwnerName()).isNull();
            assertThat(biTransaction.getProcessedAt()).isNull();
            assertThat(biTransaction.getChannel()).isNull();
            assertThat(biTransaction.getTransactionType()).isNull();
            assertThat(biTransaction.getMetadata()).isNull();
        }
    }
}
