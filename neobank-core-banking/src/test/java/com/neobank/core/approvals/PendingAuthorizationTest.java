package com.neobank.core.approvals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PendingAuthorization entity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("PendingAuthorization Unit Tests")
class PendingAuthorizationTest {

    private PendingAuthorization authorization;

    @BeforeEach
    void setUp() {
        authorization = new PendingAuthorization();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            PendingAuthorization entity = new PendingAuthorization();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            authorization.setId(id);

            // Then
            assertThat(authorization.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get action type")
        void shouldSetAndGetActionType() {
            // Given
            PendingAuthorization.ActionType actionType = PendingAuthorization.ActionType.HIGH_VALUE_TRANSFER;

            // When
            authorization.setActionType(actionType);

            // Then
            assertThat(authorization.getActionType()).isEqualTo(actionType);
        }

        @Test
        @DisplayName("Should set and get initiator ID")
        void shouldSetAndGetInitiatorId() {
            // Given
            UUID initiatorId = UUID.randomUUID();

            // When
            authorization.setInitiatorId(initiatorId);

            // Then
            assertThat(authorization.getInitiatorId()).isEqualTo(initiatorId);
        }

        @Test
        @DisplayName("Should set and get initiator role")
        void shouldSetAndGetInitiatorRole() {
            // Given
            String initiatorRole = "TELLER";

            // When
            authorization.setInitiatorRole(initiatorRole);

            // Then
            assertThat(authorization.getInitiatorRole()).isEqualTo(initiatorRole);
        }

        @Test
        @DisplayName("Should set and get target ID")
        void shouldSetAndGetTargetId() {
            // Given
            UUID targetId = UUID.randomUUID();

            // When
            authorization.setTargetId(targetId);

            // Then
            assertThat(authorization.getTargetId()).isEqualTo(targetId);
        }

        @Test
        @DisplayName("Should set and get amount")
        void shouldSetAndGetAmount() {
            // Given
            BigDecimal amount = new BigDecimal("10000.00");

            // When
            authorization.setAmount(amount);

            // Then
            assertThat(authorization.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should set and get currency")
        void shouldSetAndGetCurrency() {
            // Given
            String currency = "USD";

            // When
            authorization.setCurrency(currency);

            // Then
            assertThat(authorization.getCurrency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should set and get reason")
        void shouldSetAndGetReason() {
            // Given
            String reason = "High-value transfer requiring approval";

            // When
            authorization.setReason(reason);

            // Then
            assertThat(authorization.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            PendingAuthorization.AuthorizationStatus status = PendingAuthorization.AuthorizationStatus.PENDING;

            // When
            authorization.setStatus(status);

            // Then
            assertThat(authorization.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            authorization.setCreatedAt(createdAt);

            // Then
            assertThat(authorization.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get reviewer ID")
        void shouldSetAndGetReviewerId() {
            // Given
            UUID reviewerId = UUID.randomUUID();

            // When
            authorization.setReviewerId(reviewerId);

            // Then
            assertThat(authorization.getReviewerId()).isEqualTo(reviewerId);
        }

        @Test
        @DisplayName("Should set and get reviewer role")
        void shouldSetAndGetReviewerRole() {
            // Given
            String reviewerRole = "MANAGER";

            // When
            authorization.setReviewerRole(reviewerRole);

            // Then
            assertThat(authorization.getReviewerRole()).isEqualTo(reviewerRole);
        }

        @Test
        @DisplayName("Should set and get reviewed at timestamp")
        void shouldSetAndGetReviewedAtTimestamp() {
            // Given
            Instant reviewedAt = Instant.now();

            // When
            authorization.setReviewedAt(reviewedAt);

            // Then
            assertThat(authorization.getReviewedAt()).isEqualTo(reviewedAt);
        }

        @Test
        @DisplayName("Should set and get review notes")
        void shouldSetAndGetReviewNotes() {
            // Given
            String reviewNotes = "Approved after verification";

            // When
            authorization.setReviewNotes(reviewNotes);

            // Then
            assertThat(authorization.getReviewNotes()).isEqualTo(reviewNotes);
        }

        @Test
        @DisplayName("Should set and get metadata")
        void shouldSetAndGetMetadata() {
            // Given
            String metadata = "{\"key\":\"value\"}";

            // When
            authorization.setMetadata(metadata);

            // Then
            assertThat(authorization.getMetadata()).isEqualTo(metadata);
        }
    }

    @Nested
    @DisplayName("ActionType Enum")
    class ActionTypeEnumTests {

        @Test
        @DisplayName("Should have HIGH_VALUE_TRANSFER action type")
        void shouldHaveHighValueTransferActionType() {
            // Then
            assertThat(PendingAuthorization.ActionType.HIGH_VALUE_TRANSFER).isNotNull();
        }

        @Test
        @DisplayName("Should have ACCOUNT_DELETION action type")
        void shouldHaveAccountDeletionActionType() {
            // Then
            assertThat(PendingAuthorization.ActionType.ACCOUNT_DELETION).isNotNull();
        }

        @Test
        @DisplayName("Should have LIMIT_INCREASE action type")
        void shouldHaveLimitIncreaseActionType() {
            // Then
            assertThat(PendingAuthorization.ActionType.LIMIT_INCREASE).isNotNull();
        }

        @Test
        @DisplayName("Should have USER_SUSPENSION action type")
        void shouldHaveUserSuspensionActionType() {
            // Then
            assertThat(PendingAuthorization.ActionType.USER_SUSPENSION).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 4 action types")
        void shouldHaveExactly4ActionTypes() {
            // Then
            assertThat(PendingAuthorization.ActionType.values()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("AuthorizationStatus Enum")
    class AuthorizationStatusEnumTests {

        @Test
        @DisplayName("Should have PENDING status")
        void shouldHavePendingStatus() {
            // Then
            assertThat(PendingAuthorization.AuthorizationStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("Should have APPROVED status")
        void shouldHaveApprovedStatus() {
            // Then
            assertThat(PendingAuthorization.AuthorizationStatus.APPROVED).isNotNull();
        }

        @Test
        @DisplayName("Should have REJECTED status")
        void shouldHaveRejectedStatus() {
            // Then
            assertThat(PendingAuthorization.AuthorizationStatus.REJECTED).isNotNull();
        }

        @Test
        @DisplayName("Should have EXPIRED status")
        void shouldHaveExpiredStatus() {
            // Then
            assertThat(PendingAuthorization.AuthorizationStatus.EXPIRED).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 4 status values")
        void shouldHaveExactly4StatusValues() {
            // Then
            assertThat(PendingAuthorization.AuthorizationStatus.values()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionsTests {

        @Test
        @DisplayName("Should transition from PENDING to APPROVED")
        void shouldTransitionFromPendingToApproved() {
            // Given
            authorization.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            // When
            authorization.setStatus(PendingAuthorization.AuthorizationStatus.APPROVED);

            // Then
            assertThat(authorization.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.APPROVED);
        }

        @Test
        @DisplayName("Should transition from PENDING to REJECTED")
        void shouldTransitionFromPendingToRejected() {
            // Given
            authorization.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            // When
            authorization.setStatus(PendingAuthorization.AuthorizationStatus.REJECTED);

            // Then
            assertThat(authorization.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.REJECTED);
        }

        @Test
        @DisplayName("Should transition from PENDING to EXPIRED")
        void shouldTransitionFromPendingToExpired() {
            // Given
            authorization.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            // When
            authorization.setStatus(PendingAuthorization.AuthorizationStatus.EXPIRED);

            // Then
            assertThat(authorization.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.EXPIRED);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            authorization.setId(null);

            // Then
            assertThat(authorization.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null action type")
        void shouldHandleNullActionType() {
            // When
            authorization.setActionType(null);

            // Then
            assertThat(authorization.getActionType()).isNull();
        }

        @Test
        @DisplayName("Should handle null initiator ID")
        void shouldHandleNullInitiatorId() {
            // When
            authorization.setInitiatorId(null);

            // Then
            assertThat(authorization.getInitiatorId()).isNull();
        }

        @Test
        @DisplayName("Should handle null initiator role")
        void shouldHandleNullInitiatorRole() {
            // When
            authorization.setInitiatorRole(null);

            // Then
            assertThat(authorization.getInitiatorRole()).isNull();
        }

        @Test
        @DisplayName("Should handle null target ID")
        void shouldHandleNullTargetId() {
            // When
            authorization.setTargetId(null);

            // Then
            assertThat(authorization.getTargetId()).isNull();
        }

        @Test
        @DisplayName("Should handle null amount")
        void shouldHandleNullAmount() {
            // When
            authorization.setAmount(null);

            // Then
            assertThat(authorization.getAmount()).isNull();
        }

        @Test
        @DisplayName("Should handle null currency")
        void shouldHandleNullCurrency() {
            // When
            authorization.setCurrency(null);

            // Then
            assertThat(authorization.getCurrency()).isNull();
        }

        @Test
        @DisplayName("Should handle null reason")
        void shouldHandleNullReason() {
            // When
            authorization.setReason(null);

            // Then
            assertThat(authorization.getReason()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            authorization.setStatus(null);

            // Then
            assertThat(authorization.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            authorization.setCreatedAt(null);

            // Then
            assertThat(authorization.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null reviewer ID")
        void shouldHandleNullReviewerId() {
            // When
            authorization.setReviewerId(null);

            // Then
            assertThat(authorization.getReviewerId()).isNull();
        }

        @Test
        @DisplayName("Should handle null reviewer role")
        void shouldHandleNullReviewerRole() {
            // When
            authorization.setReviewerRole(null);

            // Then
            assertThat(authorization.getReviewerRole()).isNull();
        }

        @Test
        @DisplayName("Should handle null reviewed at timestamp")
        void shouldHandleNullReviewedAtTimestamp() {
            // When
            authorization.setReviewedAt(null);

            // Then
            assertThat(authorization.getReviewedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null review notes")
        void shouldHandleNullReviewNotes() {
            // When
            authorization.setReviewNotes(null);

            // Then
            assertThat(authorization.getReviewNotes()).isNull();
        }

        @Test
        @DisplayName("Should handle null metadata")
        void shouldHandleNullMetadata() {
            // When
            authorization.setMetadata(null);

            // Then
            assertThat(authorization.getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata")
        void shouldHandleEmptyMetadata() {
            // When
            authorization.setMetadata("{}");

            // Then
            assertThat(authorization.getMetadata()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long review notes")
        void shouldHandleLongReviewNotes() {
            // Given
            String longNotes = "a".repeat(400);

            // When
            authorization.setReviewNotes(longNotes);

            // Then
            assertThat(authorization.getReviewNotes()).hasSize(400);
        }

        @Test
        @DisplayName("Should handle long reason")
        void shouldHandleLongReason() {
            // Given
            String longReason = "b".repeat(400);

            // When
            authorization.setReason(longReason);

            // Then
            assertThat(authorization.getReason()).hasSize(400);
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            // When
            authorization.setAmount(BigDecimal.ZERO);

            // Then
            assertThat(authorization.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle amount with 4 decimal places")
        void shouldHandleAmountWith4DecimalPlaces() {
            // When
            authorization.setAmount(new BigDecimal("123.4567"));

            // Then
            assertThat(authorization.getAmount()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large amount")
        void shouldHandleVeryLargeAmount() {
            // When
            authorization.setAmount(new BigDecimal("10000000.00"));

            // Then
            assertThat(authorization.getAmount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
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
            PendingAuthorization.ActionType actionType = PendingAuthorization.ActionType.HIGH_VALUE_TRANSFER;
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.00");
            String currency = "USD";
            String reason = "High-value transfer";
            PendingAuthorization.AuthorizationStatus status = PendingAuthorization.AuthorizationStatus.PENDING;
            Instant createdAt = Instant.now();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";
            Instant reviewedAt = Instant.now();
            String reviewNotes = "Approved";
            String metadata = "{\"key\":\"value\"}";

            // When
            authorization.setId(id);
            authorization.setActionType(actionType);
            authorization.setInitiatorId(initiatorId);
            authorization.setInitiatorRole(initiatorRole);
            authorization.setTargetId(targetId);
            authorization.setAmount(amount);
            authorization.setCurrency(currency);
            authorization.setReason(reason);
            authorization.setStatus(status);
            authorization.setCreatedAt(createdAt);
            authorization.setReviewerId(reviewerId);
            authorization.setReviewerRole(reviewerRole);
            authorization.setReviewedAt(reviewedAt);
            authorization.setReviewNotes(reviewNotes);
            authorization.setMetadata(metadata);

            // Then
            assertThat(authorization.getId()).isEqualTo(id);
            assertThat(authorization.getActionType()).isEqualTo(actionType);
            assertThat(authorization.getInitiatorId()).isEqualTo(initiatorId);
            assertThat(authorization.getInitiatorRole()).isEqualTo(initiatorRole);
            assertThat(authorization.getTargetId()).isEqualTo(targetId);
            assertThat(authorization.getAmount()).isEqualByComparingTo(amount);
            assertThat(authorization.getCurrency()).isEqualTo(currency);
            assertThat(authorization.getReason()).isEqualTo(reason);
            assertThat(authorization.getStatus()).isEqualTo(status);
            assertThat(authorization.getCreatedAt()).isEqualTo(createdAt);
            assertThat(authorization.getReviewerId()).isEqualTo(reviewerId);
            assertThat(authorization.getReviewerRole()).isEqualTo(reviewerRole);
            assertThat(authorization.getReviewedAt()).isEqualTo(reviewedAt);
            assertThat(authorization.getReviewNotes()).isEqualTo(reviewNotes);
            assertThat(authorization.getMetadata()).isEqualTo(metadata);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            PendingAuthorization.ActionType actionType = PendingAuthorization.ActionType.HIGH_VALUE_TRANSFER;
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            PendingAuthorization.AuthorizationStatus status = PendingAuthorization.AuthorizationStatus.PENDING;
            Instant createdAt = Instant.now();

            // When
            authorization.setId(id);
            authorization.setActionType(actionType);
            authorization.setInitiatorId(initiatorId);
            authorization.setInitiatorRole(initiatorRole);
            authorization.setStatus(status);
            authorization.setCreatedAt(createdAt);

            // Then
            assertThat(authorization.getId()).isEqualTo(id);
            assertThat(authorization.getActionType()).isEqualTo(actionType);
            assertThat(authorization.getInitiatorId()).isEqualTo(initiatorId);
            assertThat(authorization.getInitiatorRole()).isEqualTo(initiatorRole);
            assertThat(authorization.getStatus()).isEqualTo(status);
            assertThat(authorization.getCreatedAt()).isEqualTo(createdAt);
            assertThat(authorization.getTargetId()).isNull();
            assertThat(authorization.getAmount()).isNull();
            assertThat(authorization.getReviewerId()).isNull();
        }
    }
}
