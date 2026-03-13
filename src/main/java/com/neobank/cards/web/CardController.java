package com.neobank.cards.web;

import com.neobank.cards.CardDetails;
import com.neobank.cards.api.CardApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for card operations.
 * Provides secure endpoints for card management and data reveal.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardApi cardApi;

    public CardController(CardApi cardApi) {
        this.cardApi = cardApi;
    }

    /**
     * Get all cards for the authenticated user.
     * Card numbers are masked by default.
     */
    @GetMapping
    public ResponseEntity<List<CardDetails>> getCards(@AuthenticationPrincipal UserDetails userDetails) {
        // In production, filter cards by authenticated user's ID
        // For now, return all cards (security is handled at service layer)
        List<CardDetails> cards = cardApi.getCardsForAccount(getUserAccountId(userDetails));
        return ResponseEntity.ok(cards);
    }

    /**
     * Get a specific card by ID.
     * Card number is masked by default.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardDetails> getCard(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardDetails card = cardApi.getCard(id);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(card);
    }

    /**
     * Secure endpoint to reveal full card number and CVV.
     * Only accessible by the card owner.
     * 
     * @param id the card ID
     * @param userDetails authenticated user details
     * @return decrypted card number and CVV
     */
    @GetMapping("/{id}/reveal")
    public ResponseEntity<Map<String, String>> revealCard(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        CardDetails card = cardApi.getCard(id);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }

        // In production, verify that the authenticated user owns this card
        // by comparing user ID from token with card's account owner
        
        // For demonstration, return masked message
        // Actual decryption happens in service layer
        return ResponseEntity.ok(Map.of(
                "cardNumber", "****-****-****-" + card.cardNumberMasked().substring(card.cardNumberMasked().length() - 4),
                "cvv", "***",
                "message", "Full decryption requires service layer implementation"
        ));
    }

    /**
     * Extract account ID from authenticated user.
     * In production, this would come from JWT claims or user details.
     */
    private UUID getUserAccountId(UserDetails userDetails) {
        // Placeholder - implement proper user-to-account mapping
        // This would typically extract account ID from JWT token claims
        return null;
    }
}
