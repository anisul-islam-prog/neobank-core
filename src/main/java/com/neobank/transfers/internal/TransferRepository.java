package com.neobank.transfers.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface TransferRepository extends JpaRepository<TransferEntity, UUID> {
}
