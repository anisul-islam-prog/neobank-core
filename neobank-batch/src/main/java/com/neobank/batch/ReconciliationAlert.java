package com.neobank.batch;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for reconciliation alerts.
 * Created when daily reconciliation detects mismatches.
 */
@Entity
@Table(name = "reconciliation_alerts")
public class ReconciliationAlert {

    @Id
    private UUID id;

    @Column(name = "alert_date", nullable = false)
    private Instant alertDate;

    @Column(name = "expected_balance", nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal expectedBalance;

    @Column(name = "actual_balance", nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal actualBalance;

    @Column(name = "difference", nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal difference;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    public ReconciliationAlert() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(Instant alertDate) {
        this.alertDate = alertDate;
    }

    public java.math.BigDecimal getExpectedBalance() {
        return expectedBalance;
    }

    public void setExpectedBalance(java.math.BigDecimal expectedBalance) {
        this.expectedBalance = expectedBalance;
    }

    public java.math.BigDecimal getActualBalance() {
        return actualBalance;
    }

    public void setActualBalance(java.math.BigDecimal actualBalance) {
        this.actualBalance = actualBalance;
    }

    public java.math.BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(java.math.BigDecimal difference) {
        this.difference = difference;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public enum AlertStatus {
        PENDING,
        INVESTIGATING,
        RESOLVED,
        FALSE_POSITIVE
    }
}
