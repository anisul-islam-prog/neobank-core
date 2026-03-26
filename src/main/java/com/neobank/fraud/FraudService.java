package com.neobank.fraud;

import com.neobank.core.approvals.ApprovalService;
import com.neobank.core.transfers.MoneyTransferredEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fraud detection service implementing velocity checks, blacklist validation,
 * and suspicious pattern detection.
 * 
 * Integrates with Maker-Checker protocol to automatically flag suspicious transactions.
 */
@Service
@Transactional
public class FraudService {

    private static final Logger log = LoggerFactory.getLogger(FraudService.class);

    /**
     * Velocity check threshold: more than 3 transfers in 1 minute triggers alert.
     */
    private static final int VELOCITY_THRESHOLD = 3;

    /**
     * Time window for velocity check in seconds.
     */
    private static final int VELOCITY_WINDOW_SECONDS = 60;

    /**
     * Risk score threshold for automatic manual review.
     */
    private static final int MANUAL_REVIEW_THRESHOLD = 70;

    /**
     * In-memory store for tracking transaction timestamps per account.
     * Key: account ID, Value: list of transaction timestamps
     */
    private final Map<UUID, List<Instant>> accountTransactionTimestamps = new ConcurrentHashMap<>();

    private final FraudRepository fraudRepository;
    private final BlacklistRepository blacklistRepository;
    private final ApprovalService approvalService;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    // Micrometer metrics
    private final Counter fraudDetectedTotal;
    private final Counter velocityCheckViolations;
    private final Counter blacklistHits;
    private final Counter suspiciousPatternsDetected;

    public FraudService(
            FraudRepository fraudRepository,
            BlacklistRepository blacklistRepository,
            ApprovalService approvalService,
            ApplicationEventPublisher eventPublisher,
            MeterRegistry meterRegistry
    ) {
        this.fraudRepository = fraudRepository;
        this.blacklistRepository = blacklistRepository;
        this.approvalService = approvalService;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;

        // Initialize Micrometer metrics
        this.fraudDetectedTotal = meterRegistry.counter("bank.fraud.detected.total");
        this.velocityCheckViolations = meterRegistry.counter("bank.fraud.velocity.violations");
        this.blacklistHits = meterRegistry.counter("bank.fraud.blacklist.hits");
        this.suspiciousPatternsDetected = meterRegistry.counter("bank.fraud.suspicious.patterns");
    }

    /**
     * Analyze a transfer transaction for potential fraud.
     * Performs velocity checks, blacklist validation, and pattern analysis.
     *
     * @param event the transfer completed event
     * @return fraud analysis result
     */
    public FraudResult analyzeTransfer(MoneyTransferredEvent event) {
        log.debug("Analyzing transfer {} for fraud", event.transferId());

        // Check 1: Blacklist validation
        FraudResult blacklistResult = checkBlacklist(event);
        if (blacklistResult.isFraudDetected()) {
            log.warn("Blacklist hit for transfer {}: {}", event.transferId(), blacklistResult.reason());
            blacklistHits.increment();
            fraudDetectedTotal.increment();
            return blacklistResult;
        }

        // Check 2: Velocity check
        FraudResult velocityResult = checkVelocity(event);
        if (velocityResult.isFraudDetected()) {
            log.warn("Velocity violation for transfer {}: {}", event.transferId(), velocityResult.reason());
            velocityCheckViolations.increment();
            fraudDetectedTotal.increment();
            return velocityResult;
        }

        // Check 3: Suspicious pattern detection
        FraudResult patternResult = checkSuspiciousPatterns(event);
        if (patternResult.isFraudDetected()) {
            log.warn("Suspicious pattern for transfer {}: {}", event.transferId(), patternResult.reason());
            suspiciousPatternsDetected.increment();
            fraudDetectedTotal.increment();
            return patternResult;
        }

        // No fraud detected
        log.debug("No fraud detected for transfer {}", event.transferId());
        return FraudResult.noFraud(calculateBaseRiskScore(event));
    }

