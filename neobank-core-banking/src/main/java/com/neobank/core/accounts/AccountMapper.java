package com.neobank.core.accounts;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapper between Account domain record and AccountEntity JPA entity.
 */
@Component
public class AccountMapper {

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Account(
                entity.getId(),
                entity.getOwnerName(),
                entity.getBalance()
        );
    }

    public AccountEntity toEntity(Account account) {
        if (account == null) {
            return null;
        }
        AccountEntity entity = new AccountEntity();
        entity.setId(account.id());
        entity.setOwnerName(account.ownerName());
        entity.setBalance(account.balance());
        return entity;
    }

    public AccountEntity toEntityWithBranch(Account account, UUID branchId) {
        AccountEntity entity = toEntity(account);
        if (entity != null) {
            entity.setBranchId(branchId);
        }
        return entity;
    }
}
