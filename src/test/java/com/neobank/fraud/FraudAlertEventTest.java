package com.neobank.fraud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for FraudAlertEvent using JUnit 5.
 * Tests domain event record and factory methods.
 */
@DisplayName("FraudAlertEvent Unit Tests")
class FraudAlertEventTest {

    @Nested
    @DisplayName("Event Creation")
    class EventCreationTests {

        @Test
        @DisplayName("Should create event with all required fields")
        void shouldCreateEventWithAllRequiredFields() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            String alertType = "VELOCITY_CHECK";
            String reason = "Velocity threshold exceeded";
            int riskScore = 85;

            // When
            FraudAlertEvent event = new FraudAlertEvent(
                    alertId, transferId, fromAccountId, toAccountId,
                    alertType, reason, riskScore, "2024-01-15T10:30:00Z"
            );

            // Then
            assertThat(event.alertId()).isEqualTo(alertId);
            assertThat(event.transferId()).isEqualTo(transferId);
            assertThat(event.fromAccountId()).isEqualTo(fromAccountId);
            assertThat(event.toAccountId()).isEqualTo(toAccountId);
            assertThat(event.alertType()).isEqualTo(alertType);
            assertThat(event.reason()).isEqualTo(reason);
            assertThat(event.riskScore()).isEqualTo(riskScore);
            assertThat(event.occurredAt()).isEqualTo("2024-01-15T10:30:00Z");
        }

        @Test
        @DisplayName("Should create event using factory method")
        void shouldCreateEventUsingFactoryMethod() {
            // Given
            UUID alertId = UUID.randomUUID();
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            String alertType = "BLACKLIST";
            String reason = "Account is blacklisted";
            int riskScore = 100;

            // When
            FraudAlertEvent event = FraudAlertEvent.of(
                    alertId, transferId, fromAccountId, toAccountId,
                    alertType, reason, riskScore
            );

            // Then
            assertThat(event.alertId()).isEqualTo(alertId);
            assertThat(event.transferId()).isEqualTo(transferId);
            assertThat(event.fromAccountId()).isEqualTo(fromAccountId);
            assertThat(event.toAccountId()).isEqualTo(toAccountId);
            assertThat(event.alertType()).isEqualTo(alertType);
            assertThat(event.reason()).isEqualTo(reason);
            assertThat(event.riskScore()).isEqualTo(riskScore);
            assertThat(event.occurredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set occurredAt timestamp in factory method")
        void shouldSetOccurredAtTimestampInFactoryMethod() {
            // Given
            UUID alertId = UUID.randomUUID();

            // When
            FraudAlertEvent event = FraudAlertEvent.of(
                    alertId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85
            );

            // Then
            assertThat(event.occurredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Field Validation")
    class FieldValidationTests {

        @Test
        @DisplayName("Should throw exception when alertId is null")
        void shouldThrowExceptionWhenAlertIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("alertId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when transferId is null")
        void shouldThrowExceptionWhenTransferIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("transferId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when fromAccountId is null")
        void shouldThrowExceptionWhenFromAccountIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID(),
                    "TYPE", "Reason", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fromAccountId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when toAccountId is null")
        void shouldThrowExceptionWhenToAccountIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                    "TYPE", "Reason", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("toAccountId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when alertType is null")
        void shouldThrowExceptionWhenAlertTypeIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    null, "Reason", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("alertType must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when alertType is blank")
        void shouldThrowExceptionWhenAlertTypeIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "   ", "Reason", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("alertType must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when reason is null")
        void shouldThrowExceptionWhenReasonIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", null, 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reason must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when reason is blank")
        void shouldThrowExceptionWhenReasonIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "   ", 85, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reason must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when riskScore is negative")
        void shouldThrowExceptionWhenRiskScoreIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", -10, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("riskScore must be between 0 and 100");
        }

        @Test
        @DisplayName("Should throw exception when riskScore is above 100")
        void shouldThrowExceptionWhenRiskScoreIsAbove100() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 150, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("riskScore must be between 0 and 100");
        }

        @Test
        @DisplayName("Should throw exception when occurredAt is null")
        void shouldThrowExceptionWhenOccurredAtIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("occurredAt must not be null");
        }
    }

    @Nested
    @DisplayName("Risk Score Validation")
    class RiskScoreValidationTests {

        @Test
        @DisplayName("Should accept risk score of 0")
        void shouldAcceptRiskScoreOf0() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 0
            );

            // Then
            assertThat(event.riskScore()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should accept risk score of 50")
        void shouldAcceptRiskScoreOf50() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 50
            );

            // Then
            assertThat(event.riskScore()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should accept risk score of 100")
        void shouldAcceptRiskScoreOf100() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 100
            );

            // Then
            assertThat(event.riskScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should accept risk score of 1")
        void shouldAcceptRiskScoreOf1() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 1
            );

            // Then
            assertThat(event.riskScore()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should accept risk score of 99")
        void shouldAcceptRiskScoreOf99() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 99
            );

            // Then
            assertThat(event.riskScore()).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("Alert Types")
    class AlertTypesTests {

        @Test
        @DisplayName("Should accept VELOCITY_CHECK alert type")
        void shouldAcceptVelocityCheckAlertType() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "VELOCITY_CHECK", "Reason", 85
            );

