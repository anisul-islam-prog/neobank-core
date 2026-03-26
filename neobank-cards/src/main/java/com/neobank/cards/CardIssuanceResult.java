package com.neobank.cards;

import java.util.UUID;

/**
 * Result of a card issuance request.
 *
 * @param cardId the assigned card ID (null if failed)
 * @param success whether the issuance was successful
 * @param message a descriptive message
 * @param cardNumberMasked the masked card number (e.g., "****-****-****-1234")
 */
public record CardIssuanceResult(
        UUID cardId,
        boolean success,
        String message,
        String cardNumberMasked
) {
    public static CardIssuanceResult success(UUID cardId, String maskedNumber) {
        return new CardIssuanceResult(cardId, true, "Card issued successfully", maskedNumber);
    }

    public static CardIssuanceResult failure(String reason) {
        return new CardIssuanceResult(null, false, reason, null);
    }
}
