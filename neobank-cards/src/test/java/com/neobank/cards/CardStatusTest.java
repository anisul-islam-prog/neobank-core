package com.neobank.cards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CardStatus enum using JUnit 5.
 * Tests enum values and behavior.
 */
@DisplayName("CardStatus Unit Tests")
class CardStatusTest {

    @Nested
    @DisplayName("Enum Values")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have ACTIVE status")
        void shouldHaveActiveStatus() {
            // Then
            assertThat(CardStatus.ACTIVE).isNotNull();
        }

        @Test
        @DisplayName("Should have FROZEN status")
        void shouldHaveFrozenStatus() {
            // Then
            assertThat(CardStatus.FROZEN).isNotNull();
        }

        @Test
        @DisplayName("Should have BLOCKED status")
        void shouldHaveBlockedStatus() {
            // Then
            assertThat(CardStatus.BLOCKED).isNotNull();
        }

        @Test
        @DisplayName("Should have REPORTED_STOLEN status")
        void shouldHaveReportedStolenStatus() {
            // Then
            assertThat(CardStatus.REPORTED_STOLEN).isNotNull();
        }

        @Test
        @DisplayName("Should have EXPIRED status")
        void shouldHaveExpiredStatus() {
            // Then
            assertThat(CardStatus.EXPIRED).isNotNull();
        }

        @Test
        @DisplayName("Should have REPLACED status")
        void shouldHaveReplacedStatus() {
            // Then
            assertThat(CardStatus.REPLACED).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 6 status values")
        void shouldHaveExactly6StatusValues() {
            // Then
            assertThat(CardStatus.values()).hasSize(6);
        }

        @Test
        @DisplayName("Should allow valueOf for all status names")
        void shouldAllowValueOfForAllStatusNames() {
            // Then
            assertThat(CardStatus.valueOf("ACTIVE")).isEqualTo(CardStatus.ACTIVE);
            assertThat(CardStatus.valueOf("FROZEN")).isEqualTo(CardStatus.FROZEN);
            assertThat(CardStatus.valueOf("BLOCKED")).isEqualTo(CardStatus.BLOCKED);
            assertThat(CardStatus.valueOf("REPORTED_STOLEN")).isEqualTo(CardStatus.REPORTED_STOLEN);
            assertThat(CardStatus.valueOf("EXPIRED")).isEqualTo(CardStatus.EXPIRED);
            assertThat(CardStatus.valueOf("REPLACED")).isEqualTo(CardStatus.REPLACED);
        }

        @Test
        @DisplayName("Should return status array with correct order")
        void shouldReturnStatusArrayWithCorrectOrder() {
            // When
            CardStatus[] statuses = CardStatus.values();

            // Then
            assertThat(statuses).containsExactly(
                    CardStatus.ACTIVE,
                    CardStatus.FROZEN,
                    CardStatus.BLOCKED,
                    CardStatus.REPORTED_STOLEN,
                    CardStatus.EXPIRED,
                    CardStatus.REPLACED
            );
        }
    }

    @Nested
    @DisplayName("Ordinal Values")
    class OrdinalValuesTests {

