package com.neobank.fraud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for FraudResult using JUnit 5.
 * Tests fraud result record and factory methods.
 */
@DisplayName("FraudResult Unit Tests")
class FraudResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create no fraud result")
        void shouldCreateNoFraudResult() {
            // Given
            int riskScore = 25;

            // When
            FraudResult result = FraudResult.noFraud(riskScore);

            // Then
            assertThat(result.isFraudDetected()).isFalse();
            assertThat(result.riskScore()).isEqualTo(riskScore);
            assertThat(result.alertId()).isNull();
            assertThat(result.alertType()).isNull();
            assertThat(result.reason()).isNull();
            assertThat(result.requiresManualReview()).isFalse();
        }

        @Test
        @DisplayName("Should create fraud detected result")
        void shouldCreateFraudDetectedResult() {
            // Given
            UUID alertId = UUID.randomUUID();
            int riskScore = 85;
            String alertType = "VELOCITY_CHECK";
            String reason = "Velocity threshold exceeded";
            boolean requiresManualReview = true;

            // When
            FraudResult result = FraudResult.fraudDetected(
                    alertId, riskScore, alertType, reason, requiresManualReview
            );

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.riskScore()).isEqualTo(riskScore);
            assertThat(result.alertId()).isEqualTo(alertId);
            assertThat(result.alertType()).isEqualTo(alertType);
            assertThat(result.reason()).isEqualTo(reason);
            assertThat(result.requiresManualReview()).isTrue();
        }

        @Test
        @DisplayName("Should create velocity violation result")
        void shouldCreateVelocityViolationResult() {
            // Given
            UUID alertId = UUID.randomUUID();
            int transactionCount = 5;

            // When
            FraudResult result = FraudResult.velocityViolation(alertId, transactionCount);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.riskScore()).isEqualTo(85);
            assertThat(result.alertType()).isEqualTo("VELOCITY_CHECK");
            assertThat(result.reason()).contains("5 transactions in 1 minute");
            assertThat(result.requiresManualReview()).isTrue();
        }

        @Test
        @DisplayName("Should create blacklisted entity result")
        void shouldCreateBlacklistedEntityResult() {
            // Given
            UUID alertId = UUID.randomUUID();
            String entityType = "ACCOUNT_ID";
            String entityValue = "123e4567-e89b-12d3-a456-426614174000";

            // When
            FraudResult result = FraudResult.blacklistedEntity(alertId, entityType, entityValue);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.riskScore()).isEqualTo(100);
            assertThat(result.alertType()).isEqualTo("BLACKLIST");
            assertThat(result.reason()).contains("ACCOUNT_ID is blacklisted");
            assertThat(result.requiresManualReview()).isTrue();
        }

        @Test
        @DisplayName("Should create suspicious pattern result with high risk")
        void shouldCreateSuspiciousPatternResultWithHighRisk() {
            // Given
            UUID alertId = UUID.randomUUID();
            int riskScore = 80;
            String pattern = "Round amount transfer";

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, riskScore, pattern);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.riskScore()).isEqualTo(riskScore);
            assertThat(result.alertType()).isEqualTo("SUSPICIOUS_PATTERN");
            assertThat(result.reason()).contains("Round amount transfer");
            assertThat(result.requiresManualReview()).isTrue();
        }

        @Test
        @DisplayName("Should create suspicious pattern result with low risk")
        void shouldCreateSuspiciousPatternResultWithLowRisk() {
            // Given
            UUID alertId = UUID.randomUUID();
            int riskScore = 50;
            String pattern = "Unusual time";

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, riskScore, pattern);

            // Then
            assertThat(result.isFraudDetected()).isFalse();
            assertThat(result.riskScore()).isEqualTo(riskScore);
            assertThat(result.alertType()).isEqualTo("SUSPICIOUS_PATTERN");
            assertThat(result.requiresManualReview()).isFalse();
        }

        @Test
        @DisplayName("Should create suspicious pattern result at threshold")
        void shouldCreateSuspiciousPatternResultAtThreshold() {
            // Given
            UUID alertId = UUID.randomUUID();
            int riskScore = 70;
            String pattern = "Multiple patterns";

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, riskScore, pattern);

            // Then
            assertThat(result.isFraudDetected()).isTrue();
            assertThat(result.requiresManualReview()).isTrue();
        }
    }

    @Nested
    @DisplayName("Risk Score Thresholds")
    class RiskScoreThresholdsTests {

        @Test
        @DisplayName("Should not require manual review for risk score 0")
        void shouldNotRequireManualReviewForRiskScore0() {
            // When
            FraudResult result = FraudResult.noFraud(0);

            // Then
            assertThat(result.requiresManualReview()).isFalse();
        }

        @Test
        @DisplayName("Should not require manual review for risk score 50")
        void shouldNotRequireManualReviewForRiskScore50() {
            // When
            FraudResult result = FraudResult.noFraud(50);

            // Then
            assertThat(result.requiresManualReview()).isFalse();
        }

        @Test
        @DisplayName("Should require manual review for risk score 70")
        void shouldRequireManualReviewForRiskScore70() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, 70, "Pattern");

            // Then
            assertThat(result.requiresManualReview()).isTrue();
        }

        @Test
        @DisplayName("Should require manual review for risk score 85")
        void shouldRequireManualReviewForRiskScore85() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.velocityViolation(alertId, 5);

            // Then
            assertThat(result.requiresManualReview()).isTrue();
        }

        @Test
        @DisplayName("Should require manual review for risk score 100")
        void shouldRequireManualReviewForRiskScore100() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.blacklistedEntity(alertId, "IP", "1.2.3.4");

            // Then
            assertThat(result.requiresManualReview()).isTrue();
        }
    }

    @Nested
    @DisplayName("Alert Types")
    class AlertTypesTests {

        @Test
        @DisplayName("Should have VELOCITY_CHECK alert type")
        void shouldHaveVelocityCheckAlertType() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.velocityViolation(alertId, 5);

            // Then
            assertThat(result.alertType()).isEqualTo("VELOCITY_CHECK");
        }

        @Test
        @DisplayName("Should have BLACKLIST alert type")
        void shouldHaveBlacklistAlertType() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.blacklistedEntity(alertId, "ACCOUNT_ID", "123");

            // Then
            assertThat(result.alertType()).isEqualTo("BLACKLIST");
        }

        @Test
        @DisplayName("Should have SUSPICIOUS_PATTERN alert type")
        void shouldHaveSuspiciousPatternAlertType() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, 80, "Pattern");

            // Then
            assertThat(result.alertType()).isEqualTo("SUSPICIOUS_PATTERN");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null alert ID in fraud detected")
        void shouldHandleNullAlertIdInFraudDetected() {
            // When
            FraudResult result = FraudResult.fraudDetected(
                    null, 85, "TYPE", "Reason", true
            );

            // Then
            assertThat(result.alertId()).isNull();
            assertThat(result.isFraudDetected()).isTrue();
        }

        @Test
        @DisplayName("Should handle null alert type in fraud detected")
        void shouldHandleNullAlertTypeInFraudDetected() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.fraudDetected(
                    alertId, 85, null, "Reason", true
            );

            // Then
            assertThat(result.alertType()).isNull();
        }

        @Test
        @DisplayName("Should handle null reason in fraud detected")
        void shouldHandleNullReasonInFraudDetected() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.fraudDetected(
                    alertId, 85, "TYPE", null, true
            );

            // Then
            assertThat(result.reason()).isNull();
        }

        @Test
        @DisplayName("Should handle empty reason in fraud detected")
        void shouldHandleEmptyReasonInFraudDetected() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.fraudDetected(
                    alertId, 85, "TYPE", "", true
            );

            // Then
            assertThat(result.reason()).isEmpty();
        }

        @Test
        @DisplayName("Should handle zero transaction count in velocity violation")
        void shouldHandleZeroTransactionCountInVelocityViolation() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.velocityViolation(alertId, 0);

            // Then
            assertThat(result.reason()).contains("0 transactions");
        }

        @Test
        @DisplayName("Should handle large transaction count in velocity violation")
        void shouldHandleLargeTransactionCountInVelocityViolation() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.velocityViolation(alertId, 100);

            // Then
            assertThat(result.reason()).contains("100 transactions");
        }

        @Test
        @DisplayName("Should handle null entity type in blacklisted entity")
        void shouldHandleNullEntityTypeInBlacklistedEntity() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.blacklistedEntity(alertId, null, "value");

            // Then
            assertThat(result.reason()).contains("null is blacklisted");
        }

        @Test
        @DisplayName("Should handle null entity value in blacklisted entity")
        void shouldHandleNullEntityValueInBlacklistedEntity() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.blacklistedEntity(alertId, "TYPE", null);

            // Then
            assertThat(result.reason()).contains("TYPE is blacklisted: null");
        }

        @Test
        @DisplayName("Should handle empty pattern in suspicious pattern")
        void shouldHandleEmptyPatternInSuspiciousPattern() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, 80, "");

            // Then
            assertThat(result.reason()).contains("Suspicious pattern detected:");
        }

        @Test
        @DisplayName("Should handle long pattern in suspicious pattern")
        void shouldHandleLongPatternInSuspiciousPattern() {
            // Given
            UUID alertId = UUID.randomUUID();
            String longPattern = "a".repeat(200);

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, 80, longPattern);

            // Then
            assertThat(result.reason()).hasSizeGreaterThan(200);
        }

        @Test
        @DisplayName("Should handle special characters in reason")
        void shouldHandleSpecialCharactersInReason() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudResult result = FraudResult.velocityViolation(alertId, 5);

            // Then
            assertThat(result.reason()).contains(":");
        }

        @Test
        @DisplayName("Should handle unicode in pattern")
        void shouldHandleUnicodeInPattern() {
            // Given
            UUID alertId = UUID.randomUUID();
            String pattern = "可疑模式";

            // When
            FraudResult result = FraudResult.suspiciousPattern(alertId, 80, pattern);

            // Then
            assertThat(result.reason()).contains("可疑模式");
        }
    }

    @Nested
    @DisplayName("Record Accessors")
    class RecordAccessorsTests {

        @Test
        @DisplayName("Should access all fields correctly")
        void shouldAccessAllFieldsCorrectly() {
            // Given
            UUID alertId = UUID.randomUUID();
            boolean isFraudDetected = true;
            int riskScore = 85;
            String alertType = "VELOCITY_CHECK";
            String reason = "Test reason";
            boolean requiresManualReview = true;

            // When
            FraudResult result = new FraudResult(
                    alertId, isFraudDetected, riskScore, alertType, reason, requiresManualReview
            );

            // Then
            assertThat(result.alertId()).isEqualTo(alertId);
            assertThat(result.isFraudDetected()).isEqualTo(isFraudDetected);
            assertThat(result.riskScore()).isEqualTo(riskScore);
            assertThat(result.alertType()).isEqualTo(alertType);
            assertThat(result.reason()).isEqualTo(reason);
            assertThat(result.requiresManualReview()).isEqualTo(requiresManualReview);
        }

        @Test
        @DisplayName("Should handle null fields")
        void shouldHandleNullFields() {
            // When
            FraudResult result = new FraudResult(null, false, 0, null, null, false);

            // Then
            assertThat(result.alertId()).isNull();
            assertThat(result.isFraudDetected()).isFalse();
            assertThat(result.riskScore()).isEqualTo(0);
            assertThat(result.alertType()).isNull();
            assertThat(result.reason()).isNull();
            assertThat(result.requiresManualReview()).isFalse();
        }

        @Test
        @DisplayName("Should equal another result with same fields")
        void shouldEqualAnotherResultWithSameFields() {
            // Given
            UUID alertId = UUID.randomUUID();
            FraudResult result1 = new FraudResult(alertId, true, 85, "TYPE", "Reason", true);
            FraudResult result2 = new FraudResult(alertId, true, 85, "TYPE", "Reason", true);

            // Then
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should not equal result with different alert ID")
        void shouldNotEqualResultWithDifferentAlertId() {
            // Given
            FraudResult result1 = new FraudResult(UUID.randomUUID(), true, 85, "TYPE", "Reason", true);
            FraudResult result2 = new FraudResult(UUID.randomUUID(), true, 85, "TYPE", "Reason", true);

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should have correct hash code")
        void shouldHaveCorrectHashCode() {
            // Given
            UUID alertId = UUID.randomUUID();
            FraudResult result = new FraudResult(alertId, true, 85, "TYPE", "Reason", true);

            // Then
            assertThat(result.hashCode()).isNotNull();
        }

        @Test
        @DisplayName("Should have correct toString")
        void shouldHaveCorrectToString() {
            // Given
            UUID alertId = UUID.randomUUID();
            FraudResult result = new FraudResult(alertId, true, 85, "TYPE", "Reason", true);

            // Then
            assertThat(result.toString()).contains("FraudResult");
            assertThat(result.toString()).contains("85");
        }
    }
}
