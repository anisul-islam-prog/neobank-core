package com.neobank.core.transfers.internal;

import com.neobank.core.accounts.api.AccountApi;
import com.neobank.core.approvals.ApprovalService;
import com.neobank.core.approvals.PendingAuthorization;
import com.neobank.core.transfers.MoneyTransferredEvent;
import com.neobank.core.transfers.TransactionResult;
import com.neobank.core.transfers.TransferRequest;
import com.neobank.core.transfers.api.TransferApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Service for executing fund transfers between accounts.
 * Implements Maker-Checker protocol for high-value transfers.
 */
@Service
@Transactional
class TransferService implements TransferApi {

    private static final String TRANSFER_PENDING_EVENT = "TRANSFER_PENDING_APPROVAL";

    private final AccountApi accountApi;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovalService approvalService;

    public TransferService(AccountApi accountApi, TransferRepository transferRepository,
                          TransferMapper transferMapper, ApplicationEventPublisher eventPublisher,
                          ApprovalService approvalService) {
        this.accountApi = accountApi;
        this.transferRepository = transferRepository;
        this.transferMapper = transferMapper;
        this.eventPublisher = eventPublisher;
        this.approvalService = approvalService;
    }

    @Override
    public TransactionResult transfer(TransferRequest request) {
        // Check if this transfer requires approval (Maker-Checker protocol)
        if (approvalService.requiresApproval(request.amount())) {
            // Create pending authorization instead of executing transfer
            PendingAuthorization auth = approvalService.createTransferAuthorization(
                request.initiatorId() != null ? request.initiatorId() : UUID.randomUUID(),
                request.initiatorRole() != null ? request.initiatorRole() : "TELLER",
                request.toId(),
                request.amount(),
                request.currency(),
                request.reason() != null ? request.reason() : "High-value transfer requiring approval"
            );

            // Publish event indicating transfer is pending approval
            eventPublisher.publishEvent(new TransferPendingEvent(
                auth.getId(),
                request.fromId(),
                request.toId(),
                request.amount(),
                request.currency()
            ));

            return new TransactionResult.Pending(
                "Transfer requires approval. Authorization ID: " + auth.getId(),
                auth.getId()
            );
        }

        // Execute normal transfer for amounts below threshold
        return executeTransfer(request);
    }

    /**
     * Execute a transfer without requiring approval.
     */
    private TransactionResult executeTransfer(TransferRequest request) {
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

    /**
     * Event published when a transfer is pending approval.
     */
    public record TransferPendingEvent(
        UUID authorizationId,
        UUID fromId,
        UUID toId,
        java.math.BigDecimal amount,
        String currency
    ) {}
}
