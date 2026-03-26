package com.neobank.cards;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request to issue a new card.
 *
 * @param accountId the linked account ID
 * @param cardType the type of card (VIRTUAL or PHYSICAL)
 * @param spendingLimit the initial spending limit (null for no limit)
 * @param cardholderName the name on the card
 */
public record CardIssuanceRequest(
        UUID accountId,
        CardType cardType,
        BigDecimal spendingLimit,
        String cardholderName
) {
    public CardIssuanceRequest {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }
        if (cardType == null) {
            throw new IllegalArgumentException("cardType must not be null");
        }
        if (cardholderName == null || cardholderName.isBlank()) {
            throw new IllegalArgumentException("cardholderName must not be blank");
        }
    }
}
