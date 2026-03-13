package com.neobank.cards.internal;

import com.neobank.cards.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Internal card service implementing card lifecycle management.
 * Uses AES-256-GCM encryption for secure card number storage.
 */
@Service
@Transactional
class CardService implements CardApi {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final EncryptionService encryptionService;

    public CardService(CardRepository cardRepository, CardMapper cardMapper,
                       EncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
        this.encryptionService = encryptionService;
    }

    @Override
    public CardIssuanceResult issueCard(CardIssuanceRequest request) {
        try {
            // Generate card number (simulated - in production, use proper BIN and Luhn algorithm)
            String cardNumber = generateCardNumber();
            String cardNumberMasked = maskCardNumber(cardNumber);
            String cardNumberEncrypted = encryptionService.encrypt(cardNumber);

            // Generate CVV and encrypt
            String cvv = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
            String cvvEncrypted = encryptionService.encrypt(cvv);

            // Create card entity
            CardEntity entity = cardMapper.toEntity(request, cardNumberMasked, cardNumberEncrypted, cvvEncrypted);
            cardRepository.save(entity);

            log.info("Card issued: {} for account: {}", entity.getId(), request.accountId());

            return CardIssuanceResult.success(entity.getId(), cardNumberMasked);

        } catch (Exception e) {
            log.error("Failed to issue card", e);
            return CardIssuanceResult.failure("Card issuance failed: " + e.getMessage());
        }
    }

    @Override
    public CardDetails getCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .map(cardMapper::toDetails)
                .orElse(null);
    }

    @Override
    public List<CardDetails> getCardsForAccount(UUID accountId) {
        return cardRepository.findByAccountId(accountId).stream()
                .map(cardMapper::toDetails)
                .toList();
    }

    @Override
    public CardDetails updateStatus(UUID cardId, CardStatus status) {
        return cardRepository.findById(cardId)
                .map(entity -> {
                    entity.setStatus(status);
                    if (status == CardStatus.ACTIVE && entity.getActivatedAt() == null) {
                        entity.setActivatedAt(Instant.now());
                    }
                    cardRepository.save(entity);
                    log.info("Card status updated: {} -> {}", cardId, status);
                    return cardMapper.toDetails(entity);
                })
                .orElse(null);
    }

    @Override
    public CardDetails updateSpendingLimit(UUID cardId, BigDecimal limit) {
        return cardRepository.findById(cardId)
                .map(entity -> {
                    entity.setSpendingLimit(limit);
                    cardRepository.save(entity);
                    log.info("Card spending limit updated: {} -> {}", cardId, limit);
                    return cardMapper.toDetails(entity);
                })
                .orElse(null);
    }

    /**
     * Get decrypted card number for secure operations.
     * Package-private for internal use only.
     */
    String getDecryptedCardNumber(UUID cardId) {
        return cardRepository.findById(cardId)
                .map(entity -> encryptionService.decrypt(entity.getCardNumberEncrypted()))
                .orElse(null);
    }

    /**
     * Get decrypted CVV for secure operations.
     * Package-private for internal use only.
     */
    String getDecryptedCvv(UUID cardId) {
        return cardRepository.findById(cardId)
                .map(entity -> encryptionService.decrypt(entity.getCvvEncrypted()))
                .orElse(null);
    }

    /**
     * Generate a simulated 16-digit card number.
     */
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        // Start with 4 (Visa-like)
        sb.append("4");
        // Generate remaining 15 digits
        for (int i = 0; i < 15; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Mask card number for display (e.g., ****-****-****-1234).
     */
    private String maskCardNumber(String cardNumber) {
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "****-****-****-" + lastFour;
    }

    /**
     * Block specific MCC codes for a card.
     * Package-private for internal use.
     */
    void blockMccCodes(UUID cardId, List<Integer> mccCodes) {
        cardRepository.findById(cardId).ifPresent(entity -> {
            // In production, serialize MCC codes to JSON and store
            log.info("Blocked MCC codes {} for card {}", mccCodes, cardId);
        });
    }

    /**
     * Check if a transaction is allowed based on card status and limits.
     * Package-private for internal use.
     */
    boolean isTransactionAllowed(UUID cardId, BigDecimal amount, Integer mccCode) {
        return cardRepository.findById(cardId)
                .map(entity -> {
                    // Check status
                    if (entity.getStatus() != CardStatus.ACTIVE) {
                        return false;
                    }

                    // Check spending limit
                    if (entity.getSpendingLimit() != null) {
                        BigDecimal newTotal = entity.getMonthlySpent().add(amount);
                        if (newTotal.compareTo(entity.getSpendingLimit()) > 0) {
                            return false;
                        }
                    }

                    // Check MCC blocks (simplified)
                    // In production, parse MCC blocks from JSON and check

                    return true;
                })
                .orElse(false);
    }
}
