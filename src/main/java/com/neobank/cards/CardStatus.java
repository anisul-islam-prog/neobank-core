package com.neobank.cards;

/**
 * Status of a payment card.
 */
public enum CardStatus {
    /**
     * Card is active and can be used for transactions.
     */
    ACTIVE,

    /**
     * Card is temporarily frozen by the cardholder.
     * Can be unfrozen.
     */
    FROZEN,

    /**
     * Card is blocked by the bank (e.g., suspicious activity).
     * Requires bank intervention to restore.
     */
    BLOCKED,

    /**
     * Card reported as stolen.
     * Permanently inactive, requires replacement.
     */
    REPORTED_STOLEN,

    /**
     * Card has expired.
     */
    EXPIRED,

    /**
     * Card is being replaced with a new card.
     */
    REPLACED
}
