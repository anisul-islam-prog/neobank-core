package com.neobank.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ReconciliationAlertRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("ReconciliationAlertRepository Integration Tests")
class ReconciliationAlertRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private ReconciliationAlertRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve alert by ID")
        void shouldSaveAndRetrieveAlertById() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);

            // When
            ReconciliationAlert saved = repository.save(alert);
            ReconciliationAlert retrieved = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getExpectedBalance()).isEqualByComparingTo(alert.getExpectedBalance());
            assertThat(retrieved.getActualBalance()).isEqualByComparingTo(alert.getActualBalance());
        }

        @Test
        @DisplayName("Should save alert with PENDING status")
        void shouldSaveAlertWithPendingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.PENDING);
        }

        @Test
        @DisplayName("Should save alert with INVESTIGATING status")
        void shouldSaveAlertWithInvestigatingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setStatus(ReconciliationAlert.AlertStatus.INVESTIGATING);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.INVESTIGATING);
        }

        @Test
        @DisplayName("Should save alert with RESOLVED status")
        void shouldSaveAlertWithResolvedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.RESOLVED);
        }

        @Test
        @DisplayName("Should save alert with FALSE_POSITIVE status")
        void shouldSaveAlertWithFalsePositiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setStatus(ReconciliationAlert.AlertStatus.FALSE_POSITIVE);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.FALSE_POSITIVE);
        }

        @Test
        @DisplayName("Should save alert with resolution information")
        void shouldSaveAlertWithResolutionInformation() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);
            alert.setResolvedAt(Instant.now());
            alert.setResolvedBy("admin_user");

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getResolvedAt()).isNotNull();
            assertThat(saved.getResolvedBy()).isEqualTo("admin_user");
        }

        @Test
        @DisplayName("Should save alert with details")
        void shouldSaveAlertWithDetails() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setDetails("Balance mismatch detected during daily reconciliation");

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getDetails()).isEqualTo("Balance mismatch detected during daily reconciliation");
        }
    }

    @Nested
    @DisplayName("Find By Status")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find all pending alerts")
        void shouldFindAllPendingAlerts() {
            // Given
            ReconciliationAlert alert1 = createTestAlert(UUID.randomUUID());
            alert1.setStatus(ReconciliationAlert.AlertStatus.PENDING);
            repository.save(alert1);

            ReconciliationAlert alert2 = createTestAlert(UUID.randomUUID());
            alert2.setStatus(ReconciliationAlert.AlertStatus.PENDING);
            repository.save(alert2);

            ReconciliationAlert alert3 = createTestAlert(UUID.randomUUID());
            alert3.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);
            repository.save(alert3);

            // When
            List<ReconciliationAlert> results = repository.findByStatusOrderByAlertDateDesc(ReconciliationAlert.AlertStatus.PENDING);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(a -> a.getStatus() == ReconciliationAlert.AlertStatus.PENDING);
        }

        @Test
        @DisplayName("Should find all resolved alerts")
        void shouldFindAllResolvedAlerts() {
            // Given
            ReconciliationAlert alert1 = createTestAlert(UUID.randomUUID());
            alert1.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);
            repository.save(alert1);

            ReconciliationAlert alert2 = createTestAlert(UUID.randomUUID());
            alert2.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);
            repository.save(alert2);

            // When
            List<ReconciliationAlert> results = repository.findByStatusOrderByAlertDateDesc(ReconciliationAlert.AlertStatus.RESOLVED);

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should order results by alert date descending")
        void shouldOrderResultsByAlertDateDescending() {
            // Given
            Instant now = Instant.now();

            ReconciliationAlert alert1 = createTestAlert(UUID.randomUUID());
            alert1.setStatus(ReconciliationAlert.AlertStatus.PENDING);
            alert1.setAlertDate(now.minusSeconds(3600));
            repository.save(alert1);

            ReconciliationAlert alert2 = createTestAlert(UUID.randomUUID());
            alert2.setStatus(ReconciliationAlert.AlertStatus.PENDING);
            alert2.setAlertDate(now);
            repository.save(alert2);

            ReconciliationAlert alert3 = createTestAlert(UUID.randomUUID());
            alert3.setStatus(ReconciliationAlert.AlertStatus.PENDING);
            alert3.setAlertDate(now.minusSeconds(7200));
            repository.save(alert3);

            // When
            List<ReconciliationAlert> results = repository.findByStatusOrderByAlertDateDesc(ReconciliationAlert.AlertStatus.PENDING);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).getAlertDate()).isEqualTo(now);
            assertThat(results.get(1).getAlertDate()).isEqualTo(now.minusSeconds(3600));
            assertThat(results.get(2).getAlertDate()).isEqualTo(now.minusSeconds(7200));
        }

        @Test
        @DisplayName("Should return empty list when no alerts for status")
        void shouldReturnEmptyListWhenNoAlertsForStatus() {
            // When
            List<ReconciliationAlert> results = repository.findByStatusOrderByAlertDateDesc(ReconciliationAlert.AlertStatus.PENDING);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Date")
    class FindByDateTests {

        @Test
        @DisplayName("Should find all alerts after specified date")
        void shouldFindAllAlertsAfterSpecifiedDate() {
            // Given
            Instant now = Instant.now();
            Instant searchDate = now.minusSeconds(3600);

            ReconciliationAlert alert1 = createTestAlert(UUID.randomUUID());
            alert1.setAlertDate(now.minusSeconds(1800)); // After search date
            repository.save(alert1);

            ReconciliationAlert alert2 = createTestAlert(UUID.randomUUID());
            alert2.setAlertDate(now); // After search date
            repository.save(alert2);

            ReconciliationAlert alert3 = createTestAlert(UUID.randomUUID());
            alert3.setAlertDate(now.minusSeconds(7200)); // Before search date
            repository.save(alert3);

            // When
            List<ReconciliationAlert> results = repository.findByDate(searchDate);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(a -> a.getAlertDate().isAfter(searchDate));
        }

        @Test
        @DisplayName("Should order results by alert date descending")
        void shouldOrderResultsByAlertDateDescending() {
            // Given
            Instant now = Instant.now();
            Instant searchDate = now.minusSeconds(7200);

            ReconciliationAlert alert1 = createTestAlert(UUID.randomUUID());
            alert1.setAlertDate(now.minusSeconds(3600));
            repository.save(alert1);

            ReconciliationAlert alert2 = createTestAlert(UUID.randomUUID());
            alert2.setAlertDate(now);
            repository.save(alert2);

            ReconciliationAlert alert3 = createTestAlert(UUID.randomUUID());
            alert3.setAlertDate(now.minusSeconds(5400));
            repository.save(alert3);

            // When
            List<ReconciliationAlert> results = repository.findByDate(searchDate);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).getAlertDate()).isEqualTo(now);
            assertThat(results.get(1).getAlertDate()).isEqualTo(now.minusSeconds(3600));
            assertThat(results.get(2).getAlertDate()).isEqualTo(now.minusSeconds(5400));
        }

        @Test
        @DisplayName("Should return empty list when no alerts after date")
        void shouldReturnEmptyListWhenNoAlertsAfterDate() {
            // Given
            Instant futureDate = Instant.now().plusSeconds(3600);

            // When
            List<ReconciliationAlert> results = repository.findByDate(futureDate);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null details")
        void shouldHandleNullDetails() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setDetails(null);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getDetails()).isNull();
        }

        @Test
        @DisplayName("Should handle empty details")
        void shouldHandleEmptyDetails() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setDetails("");

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getDetails()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long details string")
        void shouldHandleLongDetailsString() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setDetails("a".repeat(900));

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getDetails()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle zero expected balance")
        void shouldHandleZeroExpectedBalance() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setExpectedBalance(BigDecimal.ZERO);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getExpectedBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero actual balance")
        void shouldHandleZeroActualBalance() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setActualBalance(BigDecimal.ZERO);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getActualBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero difference")
        void shouldHandleZeroDifference() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setDifference(BigDecimal.ZERO);

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getDifference()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative difference")
        void shouldHandleNegativeDifference() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setDifference(new BigDecimal("-100.00"));

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getDifference()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle very large expected balance")
        void shouldHandleVeryLargeExpectedBalance() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setExpectedBalance(new BigDecimal("10000000.00"));

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getExpectedBalance()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle balance with 4 decimal places")
        void shouldHandleBalanceWith4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert.setExpectedBalance(new BigDecimal("123.4567"));

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getExpectedBalance()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle deleting alerts")
        void shouldHandleDeletingAlerts() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = createTestAlert(id);
            alert = repository.save(alert);

            // When
            repository.delete(alert);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all alerts")
        void shouldHandleDeletingAllAlerts() {
            // Given
            List<ReconciliationAlert> alerts = List.of(
                    createTestAlert(UUID.randomUUID()),
                    createTestAlert(UUID.randomUUID()),
                    createTestAlert(UUID.randomUUID())
            );
            repository.saveAll(alerts);

            // When
            repository.deleteAll();

            // Then
            assertThat(repository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should handle alert with all fields set")
        void shouldHandleAlertWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            ReconciliationAlert alert = new ReconciliationAlert();
            alert.setId(id);
            alert.setAlertDate(Instant.now());
            alert.setExpectedBalance(new BigDecimal("1000.00"));
            alert.setActualBalance(new BigDecimal("900.00"));
            alert.setDifference(new BigDecimal("100.00"));
            alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);
            alert.setDetails("Balance mismatch detected");
            alert.setCreatedAt(Instant.now());
            alert.setResolvedAt(Instant.now());
            alert.setResolvedBy("admin_user");

            // When
            ReconciliationAlert saved = repository.save(alert);

            // Then
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getExpectedBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(saved.getActualBalance()).isEqualByComparingTo(new BigDecimal("900.00"));
            assertThat(saved.getDifference()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(saved.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.PENDING);
            assertThat(saved.getDetails()).isEqualTo("Balance mismatch detected");
        }
    }

    /**
     * Helper method to create a test alert.
     */
    private ReconciliationAlert createTestAlert(UUID id) {
        ReconciliationAlert alert = new ReconciliationAlert();
        alert.setId(id);
        alert.setAlertDate(Instant.now());
        alert.setExpectedBalance(new BigDecimal("1000.00"));
        alert.setActualBalance(new BigDecimal("900.00"));
        alert.setDifference(new BigDecimal("100.00"));
        alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);
        alert.setCreatedAt(Instant.now());
        alert.setDetails("Test reconciliation alert");
        return alert;
    }
}
