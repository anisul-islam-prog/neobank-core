package com.neobank.core.approvals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Maker-Checker approval workflow.
 * Handles creation and review of pending authorizations.
 */
@Service
@Transactional
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    /**
     * Threshold for requiring approval on transfers.
     */
    public static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("5000.00");

    private final PendingAuthorizationRepository repository;

    public ApprovalService(PendingAuthorizationRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a pending authorization for high-value transfer.
     */
    public PendingAuthorization createTransferAuthorization(
            UUID initiatorId,
            String initiatorRole,
            UUID targetAccountId,
            BigDecimal amount,
            String currency,
            String reason
    ) {
        PendingAuthorization auth = new PendingAuthorization();
        auth.setId(UUID.randomUUID());
        auth.setActionType(PendingAuthorization.ActionType.HIGH_VALUE_TRANSFER);
        auth.setInitiatorId(initiatorId);
        auth.setInitiatorRole(initiatorRole);
        auth.setTargetId(targetAccountId);
        auth.setAmount(amount);
        auth.setCurrency(currency);
        auth.setReason(reason);
        auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);
        auth.setCreatedAt(Instant.now());

        repository.save(auth);
        log.info("Created pending authorization {} for high-value transfer of {}", auth.getId(), amount);

        return auth;
    }

    /**
     * Check if a transfer requires approval.
     */
    public boolean requiresApproval(BigDecimal amount) {
        return amount.compareTo(HIGH_VALUE_THRESHOLD) > 0;
    }

    /**
     * Get all pending authorizations.
     */
    @Transactional(readOnly = true)
    public List<PendingAuthorization> getPendingAuthorizations() {
        return repository.findByStatusOrderByCreatedAtDesc(PendingAuthorization.AuthorizationStatus.PENDING);
    }

    /**
     * Approve a pending authorization.
     */
    public Optional<PendingAuthorization> approve(
            UUID authorizationId,
            UUID reviewerId,
            String reviewerRole,
            String notes
    ) {
        return repository.findById(authorizationId).map(auth -> {
            if (auth.getStatus() != PendingAuthorization.AuthorizationStatus.PENDING) {
                throw new IllegalStateException("Authorization is not pending: " + auth.getStatus());
            }

            auth.setStatus(PendingAuthorization.AuthorizationStatus.APPROVED);
            auth.setReviewerId(reviewerId);
            auth.setReviewerRole(reviewerRole);
            auth.setReviewedAt(Instant.now());
            auth.setReviewNotes(notes);

            repository.save(auth);
            log.info("Authorization {} approved by {}", authorizationId, reviewerId);

            return auth;
        });
    }

    /**
     * Reject a pending authorization.
     */
    public Optional<PendingAuthorization> reject(
            UUID authorizationId,
            UUID reviewerId,
            String reviewerRole,
            String notes
    ) {
        return repository.findById(authorizationId).map(auth -> {
            if (auth.getStatus() != PendingAuthorization.AuthorizationStatus.PENDING) {
                throw new IllegalStateException("Authorization is not pending: " + auth.getStatus());
            }

            auth.setStatus(PendingAuthorization.AuthorizationStatus.REJECTED);
            auth.setReviewerId(reviewerId);
            auth.setReviewerRole(reviewerRole);
            auth.setReviewedAt(Instant.now());
            auth.setReviewNotes(notes);

            repository.save(auth);
            log.info("Authorization {} rejected by {}", authorizationId, reviewerId);

            return auth;
        });
    }

    /**
     * Get pending authorization count.
     */
    @Transactional(readOnly = true)
    public long getPendingCount() {
        return repository.countByStatus(PendingAuthorization.AuthorizationStatus.PENDING);
    }
}
