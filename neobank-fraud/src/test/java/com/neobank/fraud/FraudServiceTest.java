package com.neobank.fraud;

import com.neobank.core.approvals.ApprovalService;
import com.neobank.core.transfers.MoneyTransferredEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudService using JUnit 5 and Mockito.
 * Tests fraud detection, velocity checks, blacklist validation, and Maker-Checker integration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FraudService Unit Tests")
class FraudServiceTest {

    @Mock
    private FraudRepository fraudRepository;

    @Mock
    private BlacklistRepository blacklistRepository;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter fraudDetectedTotal;

    @Mock
    private Counter velocityCheckViolations;

    @Mock
    private Counter blacklistHits;

    @Mock
    private Counter suspiciousPatternsDetected;

    private FraudService fraudService;

    @BeforeEach
    void setUp() {
        // Setup mock counters
        given(meterRegistry.counter("bank.fraud.detected.total")).willReturn(fraudDetectedTotal);
        given(meterRegistry.counter("bank.fraud.velocity.violations")).willReturn(velocityCheckViolations);
        given(meterRegistry.counter("bank.fraud.blacklist.hits")).willReturn(blacklistHits);
        given(meterRegistry.counter("bank.fraud.suspicious.patterns")).willReturn(suspiciousPatternsDetected);

        fraudService = new FraudService(
                fraudRepository,
                blacklistRepository,
                approvalService,
                eventPublisher,
                meterRegistry
        );
    }

    @Nested
    @DisplayName("Fraud Analysis")
    class FraudAnalysisTests {

