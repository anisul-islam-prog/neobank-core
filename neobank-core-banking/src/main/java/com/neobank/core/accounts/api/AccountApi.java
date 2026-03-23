package com.neobank.core.accounts.api;

import com.neobank.core.accounts.Account;
import org.springframework.modulith.NamedInterface;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API interface for the accounts module.
 * This defines the contract that other modules can use to interact with accounts.
 */
@NamedInterface("account-api")
public interface AccountApi {

    Account createNewAccount(String owner, BigDecimal initialBalance);

    Optional<Account> getAccount(UUID id);

    Account getAccountById(UUID id);

    Account updateAccount(Account account);

    /**
     * Get an account with pessimistic write lock for concurrent operations.
     * Use this method when performing balance updates to prevent race conditions.
     */
    Account getAccountWithLock(UUID id);

    /**
     * Credit an account with the specified amount.
     * Used for loan disbursements and incoming transfers.
     *
     * @param accountId the account to credit
     * @param amount the amount to credit
     * @return the updated account
     */
    Account creditAccount(UUID accountId, BigDecimal amount);

    /**
     * Create a new account linked to a specific branch.
     *
     * @param owner the account owner name
     * @param initialBalance the initial balance
     * @param branchId the branch identifier (optional)
     * @return the created account
     */
    Account createNewAccountWithBranch(String owner, BigDecimal initialBalance, UUID branchId);
}
