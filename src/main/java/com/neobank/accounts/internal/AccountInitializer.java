package com.neobank.accounts.internal;

import com.neobank.accounts.AccountService;
import com.neobank.auth.api.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Listens for user creation events and creates default savings accounts.
 */
@Service
class AccountInitializer {

    private static final Logger log = LoggerFactory.getLogger(AccountInitializer.class);

    private final AccountService accountService;

    public AccountInitializer(AccountService accountService) {
        this.accountService = accountService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onUserCreated(UserCreatedEvent event) {
        try {
            // Create default savings account with $0 initial balance
            String accountOwner = "User: " + event.username();
            accountService.createNewAccount(accountOwner, BigDecimal.ZERO);

            log.info("Default savings account created for user: {} ({})",
                    event.username(), event.userId());
        } catch (Exception e) {
            log.error("Failed to create default account for user: {}", event.username(), e);
        }
    }
}
