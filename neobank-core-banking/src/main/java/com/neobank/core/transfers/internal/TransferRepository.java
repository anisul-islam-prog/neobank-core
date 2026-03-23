package com.neobank.core.transfers.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for TransferEntity.
 */
public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    List<TransferEntity> findByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId);

    List<TransferEntity> findByFromAccountId(UUID fromAccountId);

    List<TransferEntity> findByToAccountId(UUID toAccountId);
}
