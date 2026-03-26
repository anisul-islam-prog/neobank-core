package com.neobank.loans.internal;

import com.neobank.loans.ApplicationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for loan persistence.
 */
@Entity
@Table(name = "loans")
class LoanEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principal;

    @Column(name = "outstanding_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal outstandingBalance;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "monthly_payment", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_profile", columnDefinition = "jsonb")
    private String riskProfileJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "amortization_schedule", columnDefinition = "jsonb")
    private String amortizationScheduleJson;

    public LoanEntity() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(Instant disbursedAt) {
        this.disbursedAt = disbursedAt;
    }

    public String getRiskProfileJson() {
        return riskProfileJson;
    }

    public void setRiskProfileJson(String riskProfileJson) {
        this.riskProfileJson = riskProfileJson;
    }

    public String getAmortizationScheduleJson() {
        return amortizationScheduleJson;
    }

    public void setAmortizationScheduleJson(String amortizationScheduleJson) {
        this.amortizationScheduleJson = amortizationScheduleJson;
    }
}
