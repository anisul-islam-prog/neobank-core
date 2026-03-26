package com.neobank.core.transfers.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransferEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("TransferEntity Unit Tests")
class TransferEntityTest {

    private TransferEntity transferEntity;

    @BeforeEach
    void setUp() {
        transferEntity = new TransferEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            TransferEntity entity = new TransferEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            transferEntity.setId(id);

            // Then
            assertThat(transferEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get from account ID")
        void shouldSetAndGetFromAccountId() {
            // Given
            UUID fromAccountId = UUID.randomUUID();

            // When
            transferEntity.setFromAccountId(fromAccountId);

            // Then
            assertThat(transferEntity.getFromAccountId()).isEqualTo(fromAccountId);
        }

        @Test
        @DisplayName("Should set and get to account ID")
        void shouldSetAndGetToAccountId() {
            // Given
            UUID toAccountId = UUID.randomUUID();

            // When
            transferEntity.setToAccountId(toAccountId);

            // Then
            assertThat(transferEntity.getToAccountId()).isEqualTo(toAccountId);
        }

        @Test
        @DisplayName("Should set and get amount")
        void shouldSetAndGetAmount() {
            // Given
            BigDecimal amount = new BigDecimal("500.00");

            // When
            transferEntity.setAmount(amount);

            // Then
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should set and get currency")
        void shouldSetAndGetCurrency() {
            // Given
            String currency = "USD";

            // When
            transferEntity.setCurrency(currency);

            // Then
            assertThat(transferEntity.getCurrency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            TransferStatus status = TransferStatus.COMPLETED;

            // When
            transferEntity.setStatus(status);

            // Then
            assertThat(transferEntity.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            transferEntity.setCreatedAt(createdAt);

            // Then
            assertThat(transferEntity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get completed at timestamp")
        void shouldSetAndGetCompletedAtTimestamp() {
            // Given
            Instant completedAt = Instant.now();

            // When
            transferEntity.setCompletedAt(completedAt);

            // Then
            assertThat(transferEntity.getCompletedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("Should set and get failure reason")
        void shouldSetAndGetFailureReason() {
            // Given
            String failureReason = "Insufficient balance";

            // When
            transferEntity.setFailureReason(failureReason);

            // Then
            assertThat(transferEntity.getFailureReason()).isEqualTo(failureReason);
        }
    }

    @Nested
    @DisplayName("TransferStatus Enum")
    class TransferStatusEnumTests {

        @Test
        @DisplayName("Should have PENDING status")
        void shouldHavePendingStatus() {
            // Then
            assertThat(TransferStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("Should have PROCESSING status")
        void shouldHaveProcessingStatus() {
            // Then
            assertThat(TransferStatus.PROCESSING).isNotNull();
        }

        @Test
        @DisplayName("Should have COMPLETED status")
        void shouldHaveCompletedStatus() {
            // Then
            assertThat(TransferStatus.COMPLETED).isNotNull();
        }

        @Test
        @DisplayName("Should have FAILED status")
        void shouldHaveFailedStatus() {
            // Then
            assertThat(TransferStatus.FAILED).isNotNull();
        }

        @Test
        @DisplayName("Should have REVERSED status")
        void shouldHaveReversedStatus() {
            // Then
            assertThat(TransferStatus.REVERSED).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 5 status values")
        void shouldHaveExactly5StatusValues() {
            // Then
            assertThat(TransferStatus.values()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionsTests {

        @Test
        @DisplayName("Should transition from PENDING to PROCESSING")
        void shouldTransitionFromPendingToProcessing() {
            // Given
            transferEntity.setStatus(TransferStatus.PENDING);

            // When
            transferEntity.setStatus(TransferStatus.PROCESSING);

            // Then
            assertThat(transferEntity.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should transition from PROCESSING to COMPLETED")
        void shouldTransitionFromProcessingToCompleted() {
            // Given
            transferEntity.setStatus(TransferStatus.PROCESSING);

            // When
            transferEntity.setStatus(TransferStatus.COMPLETED);

            // Then
            assertThat(transferEntity.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should transition from PROCESSING to FAILED")
        void shouldTransitionFromProcessingToFailed() {
            // Given
            transferEntity.setStatus(TransferStatus.PROCESSING);

            // When
            transferEntity.setStatus(TransferStatus.FAILED);

            // Then
            assertThat(transferEntity.getStatus()).isEqualTo(TransferStatus.FAILED);
        }

        @Test
        @DisplayName("Should transition from COMPLETED to REVERSED")
        void shouldTransitionFromCompletedToReversed() {
            // Given
            transferEntity.setStatus(TransferStatus.COMPLETED);

            // When
            transferEntity.setStatus(TransferStatus.REVERSED);

            // Then
            assertThat(transferEntity.getStatus()).isEqualTo(TransferStatus.REVERSED);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            transferEntity.setId(null);

            // Then
            assertThat(transferEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null from account ID")
        void shouldHandleNullFromAccountId() {
            // When
            transferEntity.setFromAccountId(null);

            // Then
            assertThat(transferEntity.getFromAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null to account ID")
        void shouldHandleNullToAccountId() {
            // When
            transferEntity.setToAccountId(null);

            // Then
            assertThat(transferEntity.getToAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null amount")
        void shouldHandleNullAmount() {
            // When
            transferEntity.setAmount(null);

            // Then
            assertThat(transferEntity.getAmount()).isNull();
        }

        @Test
        @DisplayName("Should handle null currency")
        void shouldHandleNullCurrency() {
            // When
            transferEntity.setCurrency(null);

            // Then
            assertThat(transferEntity.getCurrency()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            transferEntity.setStatus(null);

            // Then
            assertThat(transferEntity.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            transferEntity.setCreatedAt(null);

            // Then
            assertThat(transferEntity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null completed at timestamp")
        void shouldHandleNullCompletedAtTimestamp() {
            // When
            transferEntity.setCompletedAt(null);

            // Then
            assertThat(transferEntity.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null failure reason")
        void shouldHandleNullFailureReason() {
            // When
            transferEntity.setFailureReason(null);

            // Then
            assertThat(transferEntity.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("Should handle empty failure reason")
        void shouldHandleEmptyFailureReason() {
            // When
            transferEntity.setFailureReason("");

            // Then
            assertThat(transferEntity.getFailureReason()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long failure reason")
        void shouldHandleLongFailureReason() {
            // Given
            String longReason = "a".repeat(400);

            // When
            transferEntity.setFailureReason(longReason);

            // Then
            assertThat(transferEntity.getFailureReason()).hasSize(400);
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            // When
            transferEntity.setAmount(BigDecimal.ZERO);

            // Then
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative amount")
        void shouldHandleNegativeAmount() {
            // When
            transferEntity.setAmount(new BigDecimal("-100.00"));

            // Then
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle amount with 4 decimal places")
        void shouldHandleAmountWith4DecimalPlaces() {
            // When
            transferEntity.setAmount(new BigDecimal("123.4567"));

            // Then
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large amount")
        void shouldHandleVeryLargeAmount() {
            // When
            transferEntity.setAmount(new BigDecimal("10000000.00"));

            // Then
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle different currencies")
        void shouldHandleDifferentCurrencies() {
            // Given
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF"};

            for (String currency : currencies) {
                // When
                transferEntity.setCurrency(currency);

                // Then
                assertThat(transferEntity.getCurrency()).isEqualTo(currency);
            }
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
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            TransferStatus status = TransferStatus.COMPLETED;
            Instant createdAt = Instant.now();
            Instant completedAt = Instant.now();
            String failureReason = "Test failure";

            // When
            transferEntity.setId(id);
            transferEntity.setFromAccountId(fromAccountId);
            transferEntity.setToAccountId(toAccountId);
            transferEntity.setAmount(amount);
            transferEntity.setCurrency(currency);
            transferEntity.setStatus(status);
            transferEntity.setCreatedAt(createdAt);
            transferEntity.setCompletedAt(completedAt);
            transferEntity.setFailureReason(failureReason);

            // Then
            assertThat(transferEntity.getId()).isEqualTo(id);
            assertThat(transferEntity.getFromAccountId()).isEqualTo(fromAccountId);
            assertThat(transferEntity.getToAccountId()).isEqualTo(toAccountId);
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(amount);
            assertThat(transferEntity.getCurrency()).isEqualTo(currency);
            assertThat(transferEntity.getStatus()).isEqualTo(status);
            assertThat(transferEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(transferEntity.getCompletedAt()).isEqualTo(completedAt);
            assertThat(transferEntity.getFailureReason()).isEqualTo(failureReason);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            TransferStatus status = TransferStatus.PENDING;
            Instant createdAt = Instant.now();

            // When
            transferEntity.setId(id);
            transferEntity.setFromAccountId(fromAccountId);
            transferEntity.setToAccountId(toAccountId);
            transferEntity.setAmount(amount);
            transferEntity.setCurrency(currency);
            transferEntity.setStatus(status);
            transferEntity.setCreatedAt(createdAt);

            // Then
            assertThat(transferEntity.getId()).isEqualTo(id);
            assertThat(transferEntity.getFromAccountId()).isEqualTo(fromAccountId);
            assertThat(transferEntity.getToAccountId()).isEqualTo(toAccountId);
            assertThat(transferEntity.getAmount()).isEqualByComparingTo(amount);
            assertThat(transferEntity.getCurrency()).isEqualTo(currency);
            assertThat(transferEntity.getStatus()).isEqualTo(status);
            assertThat(transferEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(transferEntity.getCompletedAt()).isNull();
            assertThat(transferEntity.getFailureReason()).isNull();
        }
    }
}
