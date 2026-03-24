package com.neobank.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Custom business metrics service using Micrometer.
 * Exposes metrics for Prometheus scraping via /actuator/prometheus.
 */
@Service
public class BankMetricsService {

    private static final Logger log = LoggerFactory.getLogger(BankMetricsService.class);

    // Metric names
    private static final String TRANSACTIONS_TOTAL = "bank.transactions.total";
    private static final String ACCOUNTS_CREATED = "bank.accounts.created";
    private static final String TRANSFERS_FAILED = "bank.transfers.failed";
    private static final String VAULT_TOTAL_LIQUIDITY = "bank.vault.total_liquidity";

    // Meters
    private final Counter transactionsCounter;
    private final Counter accountsCreatedCounter;
    private final Counter transfersFailedCounter;
    private final AtomicReference<BigDecimal> totalLiquidity;

    private final Timer transferTimer;
    private final Timer accountCreationTimer;
    private final MeterRegistry meterRegistry;

    public BankMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Counter for total transactions
        this.transactionsCounter = Counter.builder(TRANSACTIONS_TOTAL)
            .description("Total number of transactions processed")
            .tag("application", "neobank")
            .register(meterRegistry);

        // Counter for accounts created
        this.accountsCreatedCounter = Counter.builder(ACCOUNTS_CREATED)
            .description("Total number of accounts created")
            .tag("application", "neobank")
            .register(meterRegistry);

        // Counter for failed transfers
        this.transfersFailedCounter = Counter.builder(TRANSFERS_FAILED)
            .description("Total number of failed transfers")
            .tag("application", "neobank")
            .register(meterRegistry);

        // Gauge for total liquidity (sum of all account balances)
        this.totalLiquidity = new AtomicReference<>(BigDecimal.ZERO);
        Gauge.builder(VAULT_TOTAL_LIQUIDITY, () -> totalLiquidity.get().doubleValue())
            .description("Total liquidity across all accounts (sum of balances)")
            .tag("application", "neobank")
            .tag("currency", "USD")
            .register(meterRegistry);

        // Timer for transfer operations
        this.transferTimer = Timer.builder("bank.transfer.duration")
            .description("Transfer operation duration")
            .tag("application", "neobank")
            .register(meterRegistry);

        // Timer for account creation
        this.accountCreationTimer = Timer.builder("bank.account.creation.duration")
            .description("Account creation operation duration")
            .tag("application", "neobank")
            .register(meterRegistry);

        log.info("BankMetricsService initialized with metrics: {}, {}, {}, {}",
            TRANSACTIONS_TOTAL, ACCOUNTS_CREATED, TRANSFERS_FAILED, VAULT_TOTAL_LIQUIDITY);
    }

    /**
     * Record a successful transaction.
     */
    public void recordTransaction() {
        transactionsCounter.increment();
        log.debug("Transaction recorded. Total: {}", (long) transactionsCounter.count());
    }

    /**
     * Record a successful transaction with amount.
     */
    public void recordTransaction(double amount) {
        transactionsCounter.increment();
        log.debug("Transaction recorded. Amount: ${}, Total count: {}", 
            amount, (long) transactionsCounter.count());
    }

    /**
     * Record a new account creation.
     */
    public void recordAccountCreated() {
        accountsCreatedCounter.increment();
        log.debug("Account created. Total accounts: {}", (long) accountsCreatedCounter.count());
    }

    /**
     * Record a failed transfer.
     */
    public void recordTransferFailed() {
        transfersFailedCounter.increment();
        log.warn("Transfer failed recorded. Total failures: {}", 
            (long) transfersFailedCounter.count());
    }

    /**
     * Record a failed transfer with reason.
     */
    public void recordTransferFailed(String reason) {
        transfersFailedCounter.increment();
        log.warn("Transfer failed. Reason: {}. Total failures: {}", 
            reason, (long) transfersFailedCounter.count());
    }

    /**
     * Update the total liquidity (sum of all account balances).
     * Call this periodically or after balance changes.
     */
    public void updateTotalLiquidity(BigDecimal newTotal) {
        BigDecimal previous = totalLiquidity.getAndSet(newTotal);
        log.debug("Total liquidity updated: {} -> {}", previous, newTotal);
    }

    /**
     * Add to total liquidity (for deposits).
     */
    public void addToLiquidity(BigDecimal amount) {
        totalLiquidity.updateAndGet(current -> current.add(amount));
        log.debug("Added {} to liquidity. New total: {}", amount, totalLiquidity.get());
    }

    /**
     * Subtract from total liquidity (for withdrawals).
     */
    public void subtractFromLiquidity(BigDecimal amount) {
        totalLiquidity.updateAndGet(current -> current.subtract(amount));
        log.debug("Subtracted {} from liquidity. New total: {}", amount, totalLiquidity.get());
    }

    /**
     * Get current total liquidity.
     */
    public BigDecimal getTotalLiquidity() {
        return totalLiquidity.get();
    }

    /**
     * Record transfer duration.
     */
    public long startTransferTimer() {
        return System.nanoTime();
    }

    /**
     * Stop and record transfer duration.
     */
    public void stopTransferTimer(long startTimeNanos) {
        long durationNanos = System.nanoTime() - startTimeNanos;
        transferTimer.record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * Record account creation duration.
     */
    public long startAccountCreationTimer() {
        return System.nanoTime();
    }

    /**
     * Stop and record account creation duration.
     */
    public void stopAccountCreationTimer(long startTimeNanos) {
        long durationNanos = System.nanoTime() - startTimeNanos;
        accountCreationTimer.record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    // Getters for monitoring
    public long getTotalTransactions() {
        return (long) transactionsCounter.count();
    }

    public long getTotalAccountsCreated() {
        return (long) accountsCreatedCounter.count();
    }

    public long getTotalTransfersFailed() {
        return (long) transfersFailedCounter.count();
    }
}
