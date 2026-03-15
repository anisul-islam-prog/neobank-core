package com.neobank.auth.internal.docs;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for documentation access tokens.
 * Tokens grant temporary access to Swagger UI and API documentation.
 */
@Entity
@Table(name = "doc_access_tokens", uniqueConstraints = {
        @UniqueConstraint(columnNames = "token", name = "uk_doc_tokens_token")
})
public class DocTokenEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_by_username", length = 100)
    private String createdByUsername;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "use_count", nullable = false)
    private int useCount = 0;

    public DocTokenEntity() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    /**
     * Check if token is valid (active and not expired).
     */
    public boolean isValid() {
        return active && Instant.now().isBefore(expiresAt);
    }

    /**
     * Increment use count and update last used timestamp.
     */
    public void recordUsage() {
        this.useCount++;
        this.lastUsedAt = Instant.now();
    }
}
