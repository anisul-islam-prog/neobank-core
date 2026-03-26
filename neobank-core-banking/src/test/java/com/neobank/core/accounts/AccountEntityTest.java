package com.neobank.core.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AccountEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("AccountEntity Unit Tests")
class AccountEntityTest {

    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        accountEntity = new AccountEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            AccountEntity entity = new AccountEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            accountEntity.setId(id);

            // Then
            assertThat(accountEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get owner name")
        void shouldSetAndGetOwnerName() {
            // Given
            String ownerName = "John Doe";

            // When
            accountEntity.setOwnerName(ownerName);

            // Then
            assertThat(accountEntity.getOwnerName()).isEqualTo(ownerName);
        }

        @Test
        @DisplayName("Should set and get balance")
        void shouldSetAndGetBalance() {
            // Given
            BigDecimal balance = new BigDecimal("1000.00");

            // When
            accountEntity.setBalance(balance);

            // Then
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(balance);
        }

        @Test
        @DisplayName("Should set and get branch ID")
        void shouldSetAndGetBranchId() {
            // Given
            UUID branchId = UUID.randomUUID();

            // When
            accountEntity.setBranchId(branchId);

            // Then
            assertThat(accountEntity.getBranchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should set and get transaction history")
        void shouldSetAndGetTransactionHistory() {
            // Given
            List<String> transactionHistory = new ArrayList<>();
            transactionHistory.add("Transaction 1");
            transactionHistory.add("Transaction 2");

            // When
            accountEntity.setTransactionHistory(transactionHistory);

            // Then
            assertThat(accountEntity.getTransactionHistory()).hasSize(2);
            assertThat(accountEntity.getTransactionHistory()).contains("Transaction 1", "Transaction 2");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            accountEntity.setId(null);

            // Then
            assertThat(accountEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null owner name")
        void shouldHandleNullOwnerName() {
            // When
            accountEntity.setOwnerName(null);

            // Then
            assertThat(accountEntity.getOwnerName()).isNull();
        }

        @Test
        @DisplayName("Should handle null balance")
        void shouldHandleNullBalance() {
            // When
            accountEntity.setBalance(null);

            // Then
            assertThat(accountEntity.getBalance()).isNull();
        }

        @Test
        @DisplayName("Should handle null branch ID")
        void shouldHandleNullBranchId() {
            // When
            accountEntity.setBranchId(null);

            // Then
            assertThat(accountEntity.getBranchId()).isNull();
        }

        @Test
        @DisplayName("Should handle null transaction history")
        void shouldHandleNullTransactionHistory() {
            // When
            accountEntity.setTransactionHistory(null);

            // Then
            assertThat(accountEntity.getTransactionHistory()).isNotNull();
            assertThat(accountEntity.getTransactionHistory()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty transaction history")
        void shouldHandleEmptyTransactionHistory() {
            // When
            accountEntity.setTransactionHistory(new ArrayList<>());

            // Then
            assertThat(accountEntity.getTransactionHistory()).isEmpty();
        }

        @Test
        @DisplayName("Should handle zero balance")
        void shouldHandleZeroBalance() {
            // When
            accountEntity.setBalance(BigDecimal.ZERO);

            // Then
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle negative balance")
        void shouldHandleNegativeBalance() {
            // When
            accountEntity.setBalance(new BigDecimal("-100.00"));

            // Then
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should handle balance with 4 decimal places")
        void shouldHandleBalanceWith4DecimalPlaces() {
            // When
            accountEntity.setBalance(new BigDecimal("123.4567"));

            // Then
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large balance")
        void shouldHandleVeryLargeBalance() {
            // When
            accountEntity.setBalance(new BigDecimal("10000000.00"));

            // Then
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle owner name with special characters")
        void shouldHandleOwnerNameWithSpecialCharacters() {
            // When
            accountEntity.setOwnerName("John O'Brien-Smith");

            // Then
            assertThat(accountEntity.getOwnerName()).isEqualTo("John O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle owner name with unicode")
        void shouldHandleOwnerNameWithUnicode() {
            // When
            accountEntity.setOwnerName("用户 测试");

            // Then
            assertThat(accountEntity.getOwnerName()).isEqualTo("用户 测试");
        }

        @Test
        @DisplayName("Should handle long owner name")
        void shouldHandleLongOwnerName() {
            // When
            accountEntity.setOwnerName("a".repeat(100));

            // Then
            assertThat(accountEntity.getOwnerName()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle long transaction history")
        void shouldHandleLongTransactionHistory() {
            // Given
            List<String> history = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                history.add("Transaction " + i);
            }

            // When
            accountEntity.setTransactionHistory(history);

            // Then
            assertThat(accountEntity.getTransactionHistory()).hasSize(100);
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
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");
            UUID branchId = UUID.randomUUID();
            List<String> transactionHistory = new ArrayList<>();
            transactionHistory.add("Transaction 1");

            // When
            accountEntity.setId(id);
            accountEntity.setOwnerName(ownerName);
            accountEntity.setBalance(balance);
            accountEntity.setBranchId(branchId);
            accountEntity.setTransactionHistory(transactionHistory);

            // Then
            assertThat(accountEntity.getId()).isEqualTo(id);
            assertThat(accountEntity.getOwnerName()).isEqualTo(ownerName);
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(balance);
            assertThat(accountEntity.getBranchId()).isEqualTo(branchId);
            assertThat(accountEntity.getTransactionHistory()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            // When
            accountEntity.setId(id);
            accountEntity.setOwnerName(ownerName);
            accountEntity.setBalance(balance);

            // Then
            assertThat(accountEntity.getId()).isEqualTo(id);
            assertThat(accountEntity.getOwnerName()).isEqualTo(ownerName);
            assertThat(accountEntity.getBalance()).isEqualByComparingTo(balance);
            assertThat(accountEntity.getBranchId()).isNull();
            assertThat(accountEntity.getTransactionHistory()).isEmpty();
        }
    }
}
