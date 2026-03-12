package com.neobank.accounts;

import com.neobank.accounts.api.AccountApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AccountService implements AccountApi {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public Account createNewAccount(String owner, BigDecimal initialBalance) {
        Account account = new Account(UUID.randomUUID(), owner, initialBalance);
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity saved = accountRepository.save(entity);
        return accountMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccount(UUID id) {
        return accountRepository.findById(id)
                .map(accountMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .map(accountMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Override
    @Transactional
    public Account getAccountWithLock(UUID id) {
        return accountRepository.findByIdWithLock(id)
                .map(accountMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    public Account updateAccount(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity updated = accountRepository.save(entity);
        return accountMapper.toDomain(updated);
    }
}