    /**
     * Check if any entity in the transfer is blacklisted.
     */
    private FraudResult checkBlacklist(MoneyTransferredEvent event) {
        // Check from account
        if (blacklistRepository.isAccountIdBlacklisted(event.senderId().toString())) {
            UUID alertId = createFraudAlert(
                    event,
                    "BLACKLIST",
                    "From account is blacklisted",
                    100
            );
            return FraudResult.blacklistedEntity(alertId, "ACCOUNT_ID", event.senderId().toString());
        }

        // Check to account
        if (blacklistRepository.isAccountIdBlacklisted(event.receiverId().toString())) {
            UUID alertId = createFraudAlert(
                    event,
                    "BLACKLIST",
                    "To account is blacklisted",
                    100
            );
            return FraudResult.blacklistedEntity(alertId, "ACCOUNT_ID", event.receiverId().toString());
        }

        return FraudResult.noFraud(0);
    }

    /**
     * Check transaction velocity for the from account.
     * Flags accounts making more than 3 transfers in 1 minute.
     */
    private FraudResult checkVelocity(MoneyTransferredEvent event) {
        UUID fromAccountId = event.senderId();
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(VELOCITY_WINDOW_SECONDS);

        // Get or create transaction timestamp list for this account
        List<Instant> timestamps = accountTransactionTimestamps.computeIfAbsent(
                fromAccountId,
                k -> new java.util.concurrent.CopyOnWriteArrayList<>()
        );

        // Remove timestamps outside the window
        timestamps.removeIf(ts -> ts.isBefore(windowStart));

        // Add current transaction timestamp
        timestamps.add(now);

        // Check if velocity threshold exceeded
        if (timestamps.size() > VELOCITY_THRESHOLD) {
            UUID alertId = createFraudAlert(
                    event,
                    "VELOCITY_CHECK",
                    "Account exceeded transaction velocity threshold: " + timestamps.size() + 
                    " transactions in " + VELOCITY_WINDOW_SECONDS + " seconds",
                    85
            );
            return FraudResult.velocityViolation(alertId, timestamps.size());
        }

        return FraudResult.noFraud(0);
    }

    /**
     * Check for suspicious patterns in the transaction.
     */
    private FraudResult checkSuspiciousPatterns(MoneyTransferredEvent event) {
        StringBuilder suspiciousReasons = new StringBuilder();
        int riskScore = 0;

        // Pattern 1: Large round amount transfers
        if (isRoundAmount(event.amount())) {
            suspiciousReasons.append("Round amount transfer. ");
            riskScore += 20;
        }

        // Pattern 2: Transfer to newly created account (would need account age data)
        // This is a placeholder for future implementation

        // Pattern 3: Unusual transaction time (would need historical data)
        // This is a placeholder for future implementation

        // Pattern 4: Rapid sequential transfers to different accounts
        if (hasRapidSequentialTransfers(event.senderId())) {
            suspiciousReasons.append("Rapid sequential transfers to different accounts. ");
            riskScore += 30;
        }

        // If risk score exceeds threshold, create alert
        if (riskScore >= MANUAL_REVIEW_THRESHOLD) {
            UUID alertId = createFraudAlert(
                    event,
                    "SUSPICIOUS_PATTERN",
                    suspiciousReasons.toString().trim(),
                    riskScore
            );
            return FraudResult.suspiciousPattern(alertId, riskScore, suspiciousReasons.toString().trim());
        }

        return FraudResult.noFraud(riskScore);
    }

    /**
     * Calculate base risk score for a transaction.
     */
    private int calculateBaseRiskScore(MoneyTransferredEvent event) {
        int riskScore = 0;

        // Higher amounts = higher risk
        if (event.amount().compareTo(new BigDecimal("10000")) > 0) {
            riskScore += 10;
        }

        // Round amounts = slightly higher risk
        if (isRoundAmount(event.amount())) {
            riskScore += 5;
        }

        return riskScore;
    }

