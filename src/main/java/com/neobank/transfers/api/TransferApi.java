package com.neobank.transfers.api;

import com.neobank.transfers.TransactionResult;
import com.neobank.transfers.TransferRequest;
import org.springframework.modulith.NamedInterface;

/**
 * Public API interface for the transfers module.
 * This defines the contract that other modules can use to interact with transfers.
 */
@NamedInterface("transfer-api")
public interface TransferApi {

    TransactionResult transfer(TransferRequest request);
}
