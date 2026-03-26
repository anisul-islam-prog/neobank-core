package com.neobank.fraud;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for fraud blacklist persistence.
 * Stores blocked IPs, account IDs, and other blocked entities.
 * Uses schema_fraud for data isolation.
 */
@Entity
@Table(name = "fraud_blacklist", schema = "schema_fraud")
public class BlacklistEntity {

    @Id
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BlacklistEntityType entityType;

    @Column(name = "entity_value", nullable = false, length = 255)
    private String entityValue;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BlacklistSeverity severity;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadataJson;

    public BlacklistEntity() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BlacklistEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(BlacklistEntityType entityType) {
        this.entityType = entityType;
    }

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BlacklistSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(BlacklistSeverity severity) {
        this.severity = severity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    /**
     * Blacklist entity type enumeration.
     */
    public enum BlacklistEntityType {
        /**
         * Blocked IP address.
         */
        IP_ADDRESS,

        /**
         * Blocked account ID.
         */
        ACCOUNT_ID,

        /**
         * Blocked user ID.
         */
        USER_ID,

        /**
         * Blocked device fingerprint.
         */
        DEVICE_FINGERPRINT,

        /**
         * Blocked email address.
         */
        EMAIL
    }

    /**
     * Blacklist severity enumeration.
     */
    public enum BlacklistSeverity {
        /**
         * Low severity - monitor only.
         */
        LOW,

        /**
         * Medium severity - flag for review.
         */
        MEDIUM,

        /**
         * High severity - block immediately.
         */
        HIGH,

        /**
         * Critical severity - permanent block.
         */
        CRITICAL
    }
}
