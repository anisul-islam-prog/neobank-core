package com.neobank.analytics.cqrs;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-optimized flat table for BI transaction history.
 * Denormalized for fast complex queries on business dashboards.
 */
@Entity
@Table(name = "bi_transaction_history", indexes = {
    @Index(name = "idx_bi_from_account", columnList = "fromAccountId"),
    @Index(name = "idx_bi_to_account", columnList = "toAccountId"),
    @Index(name = "idx_bi_occurred_at", columnList = "occurredAt"),
    @Index(name = "idx_bi_currency", columnList = "currency")
})
public class BiTransactionHistory {

    @Id
    private UUID id;

    @Column(name = "transfer_id", nullable = false, unique = true)
    private UUID transferId;

    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;

    @Column(name = "from_owner_name")
    private String fromOwnerName;

    @Column(name = "to_account_id", nullable = false)
    private UUID toAccountId;

    @Column(name = "to_owner_name")
    private String toOwnerName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "from_balance_before", precision = 19, scale = 4)
    private BigDecimal fromBalanceBefore;

    @Column(name = "from_balance_after", precision = 19, scale = 4)
    private BigDecimal fromBalanceAfter;

    @Column(name = "to_balance_before", precision = 19, scale = 4)
    private BigDecimal toBalanceBefore;

    @Column(name = "to_balance_after", precision = 19, scale = 4)
    private BigDecimal toBalanceAfter;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    @Column(length = 500)
    private String metadata;

    public BiTransactionHistory() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getFromOwnerName() {
        return fromOwnerName;
    }

    public void setFromOwnerName(String fromOwnerName) {
        this.fromOwnerName = fromOwnerName;
    }

    public UUID getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(UUID toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getToOwnerName() {
        return toOwnerName;
    }

    public void setToOwnerName(String toOwnerName) {
        this.toOwnerName = toOwnerName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public BigDecimal getFromBalanceBefore() {
        return fromBalanceBefore;
    }

    public void setFromBalanceBefore(BigDecimal fromBalanceBefore) {
        this.fromBalanceBefore = fromBalanceBefore;
    }

    public BigDecimal getFromBalanceAfter() {
        return fromBalanceAfter;
    }

    public void setFromBalanceAfter(BigDecimal fromBalanceAfter) {
        this.fromBalanceAfter = fromBalanceAfter;
    }

    public BigDecimal getToBalanceBefore() {
        return toBalanceBefore;
    }

    public void setToBalanceBefore(BigDecimal toBalanceBefore) {
        this.toBalanceBefore = toBalanceBefore;
    }

    public BigDecimal getToBalanceAfter() {
        return toBalanceAfter;
    }

    public void setToBalanceAfter(BigDecimal toBalanceAfter) {
        this.toBalanceAfter = toBalanceAfter;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
