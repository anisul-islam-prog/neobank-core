package com.neobank.onboarding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserStatus enum using JUnit 5.
 * Tests enum values and behavior.
 */
@DisplayName("UserStatus Unit Tests")
class UserStatusTest {

    @Nested
    @DisplayName("Enum Values")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have PENDING status")
        void shouldHavePendingStatus() {
            // Then
            assertThat(UserStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("Should have ACTIVE status")
        void shouldHaveActiveStatus() {
            // Then
            assertThat(UserStatus.ACTIVE).isNotNull();
        }

        @Test
        @DisplayName("Should have SUSPENDED status")
        void shouldHaveSuspendedStatus() {
            // Then
            assertThat(UserStatus.SUSPENDED).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 3 status values")
        void shouldHaveExactly3StatusValues() {
            // Then
            assertThat(UserStatus.values()).hasSize(3);
        }

        @Test
        @DisplayName("Should allow valueOf for all status names")
        void shouldAllowValueOfForAllStatusNames() {
            // Then
            assertThat(UserStatus.valueOf("PENDING")).isEqualTo(UserStatus.PENDING);
            assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
            assertThat(UserStatus.valueOf("SUSPENDED")).isEqualTo(UserStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("Ordinal Values")
    class OrdinalValuesTests {

        @Test
        @DisplayName("Should have correct ordinal for PENDING")
        void shouldHaveCorrectOrdinalForPending() {
            // Then
            assertThat(UserStatus.PENDING.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should have correct ordinal for ACTIVE")
        void shouldHaveCorrectOrdinalForActive() {
            // Then
            assertThat(UserStatus.ACTIVE.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should have correct ordinal for SUSPENDED")
        void shouldHaveCorrectOrdinalForSuspended() {
            // Then
            assertThat(UserStatus.SUSPENDED.ordinal()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Name Values")
    class NameValuesTests {

        @Test
        @DisplayName("Should have correct name for PENDING")
        void shouldHaveCorrectNameForPending() {
            // Then
            assertThat(UserStatus.PENDING.name()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("Should have correct name for ACTIVE")
        void shouldHaveCorrectNameForActive() {
            // Then
            assertThat(UserStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should have correct name for SUSPENDED")
        void shouldHaveCorrectNameForSuspended() {
            // Then
            assertThat(UserStatus.SUSPENDED.name()).isEqualTo("SUSPENDED");
        }
    }

    @Nested
    @DisplayName("Status Comparisons")
    class StatusComparisonsTests {

        @Test
        @DisplayName("Should compare PENDING status correctly")
        void shouldComparePendingStatusCorrectly() {
            // Then
            assertThat(UserStatus.PENDING).isEqualTo(UserStatus.PENDING);
            assertThat(UserStatus.PENDING).isNotEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should compare ACTIVE status correctly")
        void shouldCompareActiveStatusCorrectly() {
            // Then
            assertThat(UserStatus.ACTIVE).isEqualTo(UserStatus.ACTIVE);
            assertThat(UserStatus.ACTIVE).isNotEqualTo(UserStatus.PENDING);
        }

        @Test
        @DisplayName("Should compare SUSPENDED status correctly")
        void shouldCompareSuspendedStatusCorrectly() {
            // Then
            assertThat(UserStatus.SUSPENDED).isEqualTo(UserStatus.SUSPENDED);
            assertThat(UserStatus.SUSPENDED).isNotEqualTo(UserStatus.ACTIVE);
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
                    () -> UserStatus.valueOf("INVALID_STATUS")
            );
        }

        @Test
        @DisplayName("Should throw exception for null status name")
        void shouldThrowExceptionForNullStatusName() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    NullPointerException.class,
                    () -> UserStatus.valueOf(null)
            );
        }

        @Test
        @DisplayName("Should handle case-sensitive status names")
        void shouldHandleCaseSensitiveStatusNames() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> UserStatus.valueOf("active")
            );
        }

        @Test
        @DisplayName("Should iterate through all status values")
        void shouldIterateThroughAllStatusValues() {
            // When
            UserStatus[] statuses = UserStatus.values();

            // Then
            assertThat(statuses).containsExactly(UserStatus.PENDING, UserStatus.ACTIVE, UserStatus.SUSPENDED);
        }
    }
}
