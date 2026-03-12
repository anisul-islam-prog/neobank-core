package com.neobank.notifications;

import com.neobank.transfers.TransferCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handles notification-related side effects for transfer events.
 * Uses @TransactionalEventListener with AFTER_COMMIT to ensure events
 * are only processed after the originating transaction successfully commits.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTransferCompleted(TransferCompletedEvent event) {
        log.info("Sending notification for transfer {}: ${} moved from {} to {}",
                event.transferId(),
                event.amount(),
                event.fromId(),
                event.toId());
    }
}
