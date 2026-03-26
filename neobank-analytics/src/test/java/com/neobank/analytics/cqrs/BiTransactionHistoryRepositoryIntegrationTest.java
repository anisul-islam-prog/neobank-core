package com.neobank.analytics.cqrs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for BiTransactionHistoryRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("BiTransactionHistoryRepository Integration Tests")
class BiTransactionHistoryRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private BiTransactionHistoryRepository repository;

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
        @DisplayName("Should save and retrieve BI record by ID")
        void shouldSaveAndRetrieveBiRecordById() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);

            // When
            BiTransactionHistory saved = repository.save(bi);
            BiTransactionHistory retrieved = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getAmount()).isEqualByComparingTo(bi.getAmount());
            assertThat(retrieved.getCurrency()).isEqualTo(bi.getCurrency());
        }

        @Test
        @DisplayName("Should save BI record with COMPLETED status")
        void shouldSaveBiRecordWithCompletedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setStatus("COMPLETED");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("Should save BI record with PENDING status")
        void shouldSaveBiRecordWithPendingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setStatus("PENDING");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("Should save BI record with FAILED status")
        void shouldSaveBiRecordWithFailedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setStatus("FAILED");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getStatus()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("Should save BI record with balance snapshots")
        void shouldSaveBiRecordWithBalanceSnapshots() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setFromBalanceBefore(new BigDecimal("1000.00"));
            bi.setFromBalanceAfter(new BigDecimal("500.00"));
            bi.setToBalanceBefore(new BigDecimal("200.00"));
            bi.setToBalanceAfter(new BigDecimal("700.00"));

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getFromBalanceBefore()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(saved.getFromBalanceAfter()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(saved.getToBalanceBefore()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(saved.getToBalanceAfter()).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Should save BI record with metadata")
        void shouldSaveBiRecordWithMetadata() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setMetadata("{\"key\":\"value\"}");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getMetadata()).isEqualTo("{\"key\":\"value\"}");
        }

        @Test
        @DisplayName("Should save BI record with owner names")
        void shouldSaveBiRecordWithOwnerNames() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setFromOwnerName("John Doe");
            bi.setToOwnerName("Jane Doe");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getFromOwnerName()).isEqualTo("John Doe");
            assertThat(saved.getToOwnerName()).isEqualTo("Jane Doe");
        }
    }

    @Nested
    @DisplayName("Find By From Account ID")
    class FindByFromAccountIdTests {

        @Test
        @DisplayName("Should find all BI records for from account")
        void shouldFindAllBiRecordsForFromAccount() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());

            // When
            List<BiTransactionHistory> results = repository.findByFromAccountIdOrderByOccurredAtDesc(fromAccountId);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results).allMatch(bi -> bi.getFromAccountId().equals(fromAccountId));
        }

        @Test
        @DisplayName("Should order results by occurred at descending")
        void shouldOrderResultsByOccurredAtDescending() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            Instant now = Instant.now();

            BiTransactionHistory bi1 = createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            bi1.setOccurredAt(now.minusSeconds(3600));

            BiTransactionHistory bi2 = createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            bi2.setOccurredAt(now);

            BiTransactionHistory bi3 = createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            bi3.setOccurredAt(now.minusSeconds(7200));

            repository.saveAll(List.of(bi1, bi2, bi3));

            // When
            List<BiTransactionHistory> results = repository.findByFromAccountIdOrderByOccurredAtDesc(fromAccountId);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).getOccurredAt()).isEqualTo(now);
            assertThat(results.get(1).getOccurredAt()).isEqualTo(now.minusSeconds(3600));
            assertThat(results.get(2).getOccurredAt()).isEqualTo(now.minusSeconds(7200));
        }

        @Test
        @DisplayName("Should return empty list when no records for from account")
        void shouldReturnEmptyListWhenNoRecordsForFromAccount() {
            // Given
            UUID nonExistentAccountId = UUID.randomUUID();

            // When
            List<BiTransactionHistory> results = repository.findByFromAccountIdOrderByOccurredAtDesc(nonExistentAccountId);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By To Account ID")
    class FindByToAccountIdTests {

        @Test
        @DisplayName("Should find all BI records for to account")
        void shouldFindAllBiRecordsForToAccount() {
            // Given
            UUID toAccountId = UUID.randomUUID();
            createTestBiRecordWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);
            createTestBiRecordWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);

            // When
            List<BiTransactionHistory> results = repository.findByToAccountIdOrderByOccurredAtDesc(toAccountId);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(bi -> bi.getToAccountId().equals(toAccountId));
        }

        @Test
        @DisplayName("Should return empty list when no records for to account")
        void shouldReturnEmptyListWhenNoRecordsForToAccount() {
            // Given
            UUID nonExistentAccountId = UUID.randomUUID();

            // When
            List<BiTransactionHistory> results = repository.findByToAccountIdOrderByOccurredAtDesc(nonExistentAccountId);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Total Outflow")
    class GetTotalOutflowTests {

        @Test
        @DisplayName("Should calculate total outflow for account in date range")
        void shouldCalculateTotalOutflowForAccountInDateRange() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant start = now.minusSeconds(7200);
            Instant end = now;

            BiTransactionHistory bi1 = createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            bi1.setAmount(new BigDecimal("100.00"));
            bi1.setOccurredAt(now.minusSeconds(3600));

            BiTransactionHistory bi2 = createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            bi2.setAmount(new BigDecimal("200.00"));
            bi2.setOccurredAt(now.minusSeconds(1800));

            BiTransactionHistory bi3 = createTestBiRecordWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            bi3.setAmount(new BigDecimal("300.00"));
            bi3.setOccurredAt(now.minusSeconds(10000)); // Outside range

            repository.saveAll(List.of(bi1, bi2, bi3));

            // When
            BigDecimal totalOutflow = repository.getTotalOutflow(fromAccountId, start, end);

            // Then
            assertThat(totalOutflow).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("Should return null when no outflow in date range")
        void shouldReturnNullWhenNoOutflowInDateRange() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            Instant start = Instant.now().minusSeconds(7200);
            Instant end = Instant.now();

            // When
            BigDecimal totalOutflow = repository.getTotalOutflow(fromAccountId, start, end);

            // Then
            assertThat(totalOutflow).isNull();
        }
    }

    @Nested
    @DisplayName("Get Total Inflow")
    class GetTotalInflowTests {

        @Test
        @DisplayName("Should calculate total inflow for account in date range")
        void shouldCalculateTotalInflowForAccountInDateRange() {
            // Given
            UUID toAccountId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant start = now.minusSeconds(7200);
            Instant end = now;

            BiTransactionHistory bi1 = createTestBiRecordWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);
            bi1.setAmount(new BigDecimal("100.00"));
            bi1.setOccurredAt(now.minusSeconds(3600));

            BiTransactionHistory bi2 = createTestBiRecordWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);
            bi2.setAmount(new BigDecimal("200.00"));
            bi2.setOccurredAt(now.minusSeconds(1800));

            BiTransactionHistory bi3 = createTestBiRecordWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);
            bi3.setAmount(new BigDecimal("300.00"));
            bi3.setOccurredAt(now.minusSeconds(10000)); // Outside range

            repository.saveAll(List.of(bi1, bi2, bi3));

            // When
            BigDecimal totalInflow = repository.getTotalInflow(toAccountId, start, end);

            // Then
            assertThat(totalInflow).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("Should return null when no inflow in date range")
        void shouldReturnNullWhenNoInflowInDateRange() {
            // Given
            UUID toAccountId = UUID.randomUUID();
            Instant start = Instant.now().minusSeconds(7200);
            Instant end = Instant.now();

            // When
            BigDecimal totalInflow = repository.getTotalInflow(toAccountId, start, end);

            // Then
            assertThat(totalInflow).isNull();
        }
    }

    @Nested
    @DisplayName("Get Volume By Currency")
    class GetVolumeByCurrencyTests {

        @Test
        @DisplayName("Should group transaction volume by currency")
        void shouldGroupTransactionVolumeByCurrency() {
            // Given
            Instant now = Instant.now();
            Instant start = now.minusSeconds(7200);
            Instant end = now;

            BiTransactionHistory bi1 = createTestBiRecord(UUID.randomUUID());
            bi1.setAmount(new BigDecimal("100.00"));
            bi1.setCurrency("USD");
            bi1.setOccurredAt(now.minusSeconds(3600));

            BiTransactionHistory bi2 = createTestBiRecord(UUID.randomUUID());
            bi2.setAmount(new BigDecimal("200.00"));
            bi2.setCurrency("USD");
            bi2.setOccurredAt(now.minusSeconds(1800));

            BiTransactionHistory bi3 = createTestBiRecord(UUID.randomUUID());
            bi3.setAmount(new BigDecimal("150.00"));
            bi3.setCurrency("EUR");
            bi3.setOccurredAt(now.minusSeconds(1800));

            repository.saveAll(List.of(bi1, bi2, bi3));

            // When
            List<Object[]> results = repository.getVolumeByCurrency(start, end);

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no transactions in date range")
        void shouldReturnEmptyListWhenNoTransactionsInDateRange() {
            // Given
            Instant start = Instant.now().minusSeconds(7200);
            Instant end = Instant.now();

            // When
            List<Object[]> results = repository.getVolumeByCurrency(start, end);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Daily Transaction Count")
    class GetDailyTransactionCountTests {

        @Test
        @DisplayName("Should group transactions by date")
        void shouldGroupTransactionsByDate() {
            // Given
            Instant day1 = Instant.parse("2024-01-15T10:00:00Z");
            Instant day2 = Instant.parse("2024-01-16T10:00:00Z");
            Instant start = day1.minusSeconds(3600);
            Instant end = day2.plusSeconds(3600);

            BiTransactionHistory bi1 = createTestBiRecord(UUID.randomUUID());
            bi1.setOccurredAt(day1);

            BiTransactionHistory bi2 = createTestBiRecord(UUID.randomUUID());
            bi2.setOccurredAt(day1.plusSeconds(3600));

            BiTransactionHistory bi3 = createTestBiRecord(UUID.randomUUID());
            bi3.setOccurredAt(day2);

            repository.saveAll(List.of(bi1, bi2, bi3));

            // When
            List<Object[]> results = repository.getDailyTransactionCount(start, end);

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no transactions in date range")
        void shouldReturnEmptyListWhenNoTransactionsInDateRange() {
            // Given
            Instant start = Instant.now().minusSeconds(7200);
            Instant end = Instant.now();

            // When
            List<Object[]> results = repository.getDailyTransactionCount(start, end);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null metadata")
        void shouldHandleNullMetadata() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setMetadata(null);

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata")
        void shouldHandleEmptyMetadata() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setMetadata("{}");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getMetadata()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long metadata")
        void shouldHandleLongMetadata() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setMetadata("a".repeat(400));

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getMetadata()).hasSize(400);
        }

        @Test
        @DisplayName("Should handle amount with 4 decimal places")
        void shouldHandleAmountWith4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setAmount(new BigDecimal("123.4567"));

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setAmount(BigDecimal.ZERO);

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative amount")
        void shouldHandleNegativeAmount() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setAmount(new BigDecimal("-100.00"));

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle very large amount")
        void shouldHandleVeryLargeAmount() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi.setAmount(new BigDecimal("10000000.00"));

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle deleting BI records")
        void shouldHandleDeletingBiRecords() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = createTestBiRecord(id);
            bi = repository.save(bi);

            // When
            repository.delete(bi);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all BI records")
        void shouldHandleDeletingAllBiRecords() {
            // Given
            List<BiTransactionHistory> records = List.of(
                    createTestBiRecord(UUID.randomUUID()),
                    createTestBiRecord(UUID.randomUUID()),
                    createTestBiRecord(UUID.randomUUID())
            );
            repository.saveAll(records);

            // When
            repository.deleteAll();

            // Then
            assertThat(repository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should handle BI record with all fields set")
        void shouldHandleBiRecordWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            BiTransactionHistory bi = new BiTransactionHistory();
            bi.setId(id);
            bi.setTransferId(UUID.randomUUID());
            bi.setFromAccountId(UUID.randomUUID());
            bi.setFromOwnerName("John Doe");
            bi.setToAccountId(UUID.randomUUID());
            bi.setToOwnerName("Jane Doe");
            bi.setAmount(new BigDecimal("500.00"));
            bi.setCurrency("USD");
            bi.setStatus("COMPLETED");
            bi.setOccurredAt(Instant.now());
            bi.setProcessedAt(Instant.now());
            bi.setFromBalanceBefore(new BigDecimal("1000.00"));
            bi.setFromBalanceAfter(new BigDecimal("500.00"));
            bi.setToBalanceBefore(new BigDecimal("200.00"));
            bi.setToBalanceAfter(new BigDecimal("700.00"));
            bi.setChannel("CORE_BANKING");
            bi.setTransactionType("TRANSFER");
            bi.setMetadata("{\"key\":\"value\"}");

            // When
            BiTransactionHistory saved = repository.save(bi);

            // Then
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(saved.getCurrency()).isEqualTo("USD");
            assertThat(saved.getStatus()).isEqualTo("COMPLETED");
            assertThat(saved.getFromOwnerName()).isEqualTo("John Doe");
            assertThat(saved.getToOwnerName()).isEqualTo("Jane Doe");
        }
    }

    /**
     * Helper method to create a test BI record.
     */
    private BiTransactionHistory createTestBiRecord(UUID id) {
        BiTransactionHistory bi = new BiTransactionHistory();
        bi.setId(id);
        bi.setTransferId(UUID.randomUUID());
        bi.setFromAccountId(UUID.randomUUID());
        bi.setToAccountId(UUID.randomUUID());
        bi.setAmount(new BigDecimal("100.00"));
        bi.setCurrency("USD");
        bi.setStatus("COMPLETED");
        bi.setOccurredAt(Instant.now());
        bi.setTransactionType("TRANSFER");
        bi.setChannel("CORE_BANKING");
        return bi;
    }

    /**
     * Helper method to create a test BI record with specific accounts.
     */
    private BiTransactionHistory createTestBiRecordWithAccount(UUID id, UUID fromAccountId, UUID toAccountId) {
        BiTransactionHistory bi = createTestBiRecord(id);
        bi.setFromAccountId(fromAccountId);
        bi.setToAccountId(toAccountId);
        return bi;
    }
}
