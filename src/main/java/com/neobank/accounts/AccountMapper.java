package com.neobank.accounts;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class AccountMapper {

    public AccountEntity toEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setId(account.id());
        entity.setOwnerName(account.ownerName());
        entity.setBalance(account.balance());
        entity.setTransactionHistory(new ArrayList<>());
        return entity;
    }

    public Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                entity.getOwnerName(),
                entity.getBalance()
        );
    }
}