    /**
     * Check if amount is a round number (e.g., 1000, 5000, 10000).
     */
    private boolean isRoundAmount(BigDecimal amount) {
        return amount.remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Check if account has rapid sequential transfers to different accounts.
     */
    private boolean hasRapidSequentialTransfers(UUID accountId) {
        // This would need historical transaction data
        // Placeholder implementation
        return false;
    }

    /**
     * Create a fraud alert and persist to database.
     * Automatically sends suspicious transactions to Manager queue via Maker-Checker.
     */
    private UUID createFraudAlert(
            MoneyTransferredEvent event,
            String alertType,
            String reason,
            int riskScore
    ) {
        UUID alertId = UUID.randomUUID();

        // Create and save fraud entity
        FraudEntity fraudEntity = new FraudEntity();
        fraudEntity.setId(alertId);
        fraudEntity.setTransferId(event.transferId());
        fraudEntity.setFromAccountId(event.senderId());
        fraudEntity.setToAccountId(event.receiverId());
        fraudEntity.setAlertType(alertType);
        fraudEntity.setReason(reason);
        fraudEntity.setRiskScore(riskScore);
        fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);
        fraudEntity.setCreatedAt(Instant.now());

        fraudRepository.save(fraudEntity);

        // Publish fraud alert event
        eventPublisher.publishEvent(FraudAlertEvent.of(
                alertId,
                event.transferId(),
                event.senderId(),
                event.receiverId(),
                alertType,
                reason,
                riskScore
        ));

        // Integration with Maker-Checker: Auto-flag suspicious transactions
        if (riskScore >= MANUAL_REVIEW_THRESHOLD) {
            flagForManualReview(event, alertId, reason);
        }

        return alertId;
    }

    /**
     * Flag transaction for manual review via Maker-Checker protocol.
     * Automatically creates pending authorization for Manager approval.
     */
    private void flagForManualReview(
            MoneyTransferredEvent event,
            UUID alertId,
            String reason
    ) {
        log.info("Flagging transfer {} for manual review due to fraud alert {}", 
                event.transferId(), alertId);

        try {
            // Create pending authorization in Maker-Checker system
            // This sends the transaction to the Manager queue regardless of amount
            approvalService.createTransferAuthorization(
                    UUID.randomUUID(), // System-generated initiator
                    "FRAUD_SYSTEM",
                    event.receiverId(),
                    event.amount(),
                    event.currency(),
                    "FRAUD ALERT: " + reason + " [Alert ID: " + alertId + "]"
            );

            log.info("Transfer {} flagged for manual review in Maker-Checker queue", 
                    event.transferId());
        } catch (Exception e) {
            log.error("Failed to flag transfer {} for manual review", event.transferId(), e);
        }
    }

    /**
     * Get fraud alert by transfer ID.
     */
    @Transactional(readOnly = true)
    public Optional<FraudEntity> getFraudAlertByTransferId(UUID transferId) {
        return fraudRepository.findByTransferId(transferId);
    }

    /**
     * Get all fraud alerts for an account.
     */
    @Transactional(readOnly = true)
    public List<FraudEntity> getFraudAlertsForAccount(UUID accountId) {
        return fraudRepository.findByFromAccountIdOrderByCreatedAtDesc(accountId);
    }

