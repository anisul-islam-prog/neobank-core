package com.neobank.fraud;

import com.neobank.core.approvals.ApprovalService;
import com.neobank.core.approvals.PendingAuthorization;
import com.neobank.core.transfers.MoneyTransferredEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Integration test for FraudService using Testcontainers.
 * Tests fraud detection with real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import({FraudService.class})
@DisplayName("FraudService Integration Tests")
class FraudServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private FraudRepository fraudRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private FraudService fraudService;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        fraudRepository.deleteAll();
        blacklistRepository.deleteAll();
    }

    @Nested
    @DisplayName("Fraud Alert Persistence")
    class FraudAlertPersistenceTests {

        @Test
        @DisplayName("Should persist fraud alert to database")
        void shouldPersistFraudAlertToDatabase() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();

            FraudEntity fraudEntity = new FraudEntity();
            fraudEntity.setId(UUID.randomUUID());
            fraudEntity.setTransferId(transferId);
            fraudEntity.setFromAccountId(fromAccountId);
            fraudEntity.setToAccountId(toAccountId);
            fraudEntity.setAlertType("VELOCITY_CHECK");
            fraudEntity.setReason("Velocity threshold exceeded");
            fraudEntity.setRiskScore(85);
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudEntity.setCreatedAt(Instant.now());

            // When
            FraudEntity saved = fraudRepository.save(fraudEntity);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTransferId()).isEqualTo(transferId);
            assertThat(saved.getAlertType()).isEqualTo("VELOCITY_CHECK");
            assertThat(saved.getRiskScore()).isEqualTo(85);
        }

        @Test
        @DisplayName("Should find fraud alert by transfer ID")
        void shouldFindFraudAlertByTransferId() {
            // Given
            UUID transferId = UUID.randomUUID();
            FraudEntity fraudEntity = createAndSaveFraudAlert(transferId);

            // When
            Optional<FraudEntity> result = fraudRepository.findByTransferId(transferId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTransferId()).isEqualTo(transferId);
        }

        @Test
        @DisplayName("Should find all fraud alerts for account")
        void shouldFindAllFraudAlertsForAccount() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            createAndSaveFraudAlert(UUID.randomUUID(), fromAccountId);
            createAndSaveFraudAlert(UUID.randomUUID(), fromAccountId);
            createAndSaveFraudAlert(UUID.randomUUID(), fromAccountId);

            // When
            List<FraudEntity> results = fraudRepository.findByFromAccountIdOrderByCreatedAtDesc(fromAccountId);

            // Then
            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("Should find all pending fraud alerts")
        void shouldFindAllPendingFraudAlerts() {
            // Given
            FraudEntity pending1 = createAndSaveFraudAlert(UUID.randomUUID());
            pending1.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(pending1);

            FraudEntity pending2 = createAndSaveFraudAlert(UUID.randomUUID());
            pending2.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(pending2);

            FraudEntity confirmed = createAndSaveFraudAlert(UUID.randomUUID());
            confirmed.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed);

            // When
            List<FraudEntity> results = fraudRepository.findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus.PENDING);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(e -> e.getStatus() == FraudEntity.FraudStatus.PENDING);
        }

        @Test
        @DisplayName("Should count fraud alerts by status")
        void shouldCountFraudAlertsByStatus() {
            // Given
            FraudEntity pending = createAndSaveFraudAlert(UUID.randomUUID());
            pending.setStatus(FraudEntity.FraudStatus.PENDING);
            fraudRepository.save(pending);

            FraudEntity confirmed1 = createAndSaveFraudAlert(UUID.randomUUID());
            confirmed1.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            fraudRepository.save(confirmed1);

            FraudEntity confirmed2 = createAndSaveFraudAlert(UUID.randomUUID());
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
        @DisplayName("Should update fraud alert status")
        void shouldUpdateFraudAlertStatus() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            FraudEntity fraudEntity = createAndSaveFraudAlert(alertId);
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
            FraudEntity fraudEntity = createAndSaveFraudAlert(alertId);
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
            FraudEntity fraudEntity = createAndSaveFraudAlert(alertId);
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
    @DisplayName("Blacklist Persistence")
    class BlacklistPersistenceTests {

        @Test
        @DisplayName("Should persist blacklist entry to database")
        void shouldPersistBlacklistEntryToDatabase() {
            // Given
            BlacklistEntity blacklistEntity = new BlacklistEntity();
            blacklistEntity.setId(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            blacklistEntity.setEntityValue(UUID.randomUUID().toString());
            blacklistEntity.setReason("Confirmed fraud");
            blacklistEntity.setSeverity(BlacklistEntity.BlacklistSeverity.CRITICAL);
            blacklistEntity.setActive(true);
            blacklistEntity.setCreatedAt(Instant.now());

            // When
            BlacklistEntity saved = blacklistRepository.save(blacklistEntity);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEntityType()).isEqualTo(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should check if IP address is blacklisted")
        void shouldCheckIfIpAddressIsBlacklisted() {
            // Given
            String ipAddress = "192.168.1.100";
            BlacklistEntity blacklistEntity = createAndSaveBlacklistEntry(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    ipAddress
            );
            blacklistEntity.setActive(true);
            blacklistRepository.save(blacklistEntity);

            // When
            boolean isBlacklisted = blacklistRepository.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-blacklisted IP address")
        void shouldReturnFalseForNonBlacklistedIpAddress() {
            // Given
            String ipAddress = "192.168.1.100";

            // When
            boolean isBlacklisted = blacklistRepository.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isFalse();
        }

        @Test
        @DisplayName("Should check if account ID is blacklisted")
        void shouldCheckIfAccountIdIsBlacklisted() {
            // Given
            String accountId = UUID.randomUUID().toString();
            BlacklistEntity blacklistEntity = createAndSaveBlacklistEntry(
                    BlacklistEntity.BlacklistEntityType.ACCOUNT_ID,
                    accountId
            );
            blacklistEntity.setActive(true);
            blacklistRepository.save(blacklistEntity);

            // When
            boolean isBlacklisted = blacklistRepository.isAccountIdBlacklisted(accountId);

            // Then
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @DisplayName("Should return false for inactive blacklist entry")
        void shouldReturnFalseForInactiveBlacklistEntry() {
            // Given
            String ipAddress = "192.168.1.100";
            BlacklistEntity blacklistEntity = createAndSaveBlacklistEntry(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    ipAddress
            );
            blacklistEntity.setActive(false);
            blacklistRepository.save(blacklistEntity);

            // When
            boolean isBlacklisted = blacklistRepository.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isFalse();
        }

        @Test
        @DisplayName("Should find all active blacklist entries")
        void shouldFindAllActiveBlacklistEntries() {
            // Given
            createAndSaveBlacklistEntry(BlacklistEntity.BlacklistEntityType.IP_ADDRESS, "192.168.1.1");
            createAndSaveBlacklistEntry(BlacklistEntity.BlacklistEntityType.IP_ADDRESS, "192.168.1.2");

            BlacklistEntity inactive = createAndSaveBlacklistEntry(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    "192.168.1.3"
            );
            inactive.setActive(false);
            blacklistRepository.save(inactive);

            // When
            List<BlacklistEntity> results = blacklistRepository.findByActiveTrueOrderByCreatedAtDesc();

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(BlacklistEntity::isActive);
        }

        @Test
        @DisplayName("Should count active blacklist entries")
        void shouldCountActiveBlacklistEntries() {
            // Given
            createAndSaveBlacklistEntry(BlacklistEntity.BlacklistEntityType.IP_ADDRESS, "192.168.1.1");
            createAndSaveBlacklistEntry(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID, UUID.randomUUID().toString());

            BlacklistEntity inactive = createAndSaveBlacklistEntry(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    "192.168.1.2"
            );
            inactive.setActive(false);
            blacklistRepository.save(inactive);

            // When
            long count = blacklistRepository.countByActiveTrue();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find blacklist entry by entity type and value")
        void shouldFindBlacklistEntryByEntityTypeAndValue() {
            // Given
            String accountId = UUID.randomUUID().toString();
            BlacklistEntity blacklistEntity = createAndSaveBlacklistEntry(
                    BlacklistEntity.BlacklistEntityType.ACCOUNT_ID,
                    accountId
            );

            // When
            Optional<BlacklistEntity> result = blacklistRepository.findByEntityTypeAndEntityValue(
                    BlacklistEntity.BlacklistEntityType.ACCOUNT_ID,
                    accountId
            );

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEntityValue()).isEqualTo(accountId);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null transfer ID gracefully")
        void shouldHandleNullTransferIdGracefully() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    null,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("100.00"),
                    "USD",
                    Instant.now().toString()
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle empty fraud alerts list")
        void shouldHandleEmptyFraudAlertsList() {
            // When
            List<FraudEntity> results = fraudRepository.findByFromAccountIdOrderByCreatedAtDesc(UUID.randomUUID());

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty blacklist")
        void shouldHandleEmptyBlacklist() {
            // When
            List<BlacklistEntity> results = blacklistRepository.findByActiveTrueOrderByCreatedAtDesc();

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple fraud alerts for same transfer")
        void shouldHandleMultipleFraudAlertsForSameTransfer() {
            // Given
            UUID transferId = UUID.randomUUID();
            createAndSaveFraudAlert(transferId);
            createAndSaveFraudAlert(transferId);

            // When
            Optional<FraudEntity> result = fraudRepository.findByTransferId(transferId);

            // Then - Returns one of the alerts (DB dependent)
            assertThat(result).isPresent();
        }
    }

    /**
     * Helper method to create and save a fraud alert.
     */
    private FraudEntity createAndSaveFraudAlert(UUID transferId) {
        return createAndSaveFraudAlert(transferId, UUID.randomUUID());
    }

    /**
     * Helper method to create and save a fraud alert with specific from account.
     */
    private FraudEntity createAndSaveFraudAlert(UUID transferId, UUID fromAccountId) {
        FraudEntity fraudEntity = new FraudEntity();
        fraudEntity.setId(UUID.randomUUID());
        fraudEntity.setTransferId(transferId);
        fraudEntity.setFromAccountId(fromAccountId);
        fraudEntity.setToAccountId(UUID.randomUUID());
        fraudEntity.setAlertType("TEST");
        fraudEntity.setReason("Test alert");
        fraudEntity.setRiskScore(50);
        fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);
        fraudEntity.setCreatedAt(Instant.now());
        return fraudRepository.save(fraudEntity);
    }

    /**
     * Helper method to create and save a blacklist entry.
     */
    private BlacklistEntity createAndSaveBlacklistEntry(
            BlacklistEntity.BlacklistEntityType entityType,
            String entityValue
    ) {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setId(UUID.randomUUID());
        entity.setEntityType(entityType);
        entity.setEntityValue(entityValue);
        entity.setReason("Test blacklist entry");
        entity.setSeverity(BlacklistEntity.BlacklistSeverity.HIGH);
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        return blacklistRepository.save(entity);
    }
}
