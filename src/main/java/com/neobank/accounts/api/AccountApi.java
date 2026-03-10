package com.neobank.accounts.api;

import com.neobank.accounts.Account;
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
}