package com.neobank.transfers.internal;

import com.neobank.transfers.TransferRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
class TransferMapper {

    public TransferEntity toEntity(TransferRequest request) {
        TransferEntity entity = new TransferEntity();
        entity.setId(UUID.randomUUID());
        entity.setFromAccountId(request.fromId());
        entity.setToAccountId(request.toId());
        entity.setAmount(request.amount());
        entity.setCreatedAt(Instant.now());
        entity.setStatus("PENDING");
        return entity;
    }

    public TransferEntity toCompletedEntity(TransferEntity entity) {
        entity.setStatus("COMPLETED");
        return entity;
    }
}
