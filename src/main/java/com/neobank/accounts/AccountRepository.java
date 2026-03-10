package com.neobank.accounts;

import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Streamable<AccountEntity> findByOwnerNameContaining(String nameFragment);

    List<AccountEntity> findByBalanceGreaterThan(BigDecimal amount);
}