            // Then
            assertThat(event.alertType()).isEqualTo("VELOCITY_CHECK");
        }

        @Test
        @DisplayName("Should accept BLACKLIST alert type")
        void shouldAcceptBlacklistAlertType() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "BLACKLIST", "Reason", 100
            );

            // Then
            assertThat(event.alertType()).isEqualTo("BLACKLIST");
        }

        @Test
        @DisplayName("Should accept SUSPICIOUS_PATTERN alert type")
        void shouldAcceptSuspiciousPatternAlertType() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "SUSPICIOUS_PATTERN", "Reason", 75
            );

            // Then
            assertThat(event.alertType()).isEqualTo("SUSPICIOUS_PATTERN");
        }

        @Test
        @DisplayName("Should accept custom alert type")
        void shouldAcceptCustomAlertType() {
            // Given/When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "CUSTOM_TYPE", "Reason", 50
            );

            // Then
            assertThat(event.alertType()).isEqualTo("CUSTOM_TYPE");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle same from and to account ID")
        void shouldHandleSameFromAndToAccountId() {
            // Given
            UUID accountId = UUID.randomUUID();

            // When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), accountId, accountId,
                    "VELOCITY_CHECK", "Reason", 85
            );

            // Then
            assertThat(event.fromAccountId()).isEqualTo(accountId);
            assertThat(event.toAccountId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should handle long reason string")
        void shouldHandleLongReasonString() {
            // Given
            String longReason = "a".repeat(500);

            // When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", longReason, 85
            );

            // Then
            assertThat(event.reason()).hasSize(500);
        }

        @Test
        @DisplayName("Should handle reason with special characters")
        void shouldHandleReasonWithSpecialCharacters() {
            // Given
            String reason = "Velocity check: 5 transactions in 60 seconds!";

            // When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "VELOCITY_CHECK", reason, 85
            );

            // Then
            assertThat(event.reason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle reason with unicode characters")
        void shouldHandleReasonWithUnicodeCharacters() {
            // Given
            String reason = "欺诈检测：可疑活动";

            // When
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", reason, 85
            );

            // Then
            assertThat(event.reason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle ISO 8601 timestamp")
        void shouldHandleIso8601Timestamp() {
            // Given
            String timestamp = "2024-01-15T10:30:00.123Z";

            // When
            FraudAlertEvent event = new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85, timestamp
            );

            // Then
            assertThat(event.occurredAt()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should handle epoch timestamp")
        void shouldHandleEpochTimestamp() {
            // Given
            String timestamp = String.valueOf(System.currentTimeMillis());

            // When
            FraudAlertEvent event = new FraudAlertEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85, timestamp
            );

            // Then
            assertThat(event.occurredAt()).isEqualTo(timestamp);
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
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            String alertType = "VELOCITY_CHECK";
            String reason = "Test reason";
            int riskScore = 85;
            String occurredAt = "2024-01-15T10:30:00Z";

            // When
            FraudAlertEvent event = new FraudAlertEvent(
                    alertId, transferId, fromAccountId, toAccountId,
                    alertType, reason, riskScore, occurredAt
            );

            // Then
            assertThat(event.alertId()).isEqualTo(alertId);
            assertThat(event.transferId()).isEqualTo(transferId);
            assertThat(event.fromAccountId()).isEqualTo(fromAccountId);
            assertThat(event.toAccountId()).isEqualTo(toAccountId);
            assertThat(event.alertType()).isEqualTo(alertType);
            assertThat(event.reason()).isEqualTo(reason);
            assertThat(event.riskScore()).isEqualTo(riskScore);
            assertThat(event.occurredAt()).isEqualTo(occurredAt);
        }

        @Test
        @DisplayName("Should equal another event with same fields")
        void shouldEqualAnotherEventWithSameFields() {
            // Given
            UUID alertId = UUID.randomUUID();
            FraudAlertEvent event1 = new FraudAlertEvent(
                    alertId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85, "2024-01-15T10:30:00Z"
            );
            FraudAlertEvent event2 = new FraudAlertEvent(
                    alertId, event1.transferId(), event1.fromAccountId(), event1.toAccountId(),
                    event1.alertType(), event1.reason(), event1.riskScore(), event1.occurredAt()
            );

            // Then
            assertThat(event1).isEqualTo(event2);
        }

        @Test
        @DisplayName("Should not equal event with different alert ID")
        void shouldNotEqualEventWithDifferentAlertId() {
            // Given
            FraudAlertEvent event1 = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85
            );
            FraudAlertEvent event2 = FraudAlertEvent.of(
                    UUID.randomUUID(), event1.transferId(), event1.fromAccountId(), event1.toAccountId(),
                    event1.alertType(), event1.reason(), event1.riskScore()
            );

            // Then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("Should have correct hash code")
        void shouldHaveCorrectHashCode() {
            // Given
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "TYPE", "Reason", 85
            );

            // Then
            assertThat(event.hashCode()).isNotNull();
        }

        @Test
        @DisplayName("Should have correct toString")
        void shouldHaveCorrectToString() {
            // Given
            FraudAlertEvent event = FraudAlertEvent.of(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    "VELOCITY_CHECK", "Reason", 85
            );

            // Then
            assertThat(event.toString()).contains("FraudAlertEvent");
            assertThat(event.toString()).contains("VELOCITY_CHECK");
        }
    }
}
