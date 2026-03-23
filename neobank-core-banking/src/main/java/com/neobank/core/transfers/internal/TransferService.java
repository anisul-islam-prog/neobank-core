package com.neobank.core.transfers.internal;

import com.neobank.core.accounts.api.AccountApi;
import com.neobank.core.transfers.MoneyTransferredEvent;
import com.neobank.core.transfers.TransactionResult;
import com.neobank.core.transfers.TransferRequest;
import com.neobank.core.transfers.api.TransferApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for executing fund transfers between accounts.
 */
@Service
@Transactional
class TransferService implements TransferApi {

    private final AccountApi accountApi;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final ApplicationEventPublisher eventPublisher;

    public TransferService(AccountApi accountApi, TransferRepository transferRepository, 
                          TransferMapper transferMapper, ApplicationEventPublisher eventPublisher) {
        this.accountApi = accountApi;
        this.transferRepository = transferRepository;
        this.transferMapper = transferMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TransactionResult transfer(TransferRequest request) {
        // Fetch both accounts with pessimistic lock to prevent race conditions
        var fromAccount = accountApi.getAccountWithLock(request.fromId());
        var toAccount = accountApi.getAccountWithLock(request.toId());

        // Check if from account has enough balance
        TransactionResult result = switch (fromAccount.balance().compareTo(request.amount())) {
            case -1 -> new TransactionResult.Failure("Insufficient balance");
            default -> {
                // Update both balances
                var updatedFromAccount = new com.neobank.core.accounts.Account(
                    fromAccount.id(),
                    fromAccount.ownerName(),
                    fromAccount.balance().subtract(request.amount())
                );
                var updatedToAccount = new com.neobank.core.accounts.Account(
                    toAccount.id(),
                    toAccount.ownerName(),
                    toAccount.balance().add(request.amount())
                );

                // Save both accounts back
                accountApi.updateAccount(updatedFromAccount);
                accountApi.updateAccount(updatedToAccount);

                // Record the transfer
                var transferEntity = transferMapper.toEntity(request);
                transferMapper.toCompletedEntity(transferEntity);
                TransferEntity saved = transferRepository.save(transferEntity);

                // Publish domain event for asynchronous side effects
                // Event is held until transaction commits (Spring Modulith guarantee)
                eventPublisher.publishEvent(MoneyTransferredEvent.of(
                        saved.getId(),
                        request.fromId(),
                        request.toId(),
                        request.amount(),
                        request.currency()
                ));

                yield new TransactionResult.Success("Transfer completed successfully");
            }
        };

        return result;
    }

    @Override
    public Object getTransferHistory(UUID accountId) {
        return transferRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
}
