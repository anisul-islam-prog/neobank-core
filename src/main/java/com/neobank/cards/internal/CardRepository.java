package com.neobank.cards.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for card persistence.
 */
@Repository
interface CardRepository extends JpaRepository<CardEntity, UUID> {

    /**
     * Find all cards for a given account.
     *
     * @param accountId the account identifier
     * @return list of cards
     */
    List<CardEntity> findByAccountId(UUID accountId);

    /**
     * Check if a card number hash already exists.
     *
     * @param cardNumberHash the hashed card number
     * @return true if exists
     */
    boolean existsByCardNumberHash(String cardNumberHash);
}
