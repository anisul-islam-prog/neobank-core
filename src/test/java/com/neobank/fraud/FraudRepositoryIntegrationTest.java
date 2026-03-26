package com.neobank.fraud;

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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for FraudRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("FraudRepository Integration Tests")
class FraudRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private FraudRepository fraudRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        fraudRepository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve fraud alert by ID")
        void shouldSaveAndRetrieveFraudAlertById() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);
            FraudEntity retrieved = fraudRepository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getAlertType()).isEqualTo(fraudEntity.getAlertType());
            assertThat(retrieved.getRiskScore()).isEqualTo(fraudEntity.getRiskScore());
        }

        @Test
        @DisplayName("Should save fraud alert with PENDING status")
        void shouldSaveFraudAlertWithPendingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getStatus()).isEqualTo(FraudEntity.FraudStatus.PENDING);
        }

        @Test
        @DisplayName("Should save fraud alert with INVESTIGATING status")
        void shouldSaveFraudAlertWithInvestigatingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setStatus(FraudEntity.FraudStatus.INVESTIGATING);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getStatus()).isEqualTo(FraudEntity.FraudStatus.INVESTIGATING);
        }

        @Test
        @DisplayName("Should save fraud alert with CONFIRMED_FRAUD status")
        void shouldSaveFraudAlertWithConfirmedFraudStatus() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getStatus()).isEqualTo(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
        }

        @Test
        @DisplayName("Should save fraud alert with FALSE_POSITIVE status")
        void shouldSaveFraudAlertWithFalsePositiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setStatus(FraudEntity.FraudStatus.FALSE_POSITIVE);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getStatus()).isEqualTo(FraudEntity.FraudStatus.FALSE_POSITIVE);
        }

        @Test
        @DisplayName("Should save fraud alert with DISMISSED status")
        void shouldSaveFraudAlertWithDismissedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setStatus(FraudEntity.FraudStatus.DISMISSED);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getStatus()).isEqualTo(FraudEntity.FraudStatus.DISMISSED);
        }

        @Test
        @DisplayName("Should save fraud alert with review information")
        void shouldSaveFraudAlertWithReviewInformation() {
            // Given
            UUID id = UUID.randomUUID();
            UUID reviewedBy = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudEntity.setReviewedBy(reviewedBy);
            fraudEntity.setReviewedAt(Instant.now());
            fraudEntity.setReviewNotes("Confirmed as fraudulent");

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getReviewedBy()).isEqualTo(reviewedBy);
            assertThat(saved.getReviewedAt()).isNotNull();
            assertThat(saved.getReviewNotes()).isEqualTo("Confirmed as fraudulent");
        }

        @Test
        @DisplayName("Should save fraud alert with metadata JSON")
        void shouldSaveFraudAlertWithMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setMetadataJson("{\"pattern\":\"velocity\"}");

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{\"pattern\":\"velocity\"}");
        }
    }

    @Nested
    @DisplayName("Find By Transfer ID")
    class FindByTransferIdTests {

        @Test
        @DisplayName("Should find fraud alert by transfer ID")
        void shouldFindFraudAlertByTransferId() {
            // Given
            UUID transferId = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(UUID.randomUUID());
            fraudEntity.setTransferId(transferId);
            fraudRepository.save(fraudEntity);

            // When
            Optional<FraudEntity> result = fraudRepository.findByTransferId(transferId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTransferId()).isEqualTo(transferId);
        }

        @Test
        @DisplayName("Should return empty when fraud alert not found by transfer ID")
        void shouldReturnEmptyWhenFraudAlertNotFoundByTransferId() {
            // Given
            UUID nonExistentTransferId = UUID.randomUUID();

            // When
            Optional<FraudEntity> result = fraudRepository.findByTransferId(nonExistentTransferId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Account ID")
    class FindByAccountIdTests {

        @Test
        @DisplayName("Should find all fraud alerts for from account")
        void shouldFindAllFraudAlertsForFromAccount() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            createTestFraudEntityWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            createTestFraudEntityWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());
            createTestFraudEntityWithAccount(UUID.randomUUID(), fromAccountId, UUID.randomUUID());

            // When
            List<FraudEntity> results = fraudRepository.findByFromAccountIdOrderByCreatedAtDesc(fromAccountId);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results).allMatch(e -> e.getFromAccountId().equals(fromAccountId));
        }

        @Test
        @DisplayName("Should find all fraud alerts for to account")
        void shouldFindAllFraudAlertsForToAccount() {
            // Given
            UUID toAccountId = UUID.randomUUID();
            createTestFraudEntityWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);
            createTestFraudEntityWithAccount(UUID.randomUUID(), UUID.randomUUID(), toAccountId);

            // When
            List<FraudEntity> results = fraudRepository.findByToAccountIdOrderByCreatedAtDesc(toAccountId);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(e -> e.getToAccountId().equals(toAccountId));
        }

        @Test
        @DisplayName("Should return empty list when no fraud alerts for account")
        void shouldReturnEmptyListWhenNoFraudAlertsForAccount() {
            // Given
            UUID nonExistentAccountId = UUID.randomUUID();

            // When
            List<FraudEntity> results = fraudRepository.findByFromAccountIdOrderByCreatedAtDesc(nonExistentAccountId);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Status")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find all pending fraud alerts")
        void shouldFindAllPendingFraudAlerts() {
            // Given
            FraudEntity pending1 = createTestFraudEntity(UUID.randomUUID());
            pending1.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(pending1);

            FraudEntity pending2 = createTestFraudEntity(UUID.randomUUID());
            pending2.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(pending2);

            FraudEntity confirmed = createTestFraudEntity(UUID.randomUUID());
            confirmed.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed);

            // When
            List<FraudEntity> results = fraudRepository.findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus.PENDING);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(e -> e.getStatus() == FraudEntity.FraudStatus.PENDING);
        }

        @Test
        @DisplayName("Should find all confirmed fraud alerts")
        void shouldFindAllConfirmedFraudAlerts() {
            // Given
            FraudEntity confirmed1 = createTestFraudEntity(UUID.randomUUID());
            confirmed1.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed1);

            FraudEntity confirmed2 = createTestFraudEntity(UUID.randomUUID());
            confirmed2.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed2);

            // When
            List<FraudEntity> results = fraudRepository.findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus.CONFIRMED_FRAUD);

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no fraud alerts for status")
        void shouldReturnEmptyListWhenNoFraudAlertsForStatus() {
            // When
            List<FraudEntity> results = fraudRepository.findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus.PENDING);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Count By Status")
    class CountByStatusTests {

        @Test
        @DisplayName("Should count fraud alerts by status")
        void shouldCountFraudAlertsByStatus() {
            // Given
            FraudEntity pending = createTestFraudEntity(UUID.randomUUID());
            pending.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(pending);

            FraudEntity confirmed1 = createTestFraudEntity(UUID.randomUUID());
            confirmed1.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed1);

            FraudEntity confirmed2 = createTestFraudEntity(UUID.randomUUID());
            confirmed2.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed2);

            // When
            long pendingCount = fraudRepository.countByStatus(FraudEntity.FraudStatus.PENDING);
            long confirmedCount = fraudRepository.countByStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);

            // Then
            assertThat(pendingCount).isEqualTo(1);
            assertThat(confirmedCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return 0 when no fraud alerts for status")
        void shouldReturn0WhenNoFraudAlertsForStatus() {
            // When
            long count = fraudRepository.countByStatus(FraudEntity.FraudStatus.PENDING);

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Update Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update fraud alert status")
        void shouldUpdateFraudAlertStatus() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(alertId);
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(fraudEntity);

            // When
            int updated = fraudRepository.updateStatus(
                    alertId,
                    FraudEntity.FraudStatus.CONFIRMED_FRAUD,
                    reviewerId,
                    "Confirmed as fraudulent"
            );

            // Then
            assertThat(updated).isEqualTo(1);
            Optional<FraudEntity> updatedEntity = fraudRepository.findById(alertId);
            assertThat(updatedEntity).isPresent();
            assertThat(updatedEntity.get().getStatus()).isEqualTo(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            assertThat(updatedEntity.get().getReviewedBy()).isEqualTo(reviewerId);
        }

        @Test
        @DisplayName("Should confirm fraud alert")
        void shouldConfirmFraudAlert() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(alertId);
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(fraudEntity);

            // When
            int confirmed = fraudRepository.confirmFraud(alertId, reviewerId);

            // Then
            assertThat(confirmed).isEqualTo(1);
            Optional<FraudEntity> confirmedEntity = fraudRepository.findById(alertId);
            assertThat(confirmedEntity).isPresent();
            assertThat(confirmedEntity.get().getStatus()).isEqualTo(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
        }

        @Test
        @DisplayName("Should mark fraud alert as false positive")
        void shouldMarkFraudAlertAsFalsePositive() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(alertId);
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(fraudEntity);

            // When
            int marked = fraudRepository.markAsFalsePositive(alertId, reviewerId);

            // Then
            assertThat(marked).isEqualTo(1);
            Optional<FraudEntity> markedEntity = fraudRepository.findById(alertId);
            assertThat(markedEntity).isPresent();
            assertThat(markedEntity.get().getStatus()).isEqualTo(FraudEntity.FraudStatus.FALSE_POSITIVE);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null metadata JSON")
        void shouldHandleNullMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setMetadataJson(null);

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setMetadataJson("{}");

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long reason string")
        void shouldHandleLongReasonString() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setReason("a".repeat(900));

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getReason()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle long review notes string")
        void shouldHandleLongReviewNotesString() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity.setReviewNotes("b".repeat(900));

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getReviewNotes()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle deleting fraud alerts")
        void shouldHandleDeletingFraudAlerts() {
            // Given
            UUID id = UUID.randomUUID();
            FraudEntity fraudEntity = createTestFraudEntity(id);
            fraudEntity = fraudRepository.save(fraudEntity);

            // When
            fraudRepository.delete(fraudEntity);

            // Then
            assertThat(fraudRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all fraud alerts")
        void shouldHandleDeletingAllFraudAlerts() {
            // Given
            List<FraudEntity> fraudEntities = List.of(
                    createTestFraudEntity(UUID.randomUUID()),
                    createTestFraudEntity(UUID.randomUUID()),
                    createTestFraudEntity(UUID.randomUUID())
            );
            fraudRepository.saveAll(fraudEntities);

            // When
            fraudRepository.deleteAll();

            // Then
            assertThat(fraudRepository.findAll()).isEmpty();
        }
    }

    /**
     * Helper method to create a test fraud entity.
     */
    private FraudEntity createTestFraudEntity(UUID id) {
        FraudEntity entity = new FraudEntity();
        entity.setId(id);
        entity.setTransferId(UUID.randomUUID());
        entity.setFromAccountId(UUID.randomUUID());
        entity.setToAccountId(UUID.randomUUID());
        entity.setAlertType("TEST");
        entity.setReason("Test fraud alert");
        entity.setRiskScore(50);
        entity.setStatus(FraudEntity.FraudStatus.PENDING);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    /**
     * Helper method to create a test fraud entity with specific accounts.
     */
    private FraudEntity createTestFraudEntityWithAccount(UUID id, UUID fromAccountId, UUID toAccountId) {
        FraudEntity entity = createTestFraudEntity(id);
        entity.setFromAccountId(fromAccountId);
        entity.setToAccountId(toAccountId);
        return entity;
    }
}
