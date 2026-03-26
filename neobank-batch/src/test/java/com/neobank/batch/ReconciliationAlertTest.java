package com.neobank.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ReconciliationAlert entity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("ReconciliationAlert Unit Tests")
class ReconciliationAlertTest {

    private ReconciliationAlert alert;

    @BeforeEach
    void setUp() {
        alert = new ReconciliationAlert();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            ReconciliationAlert entity = new ReconciliationAlert();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            alert.setId(id);

            // Then
            assertThat(alert.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get alert date")
        void shouldSetAndGetAlertDate() {
            // Given
            Instant alertDate = Instant.now();

            // When
            alert.setAlertDate(alertDate);

            // Then
            assertThat(alert.getAlertDate()).isEqualTo(alertDate);
        }

        @Test
        @DisplayName("Should set and get expected balance")
        void shouldSetAndGetExpectedBalance() {
            // Given
            BigDecimal expectedBalance = new BigDecimal("1000.00");

            // When
            alert.setExpectedBalance(expectedBalance);

            // Then
            assertThat(alert.getExpectedBalance()).isEqualByComparingTo(expectedBalance);
        }

        @Test
        @DisplayName("Should set and get actual balance")
        void shouldSetAndGetActualBalance() {
            // Given
            BigDecimal actualBalance = new BigDecimal("900.00");

            // When
            alert.setActualBalance(actualBalance);

            // Then
            assertThat(alert.getActualBalance()).isEqualByComparingTo(actualBalance);
        }

        @Test
        @DisplayName("Should set and get difference")
        void shouldSetAndGetDifference() {
            // Given
            BigDecimal difference = new BigDecimal("100.00");

            // When
            alert.setDifference(difference);

            // Then
            assertThat(alert.getDifference()).isEqualByComparingTo(difference);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            ReconciliationAlert.AlertStatus status = ReconciliationAlert.AlertStatus.PENDING;

            // When
            alert.setStatus(status);

            // Then
            assertThat(alert.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get details")
        void shouldSetAndGetDetails() {
            // Given
            String details = "Balance mismatch detected";

            // When
            alert.setDetails(details);

            // Then
            assertThat(alert.getDetails()).isEqualTo(details);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            alert.setCreatedAt(createdAt);

            // Then
            assertThat(alert.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get resolved at timestamp")
        void shouldSetAndGetResolvedAtTimestamp() {
            // Given
            Instant resolvedAt = Instant.now();

            // When
            alert.setResolvedAt(resolvedAt);

            // Then
            assertThat(alert.getResolvedAt()).isEqualTo(resolvedAt);
        }

        @Test
        @DisplayName("Should set and get resolved by")
        void shouldSetAndGetResolvedBy() {
            // Given
            String resolvedBy = "admin_user";

            // When
            alert.setResolvedBy(resolvedBy);

            // Then
            assertThat(alert.getResolvedBy()).isEqualTo(resolvedBy);
        }
    }

    @Nested
    @DisplayName("AlertStatus Enum")
    class AlertStatusEnumTests {

        @Test
        @DisplayName("Should have PENDING status")
        void shouldHavePendingStatus() {
            // Then
            assertThat(ReconciliationAlert.AlertStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("Should have INVESTIGATING status")
        void shouldHaveInvestigatingStatus() {
            // Then
            assertThat(ReconciliationAlert.AlertStatus.INVESTIGATING).isNotNull();
        }

        @Test
        @DisplayName("Should have RESOLVED status")
        void shouldHaveResolvedStatus() {
            // Then
            assertThat(ReconciliationAlert.AlertStatus.RESOLVED).isNotNull();
        }

        @Test
        @DisplayName("Should have FALSE_POSITIVE status")
        void shouldHaveFalsePositiveStatus() {
            // Then
            assertThat(ReconciliationAlert.AlertStatus.FALSE_POSITIVE).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 4 status values")
        void shouldHaveExactly4StatusValues() {
            // Then
            assertThat(ReconciliationAlert.AlertStatus.values()).hasSize(4);
        }

        @Test
        @DisplayName("Should allow valueOf for all status names")
        void shouldAllowValueOfForAllStatusNames() {
            // Then
            assertThat(ReconciliationAlert.AlertStatus.valueOf("PENDING"))
                    .isEqualTo(ReconciliationAlert.AlertStatus.PENDING);
            assertThat(ReconciliationAlert.AlertStatus.valueOf("INVESTIGATING"))
                    .isEqualTo(ReconciliationAlert.AlertStatus.INVESTIGATING);
            assertThat(ReconciliationAlert.AlertStatus.valueOf("RESOLVED"))
                    .isEqualTo(ReconciliationAlert.AlertStatus.RESOLVED);
            assertThat(ReconciliationAlert.AlertStatus.valueOf("FALSE_POSITIVE"))
                    .isEqualTo(ReconciliationAlert.AlertStatus.FALSE_POSITIVE);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionsTests {

        @Test
        @DisplayName("Should transition from PENDING to INVESTIGATING")
        void shouldTransitionFromPendingToInvestigating() {
            // Given
            alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);

            // When
            alert.setStatus(ReconciliationAlert.AlertStatus.INVESTIGATING);

            // Then
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.INVESTIGATING);
        }

        @Test
        @DisplayName("Should transition from INVESTIGATING to RESOLVED")
        void shouldTransitionFromInvestigatingToResolved() {
            // Given
            alert.setStatus(ReconciliationAlert.AlertStatus.INVESTIGATING);

            // When
            alert.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);

            // Then
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.RESOLVED);
        }

        @Test
        @DisplayName("Should transition from INVESTIGATING to FALSE_POSITIVE")
        void shouldTransitionFromInvestigatingToFalsePositive() {
            // Given
            alert.setStatus(ReconciliationAlert.AlertStatus.INVESTIGATING);

            // When
            alert.setStatus(ReconciliationAlert.AlertStatus.FALSE_POSITIVE);

            // Then
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.FALSE_POSITIVE);
        }

        @Test
        @DisplayName("Should transition from PENDING to RESOLVED")
        void shouldTransitionFromPendingToResolved() {
            // Given
            alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);

            // When
            alert.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);

            // Then
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.RESOLVED);
        }

        @Test
        @DisplayName("Should allow multiple status transitions")
        void shouldAllowMultipleStatusTransitions() {
            // Given
            alert.setStatus(ReconciliationAlert.AlertStatus.PENDING);

            // When/Then
            alert.setStatus(ReconciliationAlert.AlertStatus.INVESTIGATING);
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.INVESTIGATING);

            alert.setStatus(ReconciliationAlert.AlertStatus.RESOLVED);
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.RESOLVED);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            alert.setId(null);

            // Then
            assertThat(alert.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null alert date")
        void shouldHandleNullAlertDate() {
            // When
            alert.setAlertDate(null);

            // Then
            assertThat(alert.getAlertDate()).isNull();
        }

        @Test
        @DisplayName("Should handle null expected balance")
        void shouldHandleNullExpectedBalance() {
            // When
            alert.setExpectedBalance(null);

            // Then
            assertThat(alert.getExpectedBalance()).isNull();
        }

        @Test
        @DisplayName("Should handle null actual balance")
        void shouldHandleNullActualBalance() {
            // When
            alert.setActualBalance(null);

            // Then
            assertThat(alert.getActualBalance()).isNull();
        }

        @Test
        @DisplayName("Should handle null difference")
        void shouldHandleNullDifference() {
            // When
            alert.setDifference(null);

            // Then
            assertThat(alert.getDifference()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            alert.setStatus(null);

            // Then
            assertThat(alert.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null details")
        void shouldHandleNullDetails() {
            // When
            alert.setDetails(null);

            // Then
            assertThat(alert.getDetails()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            alert.setCreatedAt(null);

            // Then
            assertThat(alert.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null resolved at timestamp")
        void shouldHandleNullResolvedAtTimestamp() {
            // When
            alert.setResolvedAt(null);

            // Then
            assertThat(alert.getResolvedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null resolved by")
        void shouldHandleNullResolvedBy() {
            // When
            alert.setResolvedBy(null);

            // Then
            assertThat(alert.getResolvedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle empty details")
        void shouldHandleEmptyDetails() {
            // When
            alert.setDetails("");

            // Then
            assertThat(alert.getDetails()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long details string")
        void shouldHandleLongDetailsString() {
            // Given
            String longDetails = "a".repeat(900);

            // When
            alert.setDetails(longDetails);

            // Then
            assertThat(alert.getDetails()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle zero expected balance")
        void shouldHandleZeroExpectedBalance() {
            // When
            alert.setExpectedBalance(BigDecimal.ZERO);

            // Then
            assertThat(alert.getExpectedBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero actual balance")
        void shouldHandleZeroActualBalance() {
            // When
            alert.setActualBalance(BigDecimal.ZERO);

            // Then
            assertThat(alert.getActualBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero difference")
        void shouldHandleZeroDifference() {
            // When
            alert.setDifference(BigDecimal.ZERO);

            // Then
            assertThat(alert.getDifference()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative difference")
        void shouldHandleNegativeDifference() {
            // When
            alert.setDifference(new BigDecimal("-100.00"));

            // Then
            assertThat(alert.getDifference()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle very large expected balance")
        void shouldHandleVeryLargeExpectedBalance() {
            // When
            alert.setExpectedBalance(new BigDecimal("10000000.00"));

            // Then
            assertThat(alert.getExpectedBalance()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle balance with 4 decimal places")
        void shouldHandleBalanceWith4DecimalPlaces() {
            // When
            alert.setExpectedBalance(new BigDecimal("123.4567"));

            // Then
            assertThat(alert.getExpectedBalance()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle details with special characters")
        void shouldHandleDetailsWithSpecialCharacters() {
            // Given
            String details = "Balance mismatch: $100.00 difference!";

            // When
            alert.setDetails(details);

            // Then
            assertThat(alert.getDetails()).isEqualTo(details);
        }

        @Test
        @DisplayName("Should handle details with unicode characters")
        void shouldHandleDetailsWithUnicodeCharacters() {
            // Given
            String details = "余额不匹配：差异 $100.00";

            // When
            alert.setDetails(details);

            // Then
            assertThat(alert.getDetails()).isEqualTo(details);
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
            Instant alertDate = Instant.now();
            BigDecimal expectedBalance = new BigDecimal("1000.00");
            BigDecimal actualBalance = new BigDecimal("900.00");
            BigDecimal difference = new BigDecimal("100.00");
            ReconciliationAlert.AlertStatus status = ReconciliationAlert.AlertStatus.PENDING;
            String details = "Balance mismatch detected";
            Instant createdAt = Instant.now();
            Instant resolvedAt = Instant.now();
            String resolvedBy = "admin_user";

            // When
            alert.setId(id);
            alert.setAlertDate(alertDate);
            alert.setExpectedBalance(expectedBalance);
            alert.setActualBalance(actualBalance);
            alert.setDifference(difference);
            alert.setStatus(status);
            alert.setDetails(details);
            alert.setCreatedAt(createdAt);
            alert.setResolvedAt(resolvedAt);
            alert.setResolvedBy(resolvedBy);

            // Then
            assertThat(alert.getId()).isEqualTo(id);
            assertThat(alert.getAlertDate()).isEqualTo(alertDate);
            assertThat(alert.getExpectedBalance()).isEqualByComparingTo(expectedBalance);
            assertThat(alert.getActualBalance()).isEqualByComparingTo(actualBalance);
            assertThat(alert.getDifference()).isEqualByComparingTo(difference);
            assertThat(alert.getStatus()).isEqualTo(status);
            assertThat(alert.getDetails()).isEqualTo(details);
            assertThat(alert.getCreatedAt()).isEqualTo(createdAt);
            assertThat(alert.getResolvedAt()).isEqualTo(resolvedAt);
            assertThat(alert.getResolvedBy()).isEqualTo(resolvedBy);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            Instant alertDate = Instant.now();
            BigDecimal expectedBalance = new BigDecimal("1000.00");
            BigDecimal actualBalance = new BigDecimal("900.00");
            BigDecimal difference = new BigDecimal("100.00");
            ReconciliationAlert.AlertStatus status = ReconciliationAlert.AlertStatus.PENDING;
            Instant createdAt = Instant.now();

            // When
            alert.setId(id);
            alert.setAlertDate(alertDate);
            alert.setExpectedBalance(expectedBalance);
            alert.setActualBalance(actualBalance);
            alert.setDifference(difference);
            alert.setStatus(status);
            alert.setCreatedAt(createdAt);

            // Then
            assertThat(alert.getId()).isEqualTo(id);
            assertThat(alert.getAlertDate()).isEqualTo(alertDate);
            assertThat(alert.getExpectedBalance()).isEqualByComparingTo(expectedBalance);
            assertThat(alert.getActualBalance()).isEqualByComparingTo(actualBalance);
            assertThat(alert.getDifference()).isEqualByComparingTo(difference);
            assertThat(alert.getStatus()).isEqualTo(status);
            assertThat(alert.getCreatedAt()).isEqualTo(createdAt);
            assertThat(alert.getDetails()).isNull();
            assertThat(alert.getResolvedAt()).isNull();
            assertThat(alert.getResolvedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle entity with resolved status")
        void shouldHandleEntityWithResolvedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            Instant alertDate = Instant.now();
            BigDecimal expectedBalance = new BigDecimal("1000.00");
            BigDecimal actualBalance = new BigDecimal("900.00");
            BigDecimal difference = new BigDecimal("100.00");
            ReconciliationAlert.AlertStatus status = ReconciliationAlert.AlertStatus.RESOLVED;
            String details = "Investigated and resolved";
            Instant createdAt = Instant.now();
            Instant resolvedAt = Instant.now();
            String resolvedBy = "admin_user";

            // When
            alert.setId(id);
            alert.setAlertDate(alertDate);
            alert.setExpectedBalance(expectedBalance);
            alert.setActualBalance(actualBalance);
            alert.setDifference(difference);
            alert.setStatus(status);
            alert.setDetails(details);
            alert.setCreatedAt(createdAt);
            alert.setResolvedAt(resolvedAt);
            alert.setResolvedBy(resolvedBy);

            // Then
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.RESOLVED);
            assertThat(alert.getResolvedAt()).isNotNull();
            assertThat(alert.getResolvedBy()).isEqualTo("admin_user");
        }

        @Test
        @DisplayName("Should handle entity with false positive status")
        void shouldHandleEntityWithFalsePositiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            Instant alertDate = Instant.now();
            BigDecimal expectedBalance = new BigDecimal("1000.00");
            BigDecimal actualBalance = new BigDecimal("900.00");
            BigDecimal difference = new BigDecimal("100.00");
            ReconciliationAlert.AlertStatus status = ReconciliationAlert.AlertStatus.FALSE_POSITIVE;
            String details = "System error caused false alert";
            Instant createdAt = Instant.now();
            Instant resolvedAt = Instant.now();
            String resolvedBy = "system_admin";

            // When
            alert.setId(id);
            alert.setAlertDate(alertDate);
            alert.setExpectedBalance(expectedBalance);
            alert.setActualBalance(actualBalance);
            alert.setDifference(difference);
            alert.setStatus(status);
            alert.setDetails(details);
            alert.setCreatedAt(createdAt);
            alert.setResolvedAt(resolvedAt);
            alert.setResolvedBy(resolvedBy);

            // Then
            assertThat(alert.getStatus()).isEqualTo(ReconciliationAlert.AlertStatus.FALSE_POSITIVE);
            assertThat(alert.getResolvedAt()).isNotNull();
            assertThat(alert.getResolvedBy()).isEqualTo("system_admin");
        }
    }
}