    /**
     * Get all pending fraud alerts.
     */
    @Transactional(readOnly = true)
    public List<FraudEntity> getPendingFraudAlerts() {
        return fraudRepository.findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus.PENDING);
    }

    /**
     * Review and update fraud alert status.
     */
    public FraudEntity reviewFraudAlert(
            UUID alertId,
            UUID reviewerId,
            FraudEntity.FraudStatus newStatus,
            String reviewNotes
    ) {
        return fraudRepository.findById(alertId)
                .map(entity -> {
                    if (entity.getStatus() != FraudEntity.FraudStatus.PENDING &&
                        entity.getStatus() != FraudEntity.FraudStatus.INVESTIGATING) {
                        throw new IllegalStateException("Alert is not pending investigation");
                    }

                    entity.setStatus(newStatus);
                    entity.setReviewedAt(Instant.now());
                    entity.setReviewedBy(reviewerId);
                    entity.setReviewNotes(reviewNotes);

                    fraudRepository.save(entity);

                    log.info("Fraud alert {} reviewed by {}: {}", alertId, reviewerId, newStatus);

                    return entity;
                })
                .orElseThrow(() -> new IllegalArgumentException("Fraud alert not found: " + alertId));
    }

    /**
     * Confirm fraud alert as confirmed fraud.
     */
    public FraudEntity confirmFraud(UUID alertId, UUID reviewerId) {
        fraudRepository.confirmFraud(alertId, reviewerId);
        log.info("Fraud alert {} confirmed by {}", alertId, reviewerId);
        return fraudRepository.findById(alertId).orElse(null);
    }

    /**
     * Mark fraud alert as false positive.
     */
    public FraudEntity markAsFalsePositive(UUID alertId, UUID reviewerId) {
        fraudRepository.markAsFalsePositive(alertId, reviewerId);
        log.info("Fraud alert {} marked as false positive by {}", alertId, reviewerId);
        return fraudRepository.findById(alertId).orElse(null);
    }

    /**
     * Add entity to blacklist.
     */
    public BlacklistEntity addToBlacklist(
            BlacklistEntity.BlacklistEntityType entityType,
            String entityValue,
            String reason,
            BlacklistEntity.BlacklistSeverity severity,
            UUID addedBy
    ) {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setId(UUID.randomUUID());
        entity.setEntityType(entityType);
        entity.setEntityValue(entityValue);
        entity.setReason(reason);
        entity.setSeverity(severity);
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(addedBy);

        blacklistRepository.save(entity);

        log.info("Added {} to blacklist: {} (Severity: {})", entityType, entityValue, severity);

        return entity;
    }

    /**
     * Remove entity from blacklist.
     */
    public void removeFromBlacklist(UUID blacklistId) {
        blacklistRepository.findById(blacklistId)
                .ifPresent(entity -> {
                    entity.setActive(false);
                    blacklistRepository.save(entity);
                    log.info("Removed blacklist entry: {}", blacklistId);
                });
    }

    /**
     * Check if IP address is blacklisted.
     */
    @Transactional(readOnly = true)
    public boolean isIpAddressBlacklisted(String ipAddress) {
        return blacklistRepository.isIpAddressBlacklisted(ipAddress);
    }

    /**
     * Check if account ID is blacklisted.
     */
    @Transactional(readOnly = true)
    public boolean isAccountIdBlacklisted(UUID accountId) {
        return blacklistRepository.isAccountIdBlacklisted(accountId.toString());
    }

    /**
     * Get fraud detection metrics.
     */
    @Transactional(readOnly = true)
    public FraudMetrics getFraudMetrics() {
        return new FraudMetrics(
                fraudDetectedTotal.count(),
                velocityCheckViolations.count(),
                blacklistHits.count(),
                suspiciousPatternsDetected.count(),
                fraudRepository.countByStatus(FraudEntity.FraudStatus.PENDING),
                fraudRepository.countByStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD),
                fraudRepository.countByStatus(FraudEntity.FraudStatus.FALSE_POSITIVE),
                blacklistRepository.countByActiveTrue()
        );
    }

    /**
     * Fraud detection metrics record.
     */
    public record FraudMetrics(
            double totalFraudDetected,
            double velocityViolations,
            double blacklistHits,
            double suspiciousPatterns,
            long pendingAlerts,
            long confirmedFraud,
            long falsePositives,
            long activeBlacklistEntries
    ) {}
}
