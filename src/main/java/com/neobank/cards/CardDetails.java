package com.neobank.cards;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Detailed information about a card.
 *
 * @param id the card identifier
 * @param accountId the linked account ID
 * @param cardType the type of card
 * @param status the current card status
 * @param spendingLimit the spending limit (null for no limit)
 * @param cardholderName the name on the card
 * @param cardNumberMasked the masked card number
 * @param expiryDate the card expiry date
 * @param cvvHash hashed CVV for verification (never stored in plain text)
 * @param createdAt when the card was created
 * @param activatedAt when the card was activated (null if not yet)
 */
public record CardDetails(
        UUID id,
        UUID accountId,
        CardType cardType,
        CardStatus status,
        BigDecimal spendingLimit,
        String cardholderName,
        String cardNumberMasked,
        YearMonth expiryDate,
        String cvvHash,
        Instant createdAt,
        Instant activatedAt
) {
}
