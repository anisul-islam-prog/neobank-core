package com.neobank.loans.internal;

import com.neobank.loans.RiskProfile;

/**
 * Loan context holder using ThreadLocal for thread-confined risk profile storage.
 * Stores RiskProfile during loan application processing thread.
 *
 * Note: Originally used Java 25 ScopedValue (preview API).
 * Migrated to ThreadLocal for Java 21 production stability.
 */
public final class LoanContext {

    /**
     * ThreadLocal for storing the current RiskProfile during loan processing.
     * Ensures thread-confined, immutable access to risk data.
     */
    private static final ThreadLocal<RiskProfile> RISK_PROFILE_SCOPE = new ThreadLocal<>();

    private LoanContext() {
        // Utility class - prevent instantiation
    }

    /**
     * Run a loan processing operation with the given risk profile.
     * The RiskProfile is available via {@link #getCurrentRiskProfile()} within the operation.
     *
     * @param riskProfile the risk profile for this loan application
     * @param operation the loan processing operation to execute
     */
    public static void runWithRiskProfile(RiskProfile riskProfile, Runnable operation) {
        RISK_PROFILE_SCOPE.set(riskProfile);
        try {
            operation.run();
        } finally {
            RISK_PROFILE_SCOPE.remove();
        }
    }

    /**
     * Get the current RiskProfile from the thread-local context.
     * Must be called within a {@link #runWithRiskProfile} block.
     *
     * @return the current RiskProfile
     * @throws IllegalStateException if called outside a scoped context
     */
    public static RiskProfile getCurrentRiskProfile() {
        RiskProfile profile = RISK_PROFILE_SCOPE.get();
        if (profile == null) {
            throw new IllegalStateException("No RiskProfile bound to current thread. Call within runWithRiskProfile().");
        }
        return profile;
    }

    /**
     * Check if a RiskProfile is currently bound to this thread.
     *
     * @return true if a RiskProfile is available
     */
    public static boolean hasRiskProfile() {
        return RISK_PROFILE_SCOPE.get() != null;
    }
}
