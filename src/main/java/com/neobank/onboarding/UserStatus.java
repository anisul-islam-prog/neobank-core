package com.neobank.onboarding;

/**
 * User status enumeration for account lifecycle management.
 * 
 * Status controls user access and approval workflow:
 * - PENDING: Awaiting staff approval (cannot login)
 * - ACTIVE: Fully activated (can access all authorized features)
 * - SUSPENDED: Temporarily disabled (cannot login)
 */
public enum UserStatus {
    /**
     * New user awaiting approval.
     * Cannot login or access any banking features.
     */
    PENDING,

    /**
     * Active and fully authorized user.
     * Can access all features permitted by their role.
     */
    ACTIVE,

    /**
     * Suspended account (e.g., suspicious activity, compliance review).
     * Cannot login or access any features.
     */
    SUSPENDED
}
