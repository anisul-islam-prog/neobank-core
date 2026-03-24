package com.neobank.core.approvals;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity for pending authorizations requiring Maker-Checker approval.
 * Used for high-value transfers and sensitive operations.
 */
@Entity
@Table(name = "pending_authorizations")
public class PendingAuthorization {

    @Id
    private UUID id;

    @Column(name = "action_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "initiator_id", nullable = false)
    private UUID initiatorId;

    @Column(name = "initiator_role", nullable = false, length = 50)
    private String initiatorRole;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuthorizationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "reviewer_role", length = 50)
    private String reviewerRole;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    public PendingAuthorization() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public UUID getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(UUID initiatorId) {
        this.initiatorId = initiatorId;
    }

    public String getInitiatorRole() {
        return initiatorRole;
    }

    public void setInitiatorRole(String initiatorRole) {
        this.initiatorRole = initiatorRole;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AuthorizationStatus getStatus() {
        return status;
    }

    public void setStatus(AuthorizationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(UUID reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerRole() {
        return reviewerRole;
    }

    public void setReviewerRole(String reviewerRole) {
        this.reviewerRole = reviewerRole;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public enum ActionType {
        HIGH_VALUE_TRANSFER,
        ACCOUNT_DELETION,
        LIMIT_INCREASE,
        USER_SUSPENSION
    }

    public enum AuthorizationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        EXPIRED
    }
}
