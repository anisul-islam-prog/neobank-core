package com.neobank.cards.api;

import com.neobank.cards.CardDetails;
import com.neobank.cards.CardIssuanceRequest;
import com.neobank.cards.CardIssuanceResult;
import com.neobank.cards.CardStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Public API interface for the cards module.
 * Defines the contract for card issuance and management.
 */
public interface CardApi {

    /**
     * Issue a new card linked to an account.
     *
     * @param request the card issuance request
     * @return the card issuance result
     */
    CardIssuanceResult issueCard(CardIssuanceRequest request);

    /**
     * Get card details by ID.
     *
     * @param cardId the card identifier
     * @return the card details or null if not found
     */
    CardDetails getCard(UUID cardId);

    /**
     * Get all cards for an account.
     *
     * @param accountId the account identifier
     * @return list of cards
     */
    List<CardDetails> getCardsForAccount(UUID accountId);

    /**
     * Update card status.
     *
     * @param cardId the card identifier
     * @param status the new status
     * @return the updated card details
     */
    CardDetails updateStatus(UUID cardId, CardStatus status);

    /**
     * Update spending limit for a card.
     *
     * @param cardId the card identifier
     * @param limit the new spending limit
     * @return the updated card details
     */
    CardDetails updateSpendingLimit(UUID cardId, BigDecimal limit);
}
