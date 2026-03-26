package com.neobank.cards;

/**
 * Type of payment card.
 */
public enum CardType {
    /**
     * Virtual card for online transactions.
     * Issued instantly, no physical card.
     */
    VIRTUAL,

    /**
     * Physical card with plastic/metal body.
     * Requires shipping to cardholder address.
     */
    PHYSICAL
}
