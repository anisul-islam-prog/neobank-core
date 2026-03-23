package com.neobank.core.transfers.internal;

import com.neobank.core.transfers.TransferRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper between TransferRequest and TransferEntity.
 */
@Component
public class TransferMapper {

    public TransferEntity toEntity(TransferRequest request) {
        TransferEntity entity = new TransferEntity();
        entity.setId(UUID.randomUUID());
        entity.setFromAccountId(request.fromId());
        entity.setToAccountId(request.toId());
        entity.setAmount(request.amount());
        entity.setCurrency(request.currency());
        entity.setStatus(TransferStatus.PENDING);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    public void toCompletedEntity(TransferEntity entity) {
        entity.setStatus(TransferStatus.COMPLETED);
        entity.setCompletedAt(Instant.now());
    }

    public void toFailedEntity(TransferEntity entity, String reason) {
        entity.setStatus(TransferStatus.FAILED);
        entity.setCompletedAt(Instant.now());
        entity.setFailureReason(reason);
    }
}