        @Test
        @DisplayName("Should have correct ordinal for ACTIVE")
        void shouldHaveCorrectOrdinalForActive() {
            // Then
            assertThat(CardStatus.ACTIVE.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should have correct ordinal for FROZEN")
        void shouldHaveCorrectOrdinalForFrozen() {
            // Then
            assertThat(CardStatus.FROZEN.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should have correct ordinal for BLOCKED")
        void shouldHaveCorrectOrdinalForBlocked() {
            // Then
            assertThat(CardStatus.BLOCKED.ordinal()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should have correct ordinal for REPORTED_STOLEN")
        void shouldHaveCorrectOrdinalForReportedStolen() {
            // Then
            assertThat(CardStatus.REPORTED_STOLEN.ordinal()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should have correct ordinal for EXPIRED")
        void shouldHaveCorrectOrdinalForExpired() {
            // Then
            assertThat(CardStatus.EXPIRED.ordinal()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should have correct ordinal for REPLACED")
        void shouldHaveCorrectOrdinalForReplaced() {
            // Then
            assertThat(CardStatus.REPLACED.ordinal()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Name Values")
    class NameValuesTests {

        @Test
        @DisplayName("Should have correct name for ACTIVE")
        void shouldHaveCorrectNameForActive() {
            // Then
            assertThat(CardStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should have correct name for FROZEN")
        void shouldHaveCorrectNameForFrozen() {
            // Then
            assertThat(CardStatus.FROZEN.name()).isEqualTo("FROZEN");
        }

        @Test
        @DisplayName("Should have correct name for BLOCKED")
        void shouldHaveCorrectNameForBlocked() {
            // Then
            assertThat(CardStatus.BLOCKED.name()).isEqualTo("BLOCKED");
        }

        @Test
        @DisplayName("Should have correct name for REPORTED_STOLEN")
        void shouldHaveCorrectNameForReportedStolen() {
            // Then
            assertThat(CardStatus.REPORTED_STOLEN.name()).isEqualTo("REPORTED_STOLEN");
        }

        @Test
        @DisplayName("Should have correct name for EXPIRED")
        void shouldHaveCorrectNameForExpired() {
            // Then
            assertThat(CardStatus.EXPIRED.name()).isEqualTo("EXPIRED");
        }

        @Test
        @DisplayName("Should have correct name for REPLACED")
        void shouldHaveCorrectNameForReplaced() {
            // Then
            assertThat(CardStatus.REPLACED.name()).isEqualTo("REPLACED");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw exception for invalid status name")
        void shouldThrowExceptionForInvalidStatusName() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> CardStatus.valueOf("INVALID_STATUS")
            );
        }

        @Test
        @DisplayName("Should throw exception for null status name")
        void shouldThrowExceptionForNullStatusName() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    NullPointerException.class,
                    () -> CardStatus.valueOf(null)
            );
        }

        @Test
        @DisplayName("Should handle case-sensitive status names")
        void shouldHandleCaseSensitiveStatusNames() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> CardStatus.valueOf("active")
            );
        }

        @Test
        @DisplayName("Should handle status with underscore in name")
        void shouldHandleStatusWithUnderscoreInName() {
            // Then
            assertThat(CardStatus.valueOf("REPORTED_STOLEN")).isEqualTo(CardStatus.REPORTED_STOLEN);
        }
    }

    @Nested
    @DisplayName("Status Comparisons")
    class StatusComparisonsTests {

        @Test
        @DisplayName("Should compare ACTIVE status correctly")
        void shouldCompareActiveStatusCorrectly() {
            // Then
            assertThat(CardStatus.ACTIVE).isEqualTo(CardStatus.ACTIVE);
            assertThat(CardStatus.ACTIVE).isNotEqualTo(CardStatus.FROZEN);
        }

        @Test
        @DisplayName("Should compare FROZEN status correctly")
        void shouldCompareFrozenStatusCorrectly() {
            // Then
            assertThat(CardStatus.FROZEN).isEqualTo(CardStatus.FROZEN);
            assertThat(CardStatus.FROZEN).isNotEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should compare BLOCKED status correctly")
        void shouldCompareBlockedStatusCorrectly() {
            // Then
            assertThat(CardStatus.BLOCKED).isEqualTo(CardStatus.BLOCKED);
            assertThat(CardStatus.BLOCKED).isNotEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should compare REPORTED_STOLEN status correctly")
        void shouldCompareReportedStolenStatusCorrectly() {
            // Then
            assertThat(CardStatus.REPORTED_STOLEN).isEqualTo(CardStatus.REPORTED_STOLEN);
            assertThat(CardStatus.REPORTED_STOLEN).isNotEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should compare EXPIRED status correctly")
        void shouldCompareExpiredStatusCorrectly() {
            // Then
            assertThat(CardStatus.EXPIRED).isEqualTo(CardStatus.EXPIRED);
            assertThat(CardStatus.EXPIRED).isNotEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should compare REPLACED status correctly")
        void shouldCompareReplacedStatusCorrectly() {
            // Then
            assertThat(CardStatus.REPLACED).isEqualTo(CardStatus.REPLACED);
            assertThat(CardStatus.REPLACED).isNotEqualTo(CardStatus.ACTIVE);
        }
    }
}
