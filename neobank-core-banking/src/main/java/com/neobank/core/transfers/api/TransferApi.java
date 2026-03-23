package com.neobank.core.transfers.api;

import com.neobank.core.transfers.TransactionResult;
import com.neobank.core.transfers.TransferRequest;
import org.springframework.modulith.NamedInterface;

import java.util.UUID;

/**
 * Public API interface for the transfers module.
 */
@NamedInterface("transfer-api")
public interface TransferApi {

    /**
     * Execute a fund transfer between two accounts.
     *
     * @param request the transfer request containing from, to, amount, and currency
     * @return the transaction result (success or failure)
     */
    TransactionResult transfer(TransferRequest request);

    /**
     * Get transfer history for an account.
     *
     * @param accountId the account to get history for
     * @return list of transfer records
     */
    Object getTransferHistory(UUID accountId);
}
