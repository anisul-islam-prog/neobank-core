package com.neobank.loans.internal;

import com.neobank.loans.CreditScoreApi;
import com.neobank.loans.CreditScoreResult;
import com.neobank.loans.RiskProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock implementation of CreditScoreApi.
 * 
 * This service derives credit scores based on the RiskProfile implemented
 * in Phase 1. For demonstration purposes, it generates mock risk profiles
 * based on user ID hash.
 * 
 * In a production environment, this would integrate with:
 * - Credit bureaus (Experian, Equifax, TransUnion)
 * - Internal transaction history
 * - Payment behavior analysis
 */
@Service
public class CreditScoreService implements CreditScoreApi {

    /**
     * Mock user profiles for demonstration.
     * In production, this would be fetched from a database.
     */
    private final Map<UUID, RiskProfile> userProfiles = new HashMap<>();

    /**
     * Get the credit score for a user.
     * Uses a mock RiskProfile derived from user ID for demonstration.
     */
    @Override
    public CreditScoreResult getCreditScore(UUID userId) {
        RiskProfile profile = getUserProfile(userId);
        return CreditScoreResult.fromRiskProfile(userId.toString(), profile);
    }

    /**
     * Calculate a credit score based on risk profile.
     * Delegates to RiskProfile's calculateRiskScore method.
     */
    @Override
    public int calculateScore(RiskProfile riskProfile) {
        return riskProfile.calculateRiskScore();
    }

    /**
     * Get or create a mock risk profile for a user.
     * Uses deterministic values based on user ID hash for consistency.
     */
    private RiskProfile getUserProfile(UUID userId) {
        return userProfiles.computeIfAbsent(userId, this::createMockProfile);
    }

    /**
     * Create a deterministic mock profile based on user ID.
     * This ensures the same user always gets the same score.
     */
    private RiskProfile createMockProfile(UUID userId) {
        // Use hash code for deterministic but varied results
        int hash = Math.abs(userId.hashCode());
        
        // Generate realistic values based on hash
        int creditScore = 580 + (hash % 271); // 580-850 range
        BigDecimal debtToIncome = BigDecimal.valueOf(0.2 + (hash % 40) / 100.0); // 0.2-0.6
        int employmentYears = hash % 20; // 0-19 years
        BigDecimal annualIncome = BigDecimal.valueOf(30000 + (hash % 120000)); // $30k-$150k

        return new RiskProfile(creditScore, debtToIncome, employmentYears, annualIncome);
    }
}
