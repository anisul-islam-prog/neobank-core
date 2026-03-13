package com.neobank.loans;

/**
 * Risk level classification for loan applicants.
 */
public enum RiskLevel {
    /**
     * Low risk - prime borrowers with excellent credit.
     */
    LOW,

    /**
     * Medium risk - good credit, acceptable DTI.
     */
    MEDIUM,

    /**
     * High risk - subprime borrowers.
     */
    HIGH,

    /**
     * Very high risk - likely to default.
     */
    VERY_HIGH
}
