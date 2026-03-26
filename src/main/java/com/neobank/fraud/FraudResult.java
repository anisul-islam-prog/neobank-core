package com.neobank.fraud;

import java.util.UUID;

/**
 * Result of fraud analysis operation.
 *
 * @param alertId the fraud alert ID (null if no fraud detected)
 * @param isFraudDetected whether fraud was detected
 * @param riskScore the calculated risk score (0-100)
 * @param alertType the type of fraud alert
 * @param reason the reason for fraud detection
 * @param requiresManualReview whether manual review is required
 */
public record FraudResult(
        UUID alertId,
        boolean isFraudDetected,
        int riskScore,
        String alertType,
        String reason,
        boolean requiresManualReview
) {
    /**
     * Create a fraud result indicating no fraud detected.
     */
    public static FraudResult noFraud(int riskScore) {
        return new FraudResult(null, false, riskScore, null, null, false);
    }

    /**
     * Create a fraud result indicating fraud detected.
     */
    public static FraudResult fraudDetected(
            UUID alertId,
            int riskScore,
            String alertType,
            String reason,
            boolean requiresManualReview
    ) {
        return new FraudResult(alertId, true, riskScore, alertType, reason, requiresManualReview);
    }

    /**
     * Create a fraud result for velocity check violation.
     */
    public static FraudResult velocityViolation(UUID alertId, int transactionCount) {
        return new FraudResult(
                alertId,
                true,
                85,
                "VELOCITY_CHECK",
                "Account exceeded transaction velocity threshold: " + transactionCount + " transactions in 1 minute",
                true
        );
    }

    /**
     * Create a fraud result for blacklisted entity.
     */
    public static FraudResult blacklistedEntity(UUID alertId, String entityType, String entityValue) {
        return new FraudResult(
                alertId,
                true,
                100,
                "BLACKLIST",
                entityType + " is blacklisted: " + entityValue,
                true
        );
    }

    /**
     * Create a fraud result for suspicious pattern.
     */
    public static FraudResult suspiciousPattern(UUID alertId, int riskScore, String pattern) {
        return new FraudResult(
                alertId,
                riskScore >= 70,
                riskScore,
                "SUSPICIOUS_PATTERN",
                "Suspicious pattern detected: " + pattern,
                riskScore >= 70
        );
    }
}