        @Test
        @DisplayName("Should detect fraud when from account is blacklisted")
        void shouldDetectFraudWhenFromAccountIsBlacklisted() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(fromAccountId.toString())).willReturn(true);
            given(fraudRepository.save(any(FraudEntity.class))).willReturn(new FraudEntity());

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.alertType()).isEqualTo("BLACKLIST");
            assertThat(result.riskScore()).isEqualTo(100);
            assertThat(result.reason()).contains("is blacklisted");

            verify(blacklistHits).increment();
            verify(fraudDetectedTotal).increment();
            verify(eventPublisher).publishEvent(any(FraudAlertEvent.class));
        }

        @Test
        @DisplayName("Should detect fraud when to account is blacklisted")
        void shouldDetectFraudWhenToAccountIsBlacklisted() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(fromAccountId.toString())).willReturn(false);
            given(blacklistRepository.isAccountIdBlacklisted(toAccountId.toString())).willReturn(true);
            given(fraudRepository.save(any(FraudEntity.class))).willReturn(new FraudEntity());

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.alertType()).isEqualTo("BLACKLIST");
            assertThat(result.riskScore()).isEqualTo(100);
            assertThat(result.reason()).contains("is blacklisted");

            verify(blacklistHits).increment();
            verify(fraudDetectedTotal).increment();
        }

        @Test
        @DisplayName("Should detect velocity violation when more than 3 transfers in 1 minute")
        void shouldDetectVelocityViolationWhenMoreThan3TransfersIn1Minute() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);
            given(fraudRepository.save(any(FraudEntity.class))).willReturn(new FraudEntity());

            // When - Simulate 4 rapid transfers
            FraudResult result1 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));
            FraudResult result2 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));
            FraudResult result3 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));
            FraudResult result4 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));

            // Then
            assertThat(result1.isFraudDetected()).isFalse();
            assertThat(result2.isFraudDetected()).isFalse();
            assertThat(result3.isFraudDetected()).isFalse();
            assertThat(result4.isFraudDetected()).isTrue();
            assertThat(result4.alertType()).isEqualTo("VELOCITY_CHECK");
            assertThat(result4.riskScore()).isEqualTo(85);

            verify(velocityCheckViolations, atLeastOnce()).increment();
            verify(fraudDetectedTotal, atLeastOnce()).increment();
        }

        @Test
        @DisplayName("Should not flag when 3 or fewer transfers in 1 minute")
        void shouldNotFlagWhen3OrFewerTransfersIn1Minute() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);

            // When - 3 transfers within threshold
            FraudResult result1 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));
            FraudResult result2 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));
            FraudResult result3 = fraudService.analyzeTransfer(createTransferEvent(UUID.randomUUID(), fromAccountId, toAccountId, amount));

            // Then
            assertThat(result1.isFraudDetected()).isFalse();
            assertThat(result2.isFraudDetected()).isFalse();
            assertThat(result3.isFraudDetected()).isFalse();
        }

        @Test
        @DisplayName("Should detect suspicious pattern for round amount transfers")
        void shouldDetectSuspiciousPatternForRoundAmountTransfers() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.00"); // Round amount
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then - Round amounts add risk but may not trigger alert alone
            assertThat(result.riskScore()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should not flag normal transactions")
        void shouldNotFlagNormalTransactions() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("123.45"); // Non-round amount
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result.isFraudDetected()).isFalse();
        }
    }

    @Nested
    @DisplayName("Maker-Checker Integration")
    class MakerCheckerIntegrationTests {

        @Test
        @DisplayName("Should flag for manual review when risk score exceeds threshold")
        void shouldFlagForManualReviewWhenRiskScoreExceedsThreshold() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(fromAccountId.toString())).willReturn(true);
            given(fraudRepository.save(any(FraudEntity.class))).willReturn(new FraudEntity());
            given(approvalService.createTransferAuthorization(any(), any(), any(), any(), any(), any()))
                    .willReturn(new com.neobank.core.approvals.PendingAuthorization());

            // When
            fraudService.analyzeTransfer(event);

            // Then
            verify(approvalService).createTransferAuthorization(
                    any(UUID.class),
                    eq("FRAUD_SYSTEM"),
                    any(UUID.class),
                    any(BigDecimal.class),
                    anyString(),
                    argThat(reason -> reason.contains("FRAUD ALERT"))
            );
        }

        @Test
        @DisplayName("Should include fraud alert ID in manual review reason")
        void shouldIncludeFraudAlertIdInManualReviewReason() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(fromAccountId.toString())).willReturn(true);
            given(fraudRepository.save(any(FraudEntity.class))).willReturn(new FraudEntity());

            // When
            fraudService.analyzeTransfer(event);

            // Then
            ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
            verify(approvalService).createTransferAuthorization(
                    any(UUID.class),
                    anyString(),
                    any(UUID.class),
                    any(BigDecimal.class),
                    anyString(),
                    reasonCaptor.capture()
            );

            assertThat(reasonCaptor.getValue()).contains("Alert ID:");
        }

        @Test
        @DisplayName("Should handle approval service exception gracefully")
        void shouldHandleApprovalServiceExceptionGracefully() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, amount);

            given(blacklistRepository.isAccountIdBlacklisted(fromAccountId.toString())).willReturn(true);
            given(fraudRepository.save(any(FraudEntity.class))).willReturn(new FraudEntity());
            doThrow(new RuntimeException("Approval service unavailable"))
                    .when(approvalService).createTransferAuthorization(any(), any(), any(), any(), any(), any());

            // When - Should not throw exception
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
        }
    }

    @Nested
    @DisplayName("Fraud Alert Management")
    class FraudAlertManagementTests {

        @Test
        @DisplayName("Should get fraud alert by transfer ID")
        void shouldGetFraudAlertByTransferId() {
            // Given
            UUID transferId = UUID.randomUUID();
            FraudEntity fraudEntity = new FraudEntity();
            fraudEntity.setId(UUID.randomUUID());
            fraudEntity.setTransferId(transferId);

            given(fraudRepository.findByTransferId(transferId)).willReturn(Optional.of(fraudEntity));

            // When
            Optional<FraudEntity> result = fraudService.getFraudAlertByTransferId(transferId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTransferId()).isEqualTo(transferId);
        }

        @Test
        @DisplayName("Should return empty when fraud alert not found by transfer ID")
        void shouldReturnEmptyWhenFraudAlertNotFoundByTransferId() {
            // Given
            UUID transferId = UUID.randomUUID();
            given(fraudRepository.findByTransferId(transferId)).willReturn(Optional.empty());

            // When
            Optional<FraudEntity> result = fraudService.getFraudAlertByTransferId(transferId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get all fraud alerts for account")
        void shouldGetAllFraudAlertsForAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            FraudEntity alert1 = new FraudEntity();
            alert1.setId(UUID.randomUUID());
            FraudEntity alert2 = new FraudEntity();
            alert2.setId(UUID.randomUUID());
            given(fraudRepository.findByFromAccountIdOrderByCreatedAtDesc(accountId)).willReturn(java.util.List.of(alert1, alert2));

            // When
            java.util.List<FraudEntity> result = fraudService.getFraudAlertsForAccount(accountId);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get all pending fraud alerts")
        void shouldGetAllPendingFraudAlerts() {
            // Given
            FraudEntity alert1 = new FraudEntity();
            alert1.setId(UUID.randomUUID());
            FraudEntity alert2 = new FraudEntity();
            alert2.setId(UUID.randomUUID());
            given(fraudRepository.findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus.PENDING))
                    .willReturn(java.util.List.of(alert1, alert2));

            // When
            java.util.List<FraudEntity> result = fraudService.getPendingFraudAlerts();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should review and update fraud alert status")
        void shouldReviewAndUpdateFraudAlertStatus() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            FraudEntity fraudEntity = new FraudEntity();
            fraudEntity.setId(alertId);
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);

            given(fraudRepository.findById(alertId)).willReturn(Optional.of(fraudEntity));
            given(fraudRepository.save(fraudEntity)).willReturn(fraudEntity);

            // When
            FraudEntity result = fraudService.reviewFraudAlert(
                    alertId,
                    reviewerId,
                    FraudEntity.FraudStatus.CONFIRMED_FRAUD,
                    "Confirmed as fraudulent activity"
            );

            // Then
            assertThat(result.getStatus()).isEqualTo(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
            assertThat(result.getReviewedBy()).isEqualTo(reviewerId);
        }

        @Test
        @DisplayName("Should throw exception when reviewing non-pending alert")
        void shouldThrowExceptionWhenReviewingNonPendingAlert() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            FraudEntity fraudEntity = new FraudEntity();
            fraudEntity.setId(alertId);
            fraudEntity.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);

            given(fraudRepository.findById(alertId)).willReturn(Optional.of(fraudEntity));

            // When/Then
            assertThatThrownBy(() -> fraudService.reviewFraudAlert(
                    alertId,
                    reviewerId,
                    FraudEntity.FraudStatus.FALSE_POSITIVE,
                    "Test notes"
            )).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending investigation");
        }

        @Test
        @DisplayName("Should throw exception when alert not found")
        void shouldThrowExceptionWhenAlertNotFound() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            given(fraudRepository.findById(alertId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> fraudService.reviewFraudAlert(
                    alertId,
                    reviewerId,
                    FraudEntity.FraudStatus.FALSE_POSITIVE,
                    "Test notes"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fraud alert not found");
        }

        @Test
        @DisplayName("Should confirm fraud alert")
        void shouldConfirmFraudAlert() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            given(fraudRepository.confirmFraud(alertId, reviewerId)).willReturn(1);

            // When
            fraudService.confirmFraud(alertId, reviewerId);

            // Then
            verify(fraudRepository).confirmFraud(alertId, reviewerId);
        }

        @Test
        @DisplayName("Should mark fraud alert as false positive")
        void shouldMarkFraudAlertAsFalsePositive() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            given(fraudRepository.markAsFalsePositive(alertId, reviewerId)).willReturn(1);

            // When
            fraudService.markAsFalsePositive(alertId, reviewerId);

            // Then
            verify(fraudRepository).markAsFalsePositive(alertId, reviewerId);
        }
    }

    @Nested
    @DisplayName("Blacklist Management")
    class BlacklistManagementTests {

        @Test
        @DisplayName("Should add IP address to blacklist")
        void shouldAddIpAddressToBlacklist() {
            // Given
            String ipAddress = "192.168.1.100";
            UUID addedBy = UUID.randomUUID();

            given(blacklistRepository.save(any(BlacklistEntity.class))).willReturn(new BlacklistEntity());

            // When
            BlacklistEntity result = fraudService.addToBlacklist(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    ipAddress,
                    "Suspicious activity detected",
                    BlacklistEntity.BlacklistSeverity.HIGH,
                    addedBy
            );

            // Then
            assertThat(result.getEntityType()).isEqualTo(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            assertThat(result.getEntityValue()).isEqualTo(ipAddress);
            assertThat(result.getSeverity()).isEqualTo(BlacklistEntity.BlacklistSeverity.HIGH);
            assertThat(result.isActive()).isTrue();

            verify(blacklistRepository).save(any(BlacklistEntity.class));
        }

        @Test
        @DisplayName("Should add account ID to blacklist")
        void shouldAddAccountIdToBlacklist() {
            // Given
            String accountId = UUID.randomUUID().toString();
            UUID addedBy = UUID.randomUUID();

            given(blacklistRepository.save(any(BlacklistEntity.class))).willReturn(new BlacklistEntity());

            // When
            BlacklistEntity result = fraudService.addToBlacklist(
                    BlacklistEntity.BlacklistEntityType.ACCOUNT_ID,
                    accountId,
                    "Confirmed fraud",
                    BlacklistEntity.BlacklistSeverity.CRITICAL,
                    addedBy
            );

            // Then
            assertThat(result.getEntityType()).isEqualTo(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            assertThat(result.getSeverity()).isEqualTo(BlacklistEntity.BlacklistSeverity.CRITICAL);
        }

        @Test
        @DisplayName("Should remove entity from blacklist")
        void shouldRemoveEntityFromBlacklist() {
            // Given
            UUID blacklistId = UUID.randomUUID();
            BlacklistEntity entity = new BlacklistEntity();
            entity.setId(blacklistId);
            entity.setActive(true);

            given(blacklistRepository.findById(blacklistId)).willReturn(Optional.of(entity));
            given(blacklistRepository.save(entity)).willReturn(entity);

            // When
            fraudService.removeFromBlacklist(blacklistId);

            // Then
            assertThat(entity.isActive()).isFalse();
            verify(blacklistRepository).save(entity);
        }

        @Test
        @DisplayName("Should check if IP address is blacklisted")
        void shouldCheckIfIpAddressIsBlacklisted() {
            // Given
            String ipAddress = "192.168.1.100";
            given(blacklistRepository.isIpAddressBlacklisted(ipAddress)).willReturn(true);

            // When
            boolean isBlacklisted = fraudService.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @DisplayName("Should check if account ID is blacklisted")
        void shouldCheckIfAccountIdIsBlacklisted() {
            // Given
            UUID accountId = UUID.randomUUID();
            given(blacklistRepository.isAccountIdBlacklisted(accountId.toString())).willReturn(true);

            // When
            boolean isBlacklisted = fraudService.isAccountIdBlacklisted(accountId);

            // Then
            assertThat(isBlacklisted).isTrue();
        }
    }

    @Nested
    @DisplayName("Fraud Metrics")
    class FraudMetricsTests {

        @Test
        @DisplayName("Should get fraud detection metrics")
        void shouldGetFraudDetectionMetrics() {
            // Given
            given(fraudDetectedTotal.count()).willReturn(100.0);
            given(velocityCheckViolations.count()).willReturn(25.0);
            given(blacklistHits.count()).willReturn(10.0);
            given(suspiciousPatternsDetected.count()).willReturn(15.0);
            given(fraudRepository.countByStatus(FraudEntity.FraudStatus.PENDING)).willReturn(5L);
            given(fraudRepository.countByStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD)).willReturn(20L);
            given(fraudRepository.countByStatus(FraudEntity.FraudStatus.FALSE_POSITIVE)).willReturn(30L);
            given(blacklistRepository.countByActiveTrue()).willReturn(50L);

            // When
            FraudService.FraudMetrics metrics = fraudService.getFraudMetrics();

            // Then
            assertThat(metrics.totalFraudDetected()).isEqualTo(100.0);
            assertThat(metrics.velocityViolations()).isEqualTo(25.0);
            assertThat(metrics.blacklistHits()).isEqualTo(10.0);
            assertThat(metrics.suspiciousPatterns()).isEqualTo(15.0);
            assertThat(metrics.pendingAlerts()).isEqualTo(5L);
            assertThat(metrics.confirmedFraud()).isEqualTo(20L);
            assertThat(metrics.falsePositives()).isEqualTo(30L);
            assertThat(metrics.activeBlacklistEntries()).isEqualTo(50L);
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
        @DisplayName("Should handle zero amount transfer")
        void shouldHandleZeroAmountTransfer() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, BigDecimal.ZERO);

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle very large amount transfer")
        void shouldHandleVeryLargeAmountTransfer() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            MoneyTransferredEvent event = createTransferEvent(transferId, fromAccountId, toAccountId, new BigDecimal("10000000.00"));

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle same account transfer")
        void shouldHandleSameAccountTransfer() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            MoneyTransferredEvent event = createTransferEvent(transferId, accountId, accountId, new BigDecimal("100.00"));

            given(blacklistRepository.isAccountIdBlacklisted(anyString())).willReturn(false);

            // When
            FraudResult result = fraudService.analyzeTransfer(event);

            // Then
            assertThat(result).isNotNull();
        }
    }

    /**
     * Helper method to create a transfer event.
     */
    private MoneyTransferredEvent createTransferEvent(UUID transferId, UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        return new MoneyTransferredEvent(
                transferId,
                fromAccountId,
                toAccountId,
                amount,
                "USD",
                Instant.now().toString()
        );
    }
}
