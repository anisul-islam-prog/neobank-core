package com.neobank.core.transfers.internal;

import com.neobank.core.transfers.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransferMapper using JUnit 5.
 * Tests entity-to-request and request-to-entity mapping.
 */
@DisplayName("TransferMapper Unit Tests")
class TransferMapperTest {

    private TransferMapper transferMapper;

    @BeforeEach
    void setUp() {
        transferMapper = new TransferMapper();
    }

    @Nested
    @DisplayName("To Entity Mapping")
    class ToEntityMappingTests {

        @Test
        @DisplayName("Should map TransferRequest to TransferEntity")
        void shouldMapTransferRequestToTransferEntity() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            TransferRequest request = new TransferRequest(fromId, toId, amount, currency, null, null, null);

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getFromAccountId()).isEqualTo(fromId);
            assertThat(entity.getToAccountId()).isEqualTo(toId);
            assertThat(entity.getAmount()).isEqualByComparingTo(amount);
            assertThat(entity.getCurrency()).isEqualTo(currency);
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.PENDING);
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set PENDING status for new transfer")
        void shouldSetPendingStatusForNewTransfer() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.PENDING);
        }

        @Test
        @DisplayName("Should set created at timestamp")
        void shouldSetCreatedAtTimestamp() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(fromId, toId, amount);
            Instant beforeMapping = Instant.now();

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getCreatedAt()).isAfterOrEqualTo(beforeMapping);
        }

        @Test
        @DisplayName("Should map request with default currency (USD)")
        void shouldMapRequestWithDefaultCurrency() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should map request with custom currency")
        void shouldMapRequestWithCustomCurrency() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(fromId, toId, amount, "EUR");

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getCurrency()).isEqualTo("EUR");
        }
    }

    @Nested
    @DisplayName("To Completed Entity Mapping")
    class ToCompletedEntityMappingTests {

        @Test
        @DisplayName("Should set COMPLETED status")
        void shouldSetCompletedStatus() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            entity.setStatus(TransferStatus.PENDING);

            // When
            transferMapper.toCompletedEntity(entity);

            // Then
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should set completed at timestamp")
        void shouldSetCompletedAtTimestamp() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            Instant beforeMapping = Instant.now();

            // When
            transferMapper.toCompletedEntity(entity);

            // Then
            assertThat(entity.getCompletedAt()).isNotNull();
            assertThat(entity.getCompletedAt()).isAfterOrEqualTo(beforeMapping);
        }

        @Test
        @DisplayName("Should not change failure reason when completing")
        void shouldNotChangeFailureReasonWhenCompleting() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            entity.setFailureReason("Original failure reason");

            // When
            transferMapper.toCompletedEntity(entity);

            // Then
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.COMPLETED);
            assertThat(entity.getFailureReason()).isEqualTo("Original failure reason");
        }
    }

    @Nested
    @DisplayName("To Failed Entity Mapping")
    class ToFailedEntityMappingTests {

        @Test
        @DisplayName("Should set FAILED status")
        void shouldSetFailedStatus() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            String reason = "Insufficient balance";

            // When
            transferMapper.toFailedEntity(entity, reason);

            // Then
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.FAILED);
        }

        @Test
        @DisplayName("Should set failure reason")
        void shouldSetFailureReason() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            String reason = "Insufficient balance";

            // When
            transferMapper.toFailedEntity(entity, reason);

            // Then
            assertThat(entity.getFailureReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should set completed at timestamp")
        void shouldSetCompletedAtTimestampForFailed() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            Instant beforeMapping = Instant.now();

            // When
            transferMapper.toFailedEntity(entity, "Test failure");

            // Then
            assertThat(entity.getCompletedAt()).isNotNull();
            assertThat(entity.getCompletedAt()).isAfterOrEqualTo(beforeMapping);
        }

        @Test
        @DisplayName("Should handle null failure reason")
        void shouldHandleNullFailureReason() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());

            // When
            transferMapper.toFailedEntity(entity, null);

            // Then
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.FAILED);
            assertThat(entity.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("Should handle empty failure reason")
        void shouldHandleEmptyFailureReason() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());

            // When
            transferMapper.toFailedEntity(entity, "");

            // Then
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.FAILED);
            assertThat(entity.getFailureReason()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long failure reason")
        void shouldHandleLongFailureReason() {
            // Given
            TransferEntity entity = new TransferEntity();
            entity.setId(UUID.randomUUID());
            String longReason = "a".repeat(400);

            // When
            transferMapper.toFailedEntity(entity, longReason);

            // Then
            assertThat(entity.getFailureReason()).hasSize(400);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle request with zero amount")
        void shouldHandleRequestWithZeroAmount() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            TransferRequest request = new TransferRequest(fromId, toId, BigDecimal.ZERO);

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle request with negative amount")
        void shouldHandleRequestWithNegativeAmount() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("-100.00"));

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle request with amount having 4 decimal places")
        void shouldHandleRequestWithAmountHaving4DecimalPlaces() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("123.4567"));

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle request with very large amount")
        void shouldHandleRequestWithVeryLargeAmount() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("10000000.00"));

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle request with different currencies")
        void shouldHandleRequestWithDifferentCurrencies() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF"};

            for (String currency : currencies) {
                TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("500.00"), currency);

                // When
                TransferEntity entity = transferMapper.toEntity(request);

                // Then
                assertThat(entity.getCurrency()).isEqualTo(currency);
            }
        }

        @Test
        @DisplayName("Should handle entity with same from and to account")
        void shouldHandleEntityWithSameFromAndToAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(accountId, accountId, amount);

            // When
            TransferEntity entity = transferMapper.toEntity(request);

            // Then
            assertThat(entity.getFromAccountId()).isEqualTo(accountId);
            assertThat(entity.getToAccountId()).isEqualTo(accountId);
        }
    }

    @Nested
    @DisplayName("Complete Mapping Flow")
    class CompleteMappingFlowTests {

        @Test
        @DisplayName("Should complete full mapping flow from request to completed entity")
        void shouldCompleteFullMappingFlowFromRequestToCompletedEntity() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", null, null, "Test transfer");

            // When - Create entity
            TransferEntity entity = transferMapper.toEntity(request);

            // Then - Verify initial state
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.PENDING);
            assertThat(entity.getCompletedAt()).isNull();
            assertThat(entity.getFailureReason()).isNull();

            // When - Complete entity
            transferMapper.toCompletedEntity(entity);

            // Then - Verify completed state
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.COMPLETED);
            assertThat(entity.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should complete full mapping flow from request to failed entity")
        void shouldCompleteFullMappingFlowFromRequestToFailedEntity() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", null, null, "Test transfer");
            String failureReason = "Insufficient balance";

            // When - Create entity
            TransferEntity entity = transferMapper.toEntity(request);

            // Then - Verify initial state
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.PENDING);

            // When - Fail entity
            transferMapper.toFailedEntity(entity, failureReason);

            // Then - Verify failed state
            assertThat(entity.getStatus()).isEqualTo(TransferStatus.FAILED);
            assertThat(entity.getCompletedAt()).isNotNull();
            assertThat(entity.getFailureReason()).isEqualTo(failureReason);
        }
    }
}
