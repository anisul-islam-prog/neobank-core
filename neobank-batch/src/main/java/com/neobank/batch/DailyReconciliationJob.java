package com.neobank.batch;

import com.neobank.core.accounts.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Daily reconciliation job.
 * Compares the sum of all transactions against total account balances.
 * Triggers ReconciliationAlert if mismatch detected.
 */
@Service
@Transactional
public class DailyReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(DailyReconciliationJob.class);

    private final AccountRepository accountRepository;
    private final ReconciliationAlertRepository alertRepository;

    public DailyReconciliationJob(AccountRepository accountRepository, 
                                  ReconciliationAlertRepository alertRepository) {
        this.accountRepository = accountRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Run daily at 2:00 AM (configurable via EOD_CRON property).
     * Compares total account balances against transaction sums.
     */
    @Scheduled(cron = "${EOD_CRON:0 0 2 * * *}")
    public void runDailyReconciliation() {
        log.info("Starting daily reconciliation job...");

        try {
            // Calculate total balance across all accounts
            BigDecimal totalAccountBalance = calculateTotalAccountBalance();

            // Calculate net transaction flow for the day
            BigDecimal netTransactionFlow = calculateNetTransactionFlow();

            // Compare balances
            BigDecimal difference = totalAccountBalance.subtract(netTransactionFlow).abs();

            log.info("Total Account Balance: {}", totalAccountBalance);
            log.info("Net Transaction Flow: {}", netTransactionFlow);
            log.info("Difference: {}", difference);

            // Check if reconciliation passes (allow small rounding differences)
            if (difference.compareTo(new BigDecimal("0.01")) > 0) {
                log.warn("RECONCILIATION FAILED: Difference of {} detected", difference);
                createReconciliationAlert(totalAccountBalance, netTransactionFlow, difference);
            } else {
                log.info("RECONCILIATION PASSED: All balances match");
            }

        } catch (Exception e) {
            log.error("Reconciliation job failed", e);
            createReconciliationAlert(
                BigDecimal.ZERO, 
                BigDecimal.ZERO, 
                BigDecimal.ZERO, 
                "Reconciliation job failed: " + e.getMessage()
            );
        }
    }

    /**
     * Calculate the sum of all account balances.
     */
    private BigDecimal calculateTotalAccountBalance() {
        return accountRepository.findAll().stream()
            .map(entity -> entity.getBalance())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate net transaction flow (credits - debits).
     * In production, this would query the transfer repository.
     */
    private BigDecimal calculateNetTransactionFlow() {
        // Simplified implementation - in production, query actual transactions
        // This is a placeholder that should integrate with TransferRepository
        return BigDecimal.ZERO;
    }

    /**
     * Create a reconciliation alert for manual investigation.
     */
    private void createReconciliationAlert(BigDecimal expected, BigDecimal actual, BigDecimal difference) {
        createReconciliationAlert(expected, actual, difference, null);
    }

    private void createReconciliationAlert(BigDecimal expected, BigDecimal actual, BigDecimal difference, String details) {
        ReconciliationAlert alert = new ReconciliationAlert();
        alert.setId(UUID.randomUUID());
        alert.setAlertDate(Instant.now());
        alert.setExpectedBalance(expected);
        alert.setActualBalance(actual);
        alert.setDifference(difference);
        alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);
        alert.setCreatedAt(Instant.now());
        alert.setDetails(details != null ? details : 
            String.format("Balance mismatch detected. Expected: %s, Actual: %s, Difference: %s", 
                expected, actual, difference));

        alertRepository.save(alert);
        log.info("Reconciliation alert created: {}", alert.getId());
    }
}
