package com.neobank.analytics.cqrs;

import com.neobank.core.transfers.MoneyTransferredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * CQRS Event Handler - listens to MoneyTransferredEvent from Core Banking.
 * Populates read-optimized BI tables for analytics dashboards.
 */
@Service
@Transactional
public class TransferEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TransferEventHandler.class);

    private final BiTransactionHistoryRepository biRepository;

    public TransferEventHandler(BiTransactionHistoryRepository biRepository) {
        this.biRepository = biRepository;
    }

    /**
     * Listen to MoneyTransferredEvent and populate BI table.
     * This runs asynchronously after the transaction commits.
     */
    @ApplicationModuleListener
    public void onMoneyTransferred(MoneyTransferredEvent event) {
        log.info("Processing MoneyTransferredEvent for analytics: {}", event.transferId());

        BiTransactionHistory bi = new BiTransactionHistory();
        bi.setId(UUID.randomUUID());
        bi.setTransferId(event.transferId());
        bi.setFromAccountId(event.senderId());
        bi.setToAccountId(event.receiverId());
        bi.setAmount(event.amount());
        bi.setCurrency(event.currency() != null ? event.currency() : "USD");
        bi.setStatus("COMPLETED");
        bi.setOccurredAt(Instant.parse(event.occurredAt()));
        bi.setProcessedAt(Instant.now());
        bi.setTransactionType("TRANSFER");
        bi.setChannel("CORE_BANKING");

        // Note: Balance snapshots would require fetching from AccountApi
        // For now, we store the transfer data. In production, you'd enrich with balance data.

        biRepository.save(bi);
        log.info("BI transaction history saved: {}", event.transferId());
    }
}
