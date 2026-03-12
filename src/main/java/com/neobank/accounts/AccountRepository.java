package com.neobank.accounts;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Streamable<AccountEntity> findByOwnerNameContaining(String nameFragment);

    List<AccountEntity> findByBalanceGreaterThan(BigDecimal amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
    Optional<AccountEntity> findByIdWithLock(@Param("id") UUID id);
}
