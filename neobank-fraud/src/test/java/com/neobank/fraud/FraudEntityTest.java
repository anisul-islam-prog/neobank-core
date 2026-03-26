package com.neobank.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FraudEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("FraudEntity Unit Tests")
class FraudEntityTest {

    private FraudEntity fraudEntity;

    @BeforeEach
    void setUp() {
        fraudEntity = new FraudEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            FraudEntity entity = new FraudEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            fraudEntity.setId(id);

            // Then
            assertThat(fraudEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get transfer ID")
        void shouldSetAndGetTransferId() {
            // Given
            UUID transferId = UUID.randomUUID();

            // When
            fraudEntity.setTransferId(transferId);

            // Then
            assertThat(fraudEntity.getTransferId()).isEqualTo(transferId);
        }

        @Test
        @DisplayName("Should set and get from account ID")
        void shouldSetAndGetFromAccountId() {
            // Given
            UUID fromAccountId = UUID.randomUUID();

            // When
            fraudEntity.setFromAccountId(fromAccountId);

            // Then
            assertThat(fraudEntity.getFromAccountId()).isEqualTo(fromAccountId);
        }

        @Test
        @DisplayName("Should set and get to account ID")
        void shouldSetAndGetToAccountId() {
            // Given
            UUID toAccountId = UUID.randomUUID();

            // When
            fraudEntity.setToAccountId(toAccountId);

            // Then
            assertThat(fraudEntity.getToAccountId()).isEqualTo(toAccountId);
        }

        @Test
        @DisplayName("Should set and get alert type")
        void shouldSetAndGetAlertType() {
            // Given
            String alertType = "VELOCITY_CHECK";

            // When
            fraudEntity.setAlertType(alertType);

            // Then
            assertThat(fraudEntity.getAlertType()).isEqualTo(alertType);
        }

        @Test
        @DisplayName("Should set and get reason")
        void shouldSetAndGetReason() {
            // Given
            String reason = "Velocity threshold exceeded";

            // When
            fraudEntity.setReason(reason);

            // Then
            assertThat(fraudEntity.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should set and get risk score")
        void shouldSetAndGetRiskScore() {
            // Given
            int riskScore = 85;

            // When
            fraudEntity.setRiskScore(riskScore);

            // Then
            assertThat(fraudEntity.getRiskScore()).isEqualTo(riskScore);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            FraudEntity.FraudStatus status = FraudEntity.FraudStatus.PENDING;

            // When
            fraudEntity.setStatus(status);

            // Then
            assertThat(fraudEntity.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            fraudEntity.setCreatedAt(createdAt);

            // Then
            assertThat(fraudEntity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get reviewed at timestamp")
        void shouldSetAndGetReviewedAtTimestamp() {
            // Given
            Instant reviewedAt = Instant.now();

            // When
            fraudEntity.setReviewedAt(reviewedAt);

            // Then
            assertThat(fraudEntity.getReviewedAt()).isEqualTo(reviewedAt);
        }

        @Test
        @DisplayName("Should set and get reviewed by")
        void shouldSetAndGetReviewedBy() {
            // Given
            UUID reviewedBy = UUID.randomUUID();

            // When
            fraudEntity.setReviewedBy(reviewedBy);

            // Then
            assertThat(fraudEntity.getReviewedBy()).isEqualTo(reviewedBy);
        }

        @Test
        @DisplayName("Should set and get review notes")
        void shouldSetAndGetReviewNotes() {
            // Given
            String reviewNotes = "Confirmed as fraudulent activity";

            // When
            fraudEntity.setReviewNotes(reviewNotes);

            // Then
            assertThat(fraudEntity.getReviewNotes()).isEqualTo(reviewNotes);
        }

        @Test
        @DisplayName("Should set and get metadata JSON")
        void shouldSetAndGetMetadataJson() {
            // Given
            String metadataJson = "{\"pattern\":\"round_amount\"}";

            // When
            fraudEntity.setMetadataJson(metadataJson);

            // Then
            assertThat(fraudEntity.getMetadataJson()).isEqualTo(metadataJson);
        }
    }

    @Nested
    @DisplayName("FraudStatus Enum")
    class FraudStatusEnumTests {

        @Test
        @DisplayName("Should have PENDING status")
        void shouldHavePendingStatus() {
            // Then
            assertThat(FraudEntity.FraudStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("Should have INVESTIGATING status")
        void shouldHaveInvestigatingStatus() {
            // Then
            assertThat(FraudEntity.FraudStatus.INVESTIGATING).isNotNull();
        }

        @Test
        @DisplayName("Should have CONFIRMED_FRAUD status")
        void shouldHaveConfirmedFraudStatus() {
            // Then
            assertThat(FraudEntity.FraudStatus.CONFIRMED_FRAUD).isNotNull();
        }

        @Test
        @DisplayName("Should have FALSE_POSITIVE status")
        void shouldHaveFalsePositiveStatus() {
            // Then
            assertThat(FraudEntity.FraudStatus.FALSE_POSITIVE).isNotNull();
        }

        @Test
        @DisplayName("Should have DISMISSED status")
        void shouldHaveDismissedStatus() {
            // Then
            assertThat(FraudEntity.FraudStatus.DISMISSED).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 5 status values")
        void shouldHaveExactly5StatusValues() {
            // Then
            assertThat(FraudEntity.FraudStatus.values()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionsTests {

        @Test
        @DisplayName("Should transition from PENDING to INVESTIGATING")
        void shouldTransitionFromPendingToInvestigating() {
            // Given
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);

            // When
            fraudEntity.setStatus(FraudEntity.FraudStatus.INVESTIGATING);

            // Then
            assertThat(fraudEntity.getStatus()).isEqualTo(FraudEntity.FraudStatus.INVESTIGATING);
        }

        @Test
        @DisplayName("Should transition from INVESTIGATING to CONFIRMED_FRAUD")
        void shouldTransitionFromInvestigatingToConfirmedFraud() {
            // Given
            fraudEntity.setStatus(FraudEntity.FraudStatus.INVESTIGATING);

            // When
            fraudEntity.setStatus(FraudEntity.FraudStatus.CONFIRMED_FRAUD);

            // Then
            assertThat(fraudEntity.getStatus()).isEqualTo(FraudEntity.FraudStatus.CONFIRMED_FRAUD);
        }

        @Test
        @DisplayName("Should transition from INVESTIGATING to FALSE_POSITIVE")
        void shouldTransitionFromInvestigatingToFalsePositive() {
            // Given
            fraudEntity.setStatus(FraudEntity.FraudStatus.INVESTIGATING);

            // When
            fraudEntity.setStatus(FraudEntity.FraudStatus.FALSE_POSITIVE);

            // Then
            assertThat(fraudEntity.getStatus()).isEqualTo(FraudEntity.FraudStatus.FALSE_POSITIVE);
        }

        @Test
        @DisplayName("Should transition from PENDING to DISMISSED")
        void shouldTransitionFromPendingToDismissed() {
            // Given
            fraudEntity.setStatus(FraudEntity.FraudStatus.PENDING);

            // When
            fraudEntity.setStatus(FraudEntity.FraudStatus.DISMISSED);

            // Then
            assertThat(fraudEntity.getStatus()).isEqualTo(FraudEntity.FraudStatus.DISMISSED);
        }
    }

    @Nested
    @DisplayName("Risk Score Validation")
    class RiskScoreValidationTests {

        @Test
        @DisplayName("Should handle risk score of 0")
        void shouldHandleRiskScoreOf0() {
            // When
            fraudEntity.setRiskScore(0);

            // Then
            assertThat(fraudEntity.getRiskScore()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle risk score of 50")
        void shouldHandleRiskScoreOf50() {
            // When
            fraudEntity.setRiskScore(50);

            // Then
            assertThat(fraudEntity.getRiskScore()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should handle risk score of 100")
        void shouldHandleRiskScoreOf100() {
            // When
            fraudEntity.setRiskScore(100);

            // Then
            assertThat(fraudEntity.getRiskScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle negative risk score")
        void shouldHandleNegativeRiskScore() {
            // When
            fraudEntity.setRiskScore(-10);

            // Then
            assertThat(fraudEntity.getRiskScore()).isEqualTo(-10);
        }

        @Test
        @DisplayName("Should handle risk score above 100")
        void shouldHandleRiskScoreAbove100() {
            // When
            fraudEntity.setRiskScore(150);

            // Then
            assertThat(fraudEntity.getRiskScore()).isEqualTo(150);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            fraudEntity.setId(null);

            // Then
            assertThat(fraudEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null transfer ID")
        void shouldHandleNullTransferId() {
            // When
            fraudEntity.setTransferId(null);

            // Then
            assertThat(fraudEntity.getTransferId()).isNull();
        }

        @Test
        @DisplayName("Should handle null from account ID")
        void shouldHandleNullFromAccountId() {
            // When
            fraudEntity.setFromAccountId(null);

            // Then
            assertThat(fraudEntity.getFromAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null to account ID")
        void shouldHandleNullToAccountId() {
            // When
            fraudEntity.setToAccountId(null);

            // Then
            assertThat(fraudEntity.getToAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null alert type")
        void shouldHandleNullAlertType() {
            // When
            fraudEntity.setAlertType(null);

            // Then
            assertThat(fraudEntity.getAlertType()).isNull();
        }

        @Test
        @DisplayName("Should handle null reason")
        void shouldHandleNullReason() {
            // When
            fraudEntity.setReason(null);

            // Then
            assertThat(fraudEntity.getReason()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            fraudEntity.setStatus(null);

            // Then
            assertThat(fraudEntity.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            fraudEntity.setCreatedAt(null);

            // Then
            assertThat(fraudEntity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null reviewed at timestamp")
        void shouldHandleNullReviewedAtTimestamp() {
            // When
            fraudEntity.setReviewedAt(null);

            // Then
            assertThat(fraudEntity.getReviewedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null reviewed by")
        void shouldHandleNullReviewedBy() {
            // When
            fraudEntity.setReviewedBy(null);

            // Then
            assertThat(fraudEntity.getReviewedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle null review notes")
        void shouldHandleNullReviewNotes() {
            // When
            fraudEntity.setReviewNotes(null);

            // Then
            assertThat(fraudEntity.getReviewNotes()).isNull();
        }

        @Test
        @DisplayName("Should handle null metadata JSON")
        void shouldHandleNullMetadataJson() {
            // When
            fraudEntity.setMetadataJson(null);

            // Then
            assertThat(fraudEntity.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // When
            fraudEntity.setMetadataJson("{}");

            // Then
            assertThat(fraudEntity.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long reason string")
        void shouldHandleLongReasonString() {
            // Given
            String longReason = "a".repeat(900);

            // When
            fraudEntity.setReason(longReason);

            // Then
            assertThat(fraudEntity.getReason()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle long review notes string")
        void shouldHandleLongReviewNotesString() {
            // Given
            String longNotes = "b".repeat(900);

            // When
            fraudEntity.setReviewNotes(longNotes);

            // Then
            assertThat(fraudEntity.getReviewNotes()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle alert type with special characters")
        void shouldHandleAlertTypeWithSpecialCharacters() {
            // Given
            String alertType = "VELOCITY_CHECK_V2";

            // When
            fraudEntity.setAlertType(alertType);

            // Then
            assertThat(fraudEntity.getAlertType()).isEqualTo(alertType);
        }

        @Test
        @DisplayName("Should handle reason with special characters")
        void shouldHandleReasonWithSpecialCharacters() {
            // Given
            String reason = "Velocity check: 5 transactions in 60 seconds!";

            // When
            fraudEntity.setReason(reason);

            // Then
            assertThat(fraudEntity.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle reason with unicode characters")
        void shouldHandleReasonWithUnicodeCharacters() {
            // Given
            String reason = "欺诈检测：可疑活动";

            // When
            fraudEntity.setReason(reason);

            // Then
            assertThat(fraudEntity.getReason()).isEqualTo(reason);
        }
    }

    @Nested
    @DisplayName("Complete Entity State")
    class CompleteEntityStateTests {

        @Test
        @DisplayName("Should handle complete entity with all fields set")
        void shouldHandleCompleteEntityWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID transferId = UUID.randomUUID();
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            String alertType = "VELOCITY_CHECK";
            String reason = "Velocity threshold exceeded";
            int riskScore = 85;
            FraudEntity.FraudStatus status = FraudEntity.FraudStatus.PENDING;
            Instant createdAt = Instant.now();
            Instant reviewedAt = Instant.now();
            UUID reviewedBy = UUID.randomUUID();
            String reviewNotes = "Under investigation";
            String metadataJson = "{\"pattern\":\"velocity\"}";

            // When
            fraudEntity.setId(id);
            fraudEntity.setTransferId(transferId);
            fraudEntity.setFromAccountId(fromAccountId);
            fraudEntity.setToAccountId(toAccountId);
            fraudEntity.setAlertType(alertType);
            fraudEntity.setReason(reason);
            fraudEntity.setRiskScore(riskScore);
            fraudEntity.setStatus(status);
            fraudEntity.setCreatedAt(createdAt);
            fraudEntity.setReviewedAt(reviewedAt);
            fraudEntity.setReviewedBy(reviewedBy);
            fraudEntity.setReviewNotes(reviewNotes);
            fraudEntity.setMetadataJson(metadataJson);

            // Then
            assertThat(fraudEntity.getId()).isEqualTo(id);
            assertThat(fraudEntity.getTransferId()).isEqualTo(transferId);
            assertThat(fraudEntity.getFromAccountId()).isEqualTo(fromAccountId);
            assertThat(fraudEntity.getToAccountId()).isEqualTo(toAccountId);
            assertThat(fraudEntity.getAlertType()).isEqualTo(alertType);
            assertThat(fraudEntity.getReason()).isEqualTo(reason);
            assertThat(fraudEntity.getRiskScore()).isEqualTo(riskScore);
            assertThat(fraudEntity.getStatus()).isEqualTo(status);
            assertThat(fraudEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(fraudEntity.getReviewedAt()).isEqualTo(reviewedAt);
            assertThat(fraudEntity.getReviewedBy()).isEqualTo(reviewedBy);
            assertThat(fraudEntity.getReviewNotes()).isEqualTo(reviewNotes);
            assertThat(fraudEntity.getMetadataJson()).isEqualTo(metadataJson);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID transferId = UUID.randomUUID();

            // When
            fraudEntity.setId(id);
            fraudEntity.setTransferId(transferId);

            // Then
            assertThat(fraudEntity.getId()).isEqualTo(id);
            assertThat(fraudEntity.getTransferId()).isEqualTo(transferId);
            assertThat(fraudEntity.getStatus()).isNull();
            assertThat(fraudEntity.getRiskScore()).isEqualTo(0);
        }
    }
}
