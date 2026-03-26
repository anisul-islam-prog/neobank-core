package com.neobank.cards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CardType enum using JUnit 5.
 * Tests enum values and behavior.
 */
@DisplayName("CardType Unit Tests")
class CardTypeTest {

    @Nested
    @DisplayName("Enum Values")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have VIRTUAL type")
        void shouldHaveVirtualType() {
            // Then
            assertThat(CardType.VIRTUAL).isNotNull();
        }

        @Test
        @DisplayName("Should have PHYSICAL type")
        void shouldHavePhysicalType() {
            // Then
            assertThat(CardType.PHYSICAL).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 2 card types")
        void shouldHaveExactly2CardTypes() {
            // Then
            assertThat(CardType.values()).hasSize(2);
        }

        @Test
        @DisplayName("Should allow valueOf for all type names")
        void shouldAllowValueOfForAllTypeNames() {
            // Then
            assertThat(CardType.valueOf("VIRTUAL")).isEqualTo(CardType.VIRTUAL);
            assertThat(CardType.valueOf("PHYSICAL")).isEqualTo(CardType.PHYSICAL);
        }

        @Test
        @DisplayName("Should return type array with correct order")
        void shouldReturnTypeArrayWithCorrectOrder() {
            // When
            CardType[] types = CardType.values();

            // Then
            assertThat(types).containsExactly(
                    CardType.VIRTUAL,
                    CardType.PHYSICAL
            );
        }
    }

    @Nested
    @DisplayName("Ordinal Values")
    class OrdinalValuesTests {

        @Test
        @DisplayName("Should have correct ordinal for VIRTUAL")
        void shouldHaveCorrectOrdinalForVirtual() {
            // Then
            assertThat(CardType.VIRTUAL.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should have correct ordinal for PHYSICAL")
        void shouldHaveCorrectOrdinalForPhysical() {
            // Then
            assertThat(CardType.PHYSICAL.ordinal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Name Values")
    class NameValuesTests {

        @Test
        @DisplayName("Should have correct name for VIRTUAL")
        void shouldHaveCorrectNameForVirtual() {
            // Then
            assertThat(CardType.VIRTUAL.name()).isEqualTo("VIRTUAL");
        }

        @Test
        @DisplayName("Should have correct name for PHYSICAL")
        void shouldHaveCorrectNameForPhysical() {
            // Then
            assertThat(CardType.PHYSICAL.name()).isEqualTo("PHYSICAL");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw exception for invalid type name")
        void shouldThrowExceptionForInvalidTypeName() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> CardType.valueOf("INVALID_TYPE")
            );
        }

        @Test
        @DisplayName("Should throw exception for null type name")
        void shouldThrowExceptionForNullTypeName() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    NullPointerException.class,
                    () -> CardType.valueOf(null)
            );
        }

        @Test
        @DisplayName("Should handle case-sensitive type names")
        void shouldHandleCaseSensitiveTypeNames() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> CardType.valueOf("virtual")
            );
        }
    }

    @Nested
    @DisplayName("Type Comparisons")
    class TypeComparisonsTests {

        @Test
        @DisplayName("Should compare VIRTUAL type correctly")
        void shouldCompareVirtualTypeCorrectly() {
            // Then
            assertThat(CardType.VIRTUAL).isEqualTo(CardType.VIRTUAL);
            assertThat(CardType.VIRTUAL).isNotEqualTo(CardType.PHYSICAL);
        }

        @Test
        @DisplayName("Should compare PHYSICAL type correctly")
        void shouldComparePhysicalTypeCorrectly() {
            // Then
            assertThat(CardType.PHYSICAL).isEqualTo(CardType.PHYSICAL);
            assertThat(CardType.PHYSICAL).isNotEqualTo(CardType.VIRTUAL);
        }
    }
}
