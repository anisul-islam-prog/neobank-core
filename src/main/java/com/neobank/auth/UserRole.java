package com.neobank.auth;

/**
 * User role enumeration for authorization.
 */
public enum UserRole {
    /**
     * Regular user with basic banking access.
     */
    USER,

    /**
     * Premium user with higher limits.
     */
    PREMIUM_USER,

    /**
     * Administrator with full system access.
     */
    ADMIN
}
