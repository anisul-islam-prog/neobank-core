package com.neobank.cards.internal;

import com.neobank.cards.CardDetails;
import com.neobank.cards.CardIssuanceRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Mapper between CardEntity and domain objects.
 */
@Component
class CardMapper {

    /**
     * Convert a CardIssuanceRequest to a CardEntity.
     */
    CardEntity toEntity(CardIssuanceRequest request, String cardNumberMasked, String cardNumberEncrypted, String cvvEncrypted) {
        CardEntity entity = new CardEntity();
        entity.setId(UUID.randomUUID());
        entity.setAccountId(request.accountId());
        entity.setCardType(request.cardType());
        entity.setStatus(com.neobank.cards.CardStatus.ACTIVE);
        entity.setSpendingLimit(request.spendingLimit());
        entity.setCardholderName(request.cardholderName());
        entity.setCardNumberMasked(cardNumberMasked);
        entity.setCardNumberEncrypted(cardNumberEncrypted);
        entity.setExpiryDate(YearMonth.now().plusYears(4));
        entity.setCvvEncrypted(cvvEncrypted);
        entity.setCreatedAt(Instant.now());
        entity.setMccBlocksJson("[]");
        entity.setMonthlySpent(java.math.BigDecimal.ZERO);
        return entity;
    }

    /**
     * Convert a CardEntity to CardDetails.
     */
    CardDetails toDetails(CardEntity entity) {
        return new CardDetails(
                entity.getId(),
                entity.getAccountId(),
                entity.getCardType(),
                entity.getStatus(),
                entity.getSpendingLimit(),
                entity.getCardholderName(),
                entity.getCardNumberMasked(),
                entity.getExpiryDate(),
                entity.getCvvEncrypted(),
                entity.getCreatedAt(),
                entity.getActivatedAt()
        );
    }
}
