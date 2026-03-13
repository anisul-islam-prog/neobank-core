package com.neobank.loans.internal;

import com.neobank.loans.RiskProfile;

/**
 * Loan context holder using Java 25 ScopedValue.
 * Stores RiskProfile during loan application processing thread.
 */
public final class LoanContext {

    /**
     * ScopedValue for storing the current RiskProfile during loan processing.
     * This ensures thread-confined, immutable access to risk data.
     */
    private static final ScopedValue<RiskProfile> RISK_PROFILE_SCOPE = ScopedValue.newInstance();

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
        ScopedValue.where(RISK_PROFILE_SCOPE, riskProfile)
                .run(operation);
    }

    /**
     * Get the current RiskProfile from the scoped context.
     * Must be called within a {@link #runWithRiskProfile} block.
     *
     * @return the current RiskProfile
     * @throws IllegalStateException if called outside a scoped context
     */
    public static RiskProfile getCurrentRiskProfile() {
        return RISK_PROFILE_SCOPE.get();
    }

    /**
     * Check if a RiskProfile is currently bound to this thread.
     *
     * @return true if a RiskProfile is available
     */
    public static boolean hasRiskProfile() {
        try {
            RISK_PROFILE_SCOPE.get();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
