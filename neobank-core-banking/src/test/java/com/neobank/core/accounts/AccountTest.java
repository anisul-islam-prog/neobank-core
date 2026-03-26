package com.neobank.core.accounts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Account domain record using JUnit 5.
 * Tests record construction and validation.
 */
@DisplayName("Account Unit Tests")
class AccountTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create Account with all fields")
        void shouldCreateAccountWithAllFields() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.id()).isEqualTo(id);
            assertThat(account.ownerName()).isEqualTo(ownerName);
            assertThat(account.balance()).isEqualByComparingTo(balance);
        }

        @Test
        @DisplayName("Should create Account with zero balance")
        void shouldCreateAccountWithZeroBalance() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = BigDecimal.ZERO;

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should create Account with negative balance")
        void shouldCreateAccountWithNegativeBalance() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("-100.00");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should create Account with balance having 4 decimal places")
        void shouldCreateAccountWithBalanceHaving4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("123.4567");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when ID is null")
        void shouldThrowExceptionWhenIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new Account(
                    null, "John Doe", new BigDecimal("1000.00")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account id must not be null");
        }

        @Test
        @DisplayName("Should throw exception when owner name is null")
        void shouldThrowExceptionWhenOwnerNameIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new Account(
                    UUID.randomUUID(), null, new BigDecimal("1000.00")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account ownerName must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when owner name is blank")
        void shouldThrowExceptionWhenOwnerNameIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new Account(
                    UUID.randomUUID(), "   ", new BigDecimal("1000.00")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account ownerName must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when owner name is empty")
        void shouldThrowExceptionWhenOwnerNameIsEmpty() {
            // Given/When/Then
            assertThatThrownBy(() -> new Account(
                    UUID.randomUUID(), "", new BigDecimal("1000.00")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account ownerName must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when balance is null")
        void shouldThrowExceptionWhenBalanceIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new Account(
                    UUID.randomUUID(), "John Doe", null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account balance must not be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large balance")
        void shouldHandleVeryLargeBalance() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("10000000.00");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle owner name with special characters")
        void shouldHandleOwnerNameWithSpecialCharacters() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John O'Brien-Smith";
            BigDecimal balance = new BigDecimal("1000.00");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.ownerName()).isEqualTo(ownerName);
        }

        @Test
        @DisplayName("Should handle owner name with unicode characters")
        void shouldHandleOwnerNameWithUnicodeCharacters() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "用户 测试";
            BigDecimal balance = new BigDecimal("1000.00");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.ownerName()).isEqualTo(ownerName);
        }

        @Test
        @DisplayName("Should handle long owner name")
        void shouldHandleLongOwnerName() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "a".repeat(100);
            BigDecimal balance = new BigDecimal("1000.00");

            // When
            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.ownerName()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another Account with same fields")
        void shouldEqualAnotherAccountWithSameFields() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            Account account1 = new Account(id, ownerName, balance);
            Account account2 = new Account(id, ownerName, balance);

            // Then
            assertThat(account1).isEqualTo(account2);
        }

        @Test
        @DisplayName("Should not equal Account with different ID")
        void shouldNotEqualAccountWithDifferentId() {
            // Given
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            Account account1 = new Account(UUID.randomUUID(), ownerName, balance);
            Account account2 = new Account(UUID.randomUUID(), ownerName, balance);

            // Then
            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.hashCode()).isNotNull();
            assertThat(account.hashCode()).isEqualTo(account.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            Account account = new Account(id, ownerName, balance);

            // Then
            assertThat(account.toString()).contains("Account");
            assertThat(account.toString()).contains(ownerName);
        }
    }
}
