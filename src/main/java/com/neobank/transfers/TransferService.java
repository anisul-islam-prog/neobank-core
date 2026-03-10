package com.neobank.transfers;

import com.neobank.accounts.api.AccountApi;
import com.neobank.accounts.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountApi accountApi;

    public TransferService(AccountApi accountApi) {
        this.accountApi = accountApi;
    }

    @Transactional
    public TransactionResult transfer(TransferRequest request) {
        // Fetch both accounts
        Account fromAccount = accountApi.getAccountById(request.fromId());
        Account toAccount = accountApi.getAccountById(request.toId());

        // Check if from account has enough balance
        TransactionResult result = switch (fromAccount.balance().compareTo(request.amount())) {
            case -1 -> new TransactionResult.Failure("Insufficient balance"); // Less than
            default -> {
                // Update both balances
                Account updatedFromAccount = new Account(fromAccount.id(), fromAccount.ownerName(),
                    fromAccount.balance().subtract(request.amount()));
                Account updatedToAccount = new Account(toAccount.id(), toAccount.ownerName(),
                    toAccount.balance().add(request.amount()));

                // Save both accounts back
                accountApi.updateAccount(updatedFromAccount);
                accountApi.updateAccount(updatedToAccount);

                yield new TransactionResult.Success("Transfer completed successfully");
            }
        };

        return result;
    }
}